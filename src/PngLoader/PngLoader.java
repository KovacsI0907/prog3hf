package PngLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PngLoader {

    public final PngInfo imageInfo;
    private int currentHeight;

    PngInflaterInputStream pngInflaterInputStream;
    byte[] currentLine = null;
    byte[] currentUnfilteredLine = null;
    byte[] previousLine = null;


    public PngLoader(File imageFile) throws IOException {
        FileInputStream fis = new FileInputStream(imageFile);
        BufferedInputStream bis = new BufferedInputStream(fis, 8192);

        if(!bis.markSupported()){
            throw new RuntimeException("BufferedInputStream mark() not supported");
        }

        //check PNG signature
        if(!checkSignature( Helper.readExactlyNBytes(bis, 8) )){
            throw new RuntimeException("Incorrect signature");
        }

        //read IHDR (Image Header chunk)
        try{
            imageInfo = readIHDR(bis);
        }
        catch(RuntimeException e){
            throw new RuntimeException("Error reading IHDR chunk: " + e.getMessage());
        }

        //Validate that the image is supported
        if(!imageInfo.validate()){
            throw new RuntimeException("This type of PNG is not supported. (Interlaced and Palette based PNGs are not supported)");
        }

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
            }else{
                bis.reset();
                break;
            }
        }while(true);
    }

    private void skipCRC(InputStream is) throws IOException {
        Helper.readExactlyNBytes(is, 4);
    }
    private void loadNextScanline() throws IOException {
        if(currentLine == null){
            currentLine = new byte[(int)imageInfo.width*4 + 1];
        }

        previousLine = currentLine;
        Helper.readExactlyNBytes(pngInflaterInputStream, (int)imageInfo.width*4 + 1, currentLine, 0);
        currentHeight++;
    }

    void getNextUnfilteredLine() throws IOException {
        loadNextScanline();

        currentUnfilteredLine = new byte[(int) (imageInfo.width * 4)];
        //TODO extend to other filter types

        if(currentLine[0] == 1){//filter type is sub

            int bpp = 4;
            //sub(x) = raw(x) - raw(x-bpp)
            //raw(x) = sub(x) + raw(x-bpp)

            //skip first 4 bytes
            currentUnfilteredLine[0] = currentLine[1];
            currentUnfilteredLine[1] = currentLine[2];
            currentUnfilteredLine[2] = currentLine[3];
            currentUnfilteredLine[3] = currentLine[4];
            for(int i = 4;i<currentUnfilteredLine.length;i++){
                currentUnfilteredLine[i] = (byte) (currentLine[i+1] + currentUnfilteredLine[i-bpp]);
            }
        }else{
            for(int i = 0;i<currentUnfilteredLine.length;i++){
                currentUnfilteredLine[i] = currentLine[i+1];
            }
        }


    }

    public Image getImage() throws IOException {
        int[] pixels = new int[(int)imageInfo.width * (int)imageInfo.height];

        for(int y = 0;y<imageInfo.height;y++){
            getNextUnfilteredLine();
            for(int x = 0;x<imageInfo.width;x++){
                int pixelIndex = (int) (y* imageInfo.width + x);
                pixels[pixelIndex] = byte4ToPixel(currentUnfilteredLine, x*4);
            }
        }

        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource((int)imageInfo.width, (int)imageInfo.width, pixels, 0, (int)imageInfo.width));
    }

    public int byte4ToPixel(byte[] arr, int offset){
        int r = arr[offset] & 0xff;
        int g = arr[offset+1] & 0xff;
        int b = arr[offset+2] & 0xff;
        int a = arr[offset+3] & 0xff;

        return (a << 24) | (r << 16) | g << 8 | b;
    }
}


