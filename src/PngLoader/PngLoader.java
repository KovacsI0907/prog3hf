package PngLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.cbrt;

public class PngLoader {
    public final PngInfo imageInfo;
    private int currentHeight;

    PngInflaterInputStream pngInflaterInputStream;
    byte[] currentLine = null;
    byte[] currentUnfilteredLine = null;
    byte[] previousUnfilteredLine = null;



    public PngLoader(File imageFile) throws IOException {
        PngLogger.info("Reading file");
        FileInputStream fis = new FileInputStream(imageFile);
        BufferedInputStream bis = new BufferedInputStream(fis, 8192);
        if(!bis.markSupported()){
            throw new RuntimeException("BufferedInputStream mark() not supported");
        }

        PngLogger.info("Mark supported OK");

        //check PNG signature
        if(!checkSignature( Helper.readExactlyNBytes(bis, 8) )){
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
        currentHeight = 0;
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
                Helper.uint32BytesToLong(dataBytes, 0),
                Helper.uint32BytesToLong(dataBytes, 4),
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
    private void loadNextScanline() throws IOException {
        if(currentLine == null){
            currentLine = new byte[(int)imageInfo.width* imageInfo.bytesPerPixel + 1];
        }
        Helper.readExactlyNBytes(pngInflaterInputStream, (int)imageInfo.width*imageInfo.bytesPerPixel + 1, currentLine, 0);
        currentHeight++;
        PngLogger.info("currentLine: " + currentHeight + "/" + imageInfo.height);
    }

    void getNextUnfilteredLine() throws IOException {
        //load the unfiltered scanline into currentLine
        loadNextScanline();

        currentUnfilteredLine = new byte[(int) (imageInfo.width * imageInfo.bytesPerPixel)];

        //logFilterType(currentLine[0], currentHeight);

        for(int i = 0;i<currentUnfilteredLine.length;i++) {
            //bytes used by the filtering
            //looks like this = (x is the current pixel)
            //   cb
            //   ax
            byte a, b, c;

            boolean firstPixel = i < imageInfo.bytesPerPixel;
            boolean firstLine = previousUnfilteredLine == null;

            if(firstLine && firstPixel){
                c = a = b = 0;
            }else if (firstLine){//but not first pixel
                c = b = 0;
                a = currentUnfilteredLine[i- imageInfo.bytesPerPixel];
            }else if (firstPixel){//but not first line
                c = a = 0;
                b = previousUnfilteredLine[i];
            }else{
                a = currentUnfilteredLine[i- imageInfo.bytesPerPixel];
                b = previousUnfilteredLine[i];
                c = previousUnfilteredLine[i- imageInfo.bytesPerPixel];
            }

            currentUnfilteredLine[i] = reconstructByte(currentLine[0], a, b, c, currentLine[i+1]);
        }

        previousUnfilteredLine = currentUnfilteredLine;
    }

    byte reconstructByte(byte filterTypeByte, byte a, byte b, byte c, byte x){
        if(currentHeight == 1){
            PngLogger.debug("" + x);
        }
        return switch (filterTypeByte) {
            case 0 -> //NONE
                    x;
            case 1 -> //SUB
                    (byte) ((x + a) & 0xff);
            case 2 -> //UP
                    (byte) (x + b);
            case 3 -> //AVERAGE
                    (byte) (x + (a + b) / 2);
            case 4 -> //PAETH
                    (byte) (x + paethPredictor(a, b, c));
            default -> throw new RuntimeException("Filter type byte is not within acceptable values");
        };
    }

    void logFilterType(int filterTypeByte, int rowNumber) {
        switch (filterTypeByte) {
            case 0:
                PngLogger.debug("Current scanline filter type is NONE at line " + rowNumber);
                break;
            case 1:
                PngLogger.debug("Current scanline filter type is SUB at line " + rowNumber);
                break;
            case 2:
                PngLogger.debug("Current scanline filter type is UP at line " + rowNumber);
                break;
            case 3:
                PngLogger.debug("Current scanline filter type is AVERAGE at line " + rowNumber);
                break;
            case 4:
                PngLogger.debug("Current scanline filter type is PAETH at line " + rowNumber);
                break;
            default:
                PngLogger.debug("Filter type byte is not within acceptable values at line " + rowNumber);
        }
    }



    byte paethPredictor(byte a, byte b, byte c){
        byte Pr;
        byte p = (byte) (a + b - c);
        byte pa = (byte) Math.abs(p - a);
        byte pb = (byte) Math.abs(p - b);
        byte pc = (byte) Math.abs(p - c);
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

    public Image getImage() throws IOException {
        int[] pixels = new int[(int)imageInfo.width * (int)imageInfo.height];

        for(int y = 0;y<imageInfo.height;y++){
            getNextUnfilteredLine();
            for(int x = 0;x<imageInfo.width;x++){
                int pixelIndex = (int) (y* imageInfo.width + x);
                pixels[pixelIndex] = byte4ToPixel(currentUnfilteredLine, x*imageInfo.bytesPerPixel, imageInfo.bytesPerPixel);
            }
        }

        return drawImage((int) imageInfo.width, (int) imageInfo.height, pixels);
    }

    public int byte4ToPixel(byte[] arr, int offset, int bytesPerPixel){
        int r = arr[offset] & 0xff;
        int g = arr[offset+1] & 0xff;
        int b = arr[offset+2] & 0xff;
        int a = 255;

        //TODO replace with more universal solution
        if(bytesPerPixel == 4) {
            a = arr[offset + 3] & 0xff;
        }

        return (a << 24) | (r << 16) | g << 8 | b;
    }

    public static BufferedImage drawImage(int width, int height, int[] pixels) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        image.setRGB(0, 0, width, height, pixels, 0, width);

        return image;
    }
}


