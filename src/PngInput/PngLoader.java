package PngInput;

import java.io.*;
import java.util.*;

public class PngLoader {
    public final PngInfo imageInfo;
    PngInflaterInputStream pngInflaterInputStream;
    UnsignedByte[] currentLine = null;
    UnsignedByte[] currentUnfilteredLine = null;
    UnsignedByte[] previousUnfilteredLine = null;

    /**
     * Készít egy PngLoader-t és ellenőrzi, hogy a megadott fájl megfelelő formátumú-e
     * @param imageFile A betöltendő PNG fájl
     * @throws IOException Hibás, nem elérhető fájl esetén IOException
     */
    public PngLoader(File imageFile) throws IOException {
        PngLogger.info("Reading file");
        FileInputStream fis = new FileInputStream(imageFile);
        BufferedInputStream bis = new BufferedInputStream(fis, 8192);
        if(!bis.markSupported()){
            throw new RuntimeException("BufferedInputStream mark() not supported");
        }

        PngLogger.info("Mark supported OK");

        //check PNG signature
        if(!checkSignature( Helper.readExactlyNBytes(bis, 8))){
            throw new RuntimeException("Incorrect signature");
        }

        PngLogger.info("PNG signature OK");

        //read IHDR (Image Header chunk)
        try{
            imageInfo = readIHDR(bis);
        }
        catch(RuntimeException e){
            throw new RuntimeException("Error reading IHDR chunk: " + e.getMessage());
        }

        PngLogger.info("IHDR read OK");

        //Validate that the image is supported
        if(!imageInfo.validate()){
            throw new RuntimeException("This type of PNG is not supported. (Interlaced and Palette based PNGs are not supported)");
        }

        PngLogger.info(imageInfo.toString());
        PngLogger.info("Image type valid");

        //skip CRC of IHDR chunk
        skipCRC(bis);

        //discard data until the first IDAT (image data chunk)
        skipNonIDAT(bis);

        this.pngInflaterInputStream = new PngInflaterInputStream(bis);
        //init done
    }

    /**
     * Leellenőrzi a fájl eleji magic number-t
     * @return Az aláírás megfelel-e PNG-nek
     */
    boolean checkSignature(byte[] signature){
        byte[] officialSignature = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        return Arrays.equals(signature, officialSignature);
    }

    /**
     * Beolvassa a kép metaadatait
     * @param is Az olvasás innen történik
     * @return A képadataokat tároló PngInfo objektum
     * @throws IOException Ha sérült a fájl
     */
    PngInfo readIHDR(InputStream is) throws IOException {
        byte[] lenBuf = Helper.readExactlyNBytes(is, 4);
        String type = Helper.readChunkType(is);

        //read chunk length
        long length = Helper.uint32BytesToLong(lenBuf, 0);
        //read chunk type
        if(!type.equals("IHDR"))
            throw new RuntimeException("Chunk type not IHDR");

        byte[] dataBytes = Helper.readExactlyNBytes(is, 13);

        return new PngInfo(
                (int)Helper.uint32BytesToLong(dataBytes, 0),
                (int)Helper.uint32BytesToLong(dataBytes, 4),
                dataBytes[8],
                dataBytes[9],
                dataBytes[10],
                dataBytes[11],
                dataBytes[12]
        );
    }

    /**
     * Elveti az összes nem IDAT chunkot
     * @param bis Input stream amiről a file jön
     * @throws IOException Sérült fájl esetén
     */
    private void skipNonIDAT(BufferedInputStream bis) throws IOException {
        long length;
        String type;
        do{
            bis.mark(4096);
            length = Helper.readUint32(bis);
            type = Helper.readChunkType(bis);
            if(!type.equals("IDAT")){
                Helper.readExactlyNBytes(bis, (int)length + 4); //+4 for CRC
                PngLogger.info("Skipping non IDAT chunk");
            }else{
                bis.reset();
                break;
            }
        }while(true);
    }

    /**
     * Elhagyja a hibaellenőrző kódot
     * @param is Stream amiről a fájl jön
     * @throws IOException Sérült fájl essetén
     */
    private void skipCRC(InputStream is) throws IOException {
        Helper.readExactlyNBytes(is, 4);
        PngLogger.info("Skipping CRC");
    }

    /**
     * Megadja, hogy az aktuális kép típusa alapján hány bájtból áll a kép egy sora
     * @return Hány bájtból áll egy sor
     */
    private int getLineLengthInBytes() {
        int bytesToLoad;

        if(imageInfo.bitDepth <= 4) {
            //this only occurs with grayscale without alpha (since indexed color is not supported)

            int pixelsPerByte = 8 / imageInfo.bitDepth;
            bytesToLoad = (int) Math.ceil((double) imageInfo.width / pixelsPerByte);
        }else{
            int numChannels = switch (imageInfo.colorType) {
                case 0 ->
                        1;
                case 2 -> //truecolor
                        3;
                case 4 -> //grayscale with alpha
                        2;
                case 6 -> //truecolor with alpha
                        4;
                default -> throw new RuntimeException("Invalid color type");
            };

            int bytesPerPixel = imageInfo.bitDepth/8 * numChannels;
            bytesToLoad = imageInfo.width * bytesPerPixel;
        }

        return bytesToLoad;
    }

    /**
     * Betölti és közben kicsomagolja a következő sor bájtjait
     * @throws IOException Sérült fájl esetén
     */
    private void loadNextScanline() throws IOException {
        if(currentLine == null){
            currentLine = new UnsignedByte[getLineLengthInBytes() + 1]; // +1 for filter type byte
        }
        Helper.readExactlyNUBytes(pngInflaterInputStream, currentLine.length, currentLine, 0);
    }


    /**
     * Veszi következő kitömörített sort és dekódolja a pixeleken található filtert
     * @throws IOException Sérült fájl esetén
     */
    void getNextUnfilteredLine() throws IOException {
        previousUnfilteredLine = currentUnfilteredLine;

        //load the filtered scanline into currentLine
        loadNextScanline();

        currentUnfilteredLine = new UnsignedByte[currentLine.length - 1]; //-1 because filter type byte is not needed

        // reconstruct original values
        for(int i = 0;i<currentUnfilteredLine.length;i++) {
            //bytes used by the filtering
            //looks like this = (x is the current pixel)
            //   cb
            //   ax
            UnsignedByte a, b, c;

            boolean firstPixel = i < imageInfo.numChannels;
            boolean firstLine = previousUnfilteredLine == null;

            if(firstLine && firstPixel){
                a = new UnsignedByte(0);
                b = new UnsignedByte(0);
                c = new UnsignedByte(0);
            }else if (firstLine){//but not first pixel
                b = new UnsignedByte(0);
                c = new UnsignedByte(0);
                a = currentUnfilteredLine[i- imageInfo.numChannels];
            }else if (firstPixel){//but not first line
                a = new UnsignedByte(0);
                c = new UnsignedByte(0);
                b = previousUnfilteredLine[i];
            }else{
                a = currentUnfilteredLine[i - imageInfo.numChannels];
                b = previousUnfilteredLine[i];
                c = previousUnfilteredLine[i - imageInfo.numChannels];
            }

            currentUnfilteredLine[i] = reconstructByte(currentLine[0], a, b, c, currentLine[i+1]);
        }
    }

    /**
     * Vesz egy bájtot és az ezzel szomszédos 3 bájtot és dekódolja az adott bájtra vonatkozó filtert
     * @param filterTypeByte A bájtra vonatkozó filter típusa
     * @param a Balra lévő pixel bájtja
     * @param b Fent lévő pixel bájtja
     * @param c Bal fenti pixel bájtja
     * @param x A dekódolandó bájt
     * @return A dekódolt bájtérték
     */
    UnsignedByte reconstructByte(UnsignedByte filterTypeByte, UnsignedByte a, UnsignedByte b, UnsignedByte c, UnsignedByte x){
        return switch (filterTypeByte.value) {
            case 0 -> //NONE
                    x;
            case 1 -> //SUB
                    UnsignedByte.fromIntMod256(x.value + a.value);
            case 2 -> //UP
                    UnsignedByte.fromIntMod256(x.value + b.value);
            case 3 -> //AVERAGE
                    UnsignedByte.fromIntMod256((x.value + (a.value + b.value)/2));
            case 4 -> //PAETH
                    UnsignedByte.fromIntMod256(x.value + paethPredictor(a, b, c).value);
            default -> throw new RuntimeException("Filter type byte is not within acceptable values");
        };
    }

    /**
     * Dekódolási algoritmus a Paeth filtertípushoz
     * @param a Balra lévő pixel bájtja
     * @param b Fent lévő pixel bájtja
     * @param c Bal fenti pixel bájtja
     * @return A prediktor értéke
     */
    UnsignedByte paethPredictor(UnsignedByte a, UnsignedByte b, UnsignedByte c){
        UnsignedByte Pr;
        int p = a.value + b.value - c.value;
        int pa = Math.abs(p - a.value);
        int pb = Math.abs(p - b.value);
        int pc = Math.abs(p - c.value);
        if (pa <= pb && pa <= pc){
            Pr = a;
        }else if (pb <= pc){
            Pr = b;
        }
        else {
            Pr = c;
        }

        return Pr;
    }

    /**
     * Össszefogja a különböző típusú képek sorainak dekódolását, ehhez tartozik a fájlban található többi hasonló nevű függvény
     * @return pixelek a következő formátumban 0000ARGB, ahol minden betű 8 bitnek felel meg
     * @throws IOException Sérült fájl esetén
     */
    public long[] decodeNextRow() throws IOException {
        getNextUnfilteredLine();
        long[] row;
        if(imageInfo.bitDepth < 8){
            row = decodeGreyscale421();
        }else if (imageInfo.bitDepth == 8){
            row = switch (imageInfo.colorType){
                case 0 -> decodeGreyscale8();
                case 2 -> decodeTruecolor8();
                case 4 -> decodeGreyscaleAlpha8();
                case 6 -> decodeTruecolorAlpha8();
                default -> throw new IllegalStateException("Unexpected value: " + imageInfo.colorType);
            };
        }else{
            throw new RuntimeException("Not implemented yet");
        }

        return row;
    }

    private long[] decodeGreyscale421() {
        long[] result = new long[imageInfo.width];
        int currentPixel = 0;
        for(int i = 0; i<currentUnfilteredLine.length;i++){
            long currentByte = currentUnfilteredLine[i].value;
            if(imageInfo.bitDepth == 1){
                result[currentPixel] = currentByte >>> 7;
                result[currentPixel + 1] = (currentByte >>> 6) & 0b1;
                result[currentPixel + 2] = (currentByte >>> 5) & 0b1;
                result[currentPixel + 3] = (currentByte >>> 4) & 0b1;
                result[currentPixel + 4] = (currentByte >>> 3) & 0b1;
                result[currentPixel + 5] = (currentByte >>> 2) & 0b1;
                result[currentPixel + 6] = (currentByte >>> 1) & 0b1;
                result[currentPixel + 7] = currentByte & 0b1;
            }else if(imageInfo.bitDepth == 2){
                result[currentPixel] = currentByte >>> 6;
                result[currentPixel + 1] = (currentByte >>> 4) & 0b11;
                result[currentPixel + 2] = (currentByte >>> 2) & 0b11;
                result[currentPixel + 3] = currentByte & 0b11;
            }else{
                result[currentPixel] = currentByte >>> 4;
                result[currentPixel + 1] = currentByte & 0b1111;
            }
        }

        expandGreyscale(result);
        return result;
    }
    private void expandGreyscale(long[] values){
        for(int i = 0;i<values.length;i++){
            int max;
            long value;
            if(imageInfo.bitDepth == 1){
                max = 0b1;
                value = ((values[i] / max) * 255);
            }else if(imageInfo.bitDepth == 2){
                max = 0b11;
                value = (int) ((values[i] / max) * 255);
            }else{
                max = 0b1111;
                value = (int) ((values[i] / max) * 255);
            }
            long pixel = 0xFFL << 24 | value << 16 | value << 8 | value;
            values[i] = pixel;
        }
    }



    private long[] decodeGreyscale8() {
        long[] result = new long[imageInfo.width];

        for(int x = 0;x< imageInfo.width;x++){
            long pixel = 0;
            pixel = pixel | 0xFFL << 24; //a
            pixel = pixel | (long)(currentUnfilteredLine[x].value) << 16;   //r
            pixel = pixel | (long)(currentUnfilteredLine[x].value) << 8;  //g
            pixel = pixel | (long)(currentUnfilteredLine[x].value);       //b

            result[x] = pixel;
        }

        return result;
    }

    private long[] decodeGreyscaleAlpha8() {
        long[] result = new long[imageInfo.width];

        for(int x = 0;x< imageInfo.width;x++){
            long pixel = 0;
            pixel = pixel | (long)(currentUnfilteredLine[2*x+1].value) << 24;   //r
            pixel = pixel | (long)(currentUnfilteredLine[2*x].value) << 16;   //r
            pixel = pixel | (long)(currentUnfilteredLine[2*x].value) << 8;  //g
            pixel = pixel | (long)(currentUnfilteredLine[2*x].value);       //b

            result[x] = pixel;
        }

        return result;
    }
    private long[] decodeTruecolorAlpha8() {
        long[] result = new long[imageInfo.width];
        //argb
        for(int currentByte = 0;currentByte< imageInfo.width*4;currentByte+=4){
            long pixel = 0;
            pixel = pixel | (long)(currentUnfilteredLine[currentByte+3].value) << 24; //a
            pixel = pixel | (long)(currentUnfilteredLine[currentByte].value) << 16;   //r
            pixel = pixel | (long)(currentUnfilteredLine[currentByte+1].value) << 8;  //g
            pixel = pixel | (long)(currentUnfilteredLine[currentByte+2].value);       //b
            result[currentByte/4] = pixel;
        }

        return result;
    }
    private long[] decodeTruecolor8(){
        long[] result = new long[imageInfo.width];
        int helper = 0;
        for(int currentByte = 0;currentByte< imageInfo.width*3;currentByte+=3){
            long pixel = 0;
            pixel = pixel | 0xFFL << 24; //a
            pixel = pixel | (long)(currentUnfilteredLine[currentByte].value) << 16;   //r
            pixel = pixel | (long)(currentUnfilteredLine[currentByte+1].value) << 8;  //g
            pixel = pixel | (long)(currentUnfilteredLine[currentByte+2].value);       //b
            result[helper++] = pixel;
        }

        return result;
    }
}