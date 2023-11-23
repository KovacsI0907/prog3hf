package PngOutput;

import ParallelImageProcessing.ImageProcessingContext;
import ParallelImageProcessing.ImageTile;

import java.io.*;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class ImageTiledWriter {
    public final ImageProcessingContext image;
    File outputFile;
    PriorityQueue<ImageTile> tilesReady;
    FileOutputStream fileOutputStream;

    int lastTileWritten = -1;

    Deflater deflater;
    DeflaterOutputStream deflaterOutputStream;
    ByteArrayOutputStream byteArrayOutputStream;

    public ImageTiledWriter(ImageProcessingContext image) throws IOException {
        this.image = image;

        String fileNameWithoutExtension = image.imageFile.getName().replaceFirst("[.][^.]+$", "");
        String newFileName = fileNameWithoutExtension + "_output.png";
        this.outputFile = new File(image.imageFile.getParent(), newFileName);

        this.tilesReady = new PriorityQueue<>(Comparator.comparingInt(ImageTile::getTileIndex));
        this.fileOutputStream = new FileOutputStream(outputFile);

        deflater = new Deflater();
        byteArrayOutputStream = new ByteArrayOutputStream();
        deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, new Deflater(), 10);

        init();
    }

    public void init() throws IOException {
        writeSignature();
        writeIHDR(image.imageWidth, image.imageHeight);
    }

    private void writeSignature() throws IOException {
        byte[] signatureBytes = new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        fileOutputStream.write(signatureBytes);
    }

    public boolean tryWriteNextTile() throws IOException {
        ImageTile nextTile = pollNextTile();
        if(nextTile == null){
            return false;
        }

        if(nextTile.tileIndex != lastTileWritten+1) {
            return false;
        }

        System.out.println("tile height: " + nextTile.height);

        byte[] data = new byte[nextTile.height * (1+4*nextTile.width)];
        int dataIndex = 0;
        for(int y = 0;y<nextTile.height;y++){
            long[] row = nextTile.getRow(y);
            data[dataIndex++] = 0; //filter type byte = NONE
            for (long pixel : row) {
                byte red = (byte) ((pixel >>> 16) & 0xFF);
                byte green = (byte) ((pixel >>> 8) & 0xFF);
                byte blue = (byte) (pixel & 0xFF);
                byte alpha = (byte) ((pixel >>> 24) & 0xFF);

                data[dataIndex++] = red;
                data[dataIndex++] = green;
                data[dataIndex++] = blue;
                data[dataIndex++] = alpha;
            }
        }

        writeIDAT(data);
        lastTileWritten = nextTile.getTileIndex();
        return true;
    }

    public void close() throws IOException {
        writeIEND();
        fileOutputStream.close();
        System.out.println("Closed " + outputFile.getName());
    }

    void writeIHDR(int width, int height) throws IOException {
        writeChunkLength(13);

        byte[] ihdrData = new byte[17];

        ihdrData[0] = 0x49;
        ihdrData[1] = 0x48;
        ihdrData[2] = 0x44;
        ihdrData[3] = 0x52;
        writeIntToByteArray(ihdrData, 4, width);
        writeIntToByteArray(ihdrData, 8, height);
        ihdrData[12] = 8; // Bit depth = 8
        ihdrData[13] = 6; // Color type = Truecolor with alpha
        ihdrData[14] = 0; // Compression method = only value is 0
        ihdrData[15] = 0; // Filter method = only value is 0
        ihdrData[16] = 0; // Interlace method = not interlaced

        fileOutputStream.write(ihdrData);
        fileOutputStream.write(CRC32.calculateCRC(ihdrData));
    }

    void writeIDAT(byte[] data) throws IOException {
        byte[] compressedData = compressWithPrefix(data, new byte[]{'I', 'D', 'A', 'T'});
        writeChunkLength(compressedData.length-4);
        fileOutputStream.write(compressedData);
        fileOutputStream.write(CRC32.calculateCRC(compressedData));
    }

    /*void writeIDAT(byte[] data) throws IOException {
        byte[] compressedData = compressWithPrefix(data, new byte[]{'I', 'D', 'A', 'T'});
        fileOutputStream.write(new byte[]{'L', 'E', 'N'});
        writeChunkLength(compressedData.length-4);
        fileOutputStream.write(new byte[]{'B', 'E', 'G'});
        fileOutputStream.write(compressedData);
        fileOutputStream.write(new byte[]{'C', 'R', 'C'});
        fileOutputStream.write(CRC32.calculateCRC(compressedData));
        fileOutputStream.write(new byte[]{'E', 'N', 'D'});

        System.out.println();
    }*/

    void writeIEND() throws IOException {
        writeChunkLength(0);
        byte[] typeBytes = new byte[]{'I', 'E', 'N', 'D'};
        fileOutputStream.write(typeBytes);
        fileOutputStream.write(CRC32.calculateCRC(typeBytes));
    }


    void writeChunkLength(int length) throws IOException {
        if(length < 0){
            throw new IllegalArgumentException("Length can not be negative");
        }
        fileOutputStream.write((byte)((length >> 24) & 0xFF));
        fileOutputStream.write((byte)((length >> 16) & 0xFF));
        fileOutputStream.write((byte)((length >> 8) & 0xFF));
        fileOutputStream.write((byte)(length & 0xFF));
    }

    void writeIntToByteArray(byte[] array, int offset, int value) {
        array[offset] = (byte) ((value >> 24) & 0xFF);
        array[offset + 1] = (byte) ((value >> 16) & 0xFF);
        array[offset + 2] = (byte) ((value >> 8) & 0xFF);
        array[offset + 3] = (byte) (value & 0xFF);
    }

    /*public byte[] compressWithPrefix(byte[] input, byte[] prefix) throws IOException {
        if (prefix.length != 4) {
            throw new IllegalArgumentException("Prefix must be a byte array of length 4");
        }

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

        // Add 4-byte prefix at the beginning
        outputBuffer.write(prefix);
        deflater.setInput(input);
        while(!deflater.needsInput()){
            byte[] buf = new byte[8192];
            int len = deflater.deflate(buf);

            if(len > 0){
                outputBuffer.write(buf, 0, len);
            }
        }

        if(lastTileWritten == image.tilingContext.numTiles-2){
            deflater.finish();
            while(!deflater.finished()){
                byte[] buf = new byte[8192];
                int len = deflater.deflate(buf);

                if(len > 0){
                    outputBuffer.write(buf, 0, len);
                }
            }
        }

        return outputBuffer.toByteArray();
    }*/

    public byte[] compressWithPrefix(byte[] input, byte[] prefix) throws IOException {
        if (prefix.length != 4) {
            throw new IllegalArgumentException("Prefix must be a byte array of length 4");
        }

        byteArrayOutputStream.reset();
        byteArrayOutputStream.write(prefix);

        deflaterOutputStream.write(input);

        if(lastTileWritten == image.tilingContext.numTiles-2){
            deflaterOutputStream.finish();
            deflaterOutputStream.close();
        }

        return byteArrayOutputStream.toByteArray();
    }

    private ImageTile pollNextTile(){
        if(tilesReady.peek() == null) {
            return null;
        }

        if(tilesReady.peek().tileIndex == lastTileWritten + 1){
            return tilesReady.poll();
        }

        return null;
    }

    boolean isFinished() {
        return lastTileWritten == image.tilingContext.numTiles-1;
    }
}
