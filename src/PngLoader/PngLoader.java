package PngLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

public class PngLoader {
    public final PngInfo imageInfo;

    //index of the current unfiltered scanline
    private int currentHeight;

    PngInflaterInputStream pngInflaterInputStream;
    UnsignedByte[] currentLine = null;
    UnsignedByte[] currentUnfilteredLine = null;
    UnsignedByte[] previousUnfilteredLine = null;



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
    boolean checkSignature(byte[] signature){
        byte[] officialSignature = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        return Arrays.equals(signature, officialSignature);
    }

    PngInfo readIHDR(InputStream is) throws IOException {
        byte[] lenBuf = Helper.readExactlyNBytes(is, 4);
        byte[] typeBuf = Helper.readExactlyNBytes(is, 4);
        //read chunk length
        long length = Helper.uint32BytesToLong(lenBuf, 0);
        //read chunk type
        String type = Helper.getChunkType(typeBuf);
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

    //discards all chunks until the first IDAT
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

    private void skipCRC(InputStream is) throws IOException {
        Helper.readExactlyNBytes(is, 4);
        PngLogger.info("Skipping CRC");
    }

    private int getLineLengthInBytes() {
        int bytesToLoad;

        if(imageInfo.bitDepth <= 4) {
            //this only occurs with grayscale without alpha (since indexed color is not supported)

            int pixelsPerByte = 8 / imageInfo.bitDepth;
            bytesToLoad = (int) Math.ceil((double) imageInfo.width / pixelsPerByte);
        }else{
            int numChannels = switch (imageInfo.colorType) {
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
    private void loadNextScanline() throws IOException {


        if(currentLine == null){
            currentLine = new UnsignedByte[getLineLengthInBytes() + 1]; // +1 for filter type byte
        }
        Helper.readExactlyNUBytes(pngInflaterInputStream, currentLine.length, currentLine, 0);
        PngLogger.info("currentLine: " + currentHeight + "/" + imageInfo.height);
    }

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

        if(previousUnfilteredLine != null){
            currentHeight++;
        }
    }

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

    public static String intToHexWithFixedLength(int value) {
        // Convert the integer to a hexadecimal string with leading zeros
        String hexString = String.format("%08X", value);

        // Insert spaces between pairs of characters (e.g., "00AABBCC" -> "00 AA BB CC")
        StringBuilder formattedHex = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            formattedHex.append(hexString, i, i + 2);
            if (i + 2 < hexString.length()) {
                formattedHex.append(" ");
            }
        }

        return formattedHex.toString();
    }

    public ImageTile getTile(int tileHeight) throws IOException {
        int[][][] pixelValues = new int[imageInfo.width][tileHeight][imageInfo.numChannels];
        int ulx = 0;
        int uly = currentHeight;
        int lrx = imageInfo.width-1;
        int lry = uly + tileHeight;

        for(int y = currentHeight;y<tileHeight;y++){
            getNextUnfilteredLine();
            int byteIndex = 0;

            //extract the pixel values from the byte array
            int x = 0;
            switch(imageInfo.bitDepth){
                case 1:
                    //image can only be grayscale without alpha
                    x = 0;
                    while(x<imageInfo.width){
                        int currentByte = currentUnfilteredLine[x/8].value;
                        pixelValues[x][y][0] = (currentByte >>> (7-x%8)) & 0b1;
                        x++;
                    }
                    break;
                case 2:
                    x = 0;
                    while(x<imageInfo.width){
                        int currentByte = currentUnfilteredLine[x/4].value;
                        pixelValues[x][y][0] = (currentByte >>> ((3-x%4)*2)) & 0b11;
                        x++;
                    }
                    break;
                case 4:
                    x = 0;
                    while(x< imageInfo.width){
                        int currentByte = currentUnfilteredLine[x/2].value;
                        pixelValues[x][y][0] = (currentByte >>> ((1-x%2)*4)) & 0b1111;
                        x++;
                    }
                    break;
                case 8:


            }

            for(int x = 0;x<imageInfo.width;x++){
                switch(imageInfo.bitDepth){
                    case 1: {
                        //image can only be grayscale without alpha
                        for(int i = 0;i<)
                    }
                    case 8: {
                        for(int i = 0;i< imageInfo.numChannels;i++){
                            pixelValues[x][y][i] = currentUnfilteredLine[byteIndex].value;
                            byteIndex++;
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException("Not implemented yet");
                }
            }
        }

        return new ImageTile(ulx, uly, imageInfo.width, tileHeight, imageInfo, pixelValues);
    }
}