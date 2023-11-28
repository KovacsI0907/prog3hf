package PngOutput;

import ParallelImageProcessing.ImageProcessingContext;
import ParallelImageProcessing.ImageTile;

import java.io.*;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Ez az osztály egy adott képhez tartozik. Az adott képhez tartozó képdarabokat megkapja majd kiírja egy fájlba.
 */
public class ImageTiledWriter {
    public final ImageProcessingContext imageProcessingContext;
    File outputFile;
    PriorityQueue<ImageTile> tilesReady;
    FileOutputStream fileOutputStream;
    int lastTileWritten = -1;
    DeflaterOutputStream deflaterOutputStream;
    ByteArrayOutputStream byteArrayOutputStream;

    /**
     * Létrehozza az írandó fájlt és beleírja azokat az információkat, amikhez nem kell tényleges képadat
     * @param image A képet leíró context
     * @throws IOException Általános IO hiba
     */
    public ImageTiledWriter(ImageProcessingContext image) throws IOException {
        this.imageProcessingContext = image;
        File outputFolder = image.imageFile.getParentFile();
        if(!outputFolder.isDirectory()){
            throw new IllegalStateException();
        }
        this.outputFile = new File(outputFolder, image.imageFile.getName());

        this.tilesReady = new PriorityQueue<>(Comparator.comparingInt(ImageTile::getTileIndex));
        this.fileOutputStream = new FileOutputStream(outputFile);

        byteArrayOutputStream = new ByteArrayOutputStream();
        deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, new Deflater(), 10);

        init();
    }

    /**
     * Kiírja a Png magic numbert és a tulajdonságotkat tartalmazó Image Header chunkot
     * @throws IOException Sima IO hibáknál
     */
    public void init() throws IOException {
        writeSignature();
        writeIHDR(imageProcessingContext.imageWidth, imageProcessingContext.imageHeight);
    }

    /**
     * Kirja a PNG magic numbert a fájlba
     * @throws IOException Sima IO hibáknál
     */
    private void writeSignature() throws IOException {
        byte[] signatureBytes = new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        fileOutputStream.write(signatureBytes);
    }

    /**
     * Ellenőrzi, hogy pufferben van e a soron következő képdarab és ha igen, akkor kiírja a fájlba egyébkét nem csinál semmit.
     * @return Tudott-e újabb képdarabot kiírni
     * @throws IOException Általános IO hiba
     */
    public boolean tryWriteNextTile() throws IOException {
        ImageTile nextTile = pollNextTile();
        if(nextTile == null){
            return false;
        }

        if(nextTile.tileIndex != lastTileWritten+1) {
            return false;
        }

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

    /**
     * Kiírja a PNG fájlokat lezáró IEND chunkot és bezárja a folyamot
     * @throws IOException Általános IO hiba
     */
    public void close() throws IOException {
        writeIEND();
        fileOutputStream.close();
        System.out.println("Closed " + outputFile.getName());
    }

    /**
     * Kiírja az Image Header chunkot. Minden képhez ugyanazt írja, mert minden képet ugyanolyan fajta pngként mentünk
     * @param width Kép szélessége
     * @param height Kép magassága
     * @throws IOException Általáno IO hiba
     */
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

    /**
     * Veszi az adott adattömböt tömöríti és egy IDAT chunk belsejében kiírja
     * @param data A kiírandó és tömörítendő adat
     * @throws IOException Tömörítéssel kapcsolatos hibák (rossz bemenet esetén)
     */
    void writeIDAT(byte[] data) throws IOException {
        byte[] compressedData = compressWithPrefix(data, new byte[]{'I', 'D', 'A', 'T'});
        writeChunkLength(compressedData.length-4);
        fileOutputStream.write(compressedData);
        fileOutputStream.write(CRC32.calculateCRC(compressedData));
    }

    /**
     * Kiírja a képet lezáró IEND chunkot
     * @throws IOException Általános IO hiba
     */
    void writeIEND() throws IOException {
        writeChunkLength(0);
        byte[] typeBytes = new byte[]{'I', 'E', 'N', 'D'};
        fileOutputStream.write(typeBytes);
        fileOutputStream.write(CRC32.calculateCRC(typeBytes));
    }


    /**
     * Kír egy 4 bájtos számot
     * @param length integer ami ki lesz írva
     * @throws IOException Általános IO hiba
     */
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

    /**
     * Bevesz egy adattömböt és egy prefixet, az adatot tömöríti a prefixet pedig beteszi a tömörített adat elé
     * Tömörítés: DEFLATE
     * @param input Tömörítendő adat
     * @param prefix Prefix adat elé
     * @return prefix + tömörített adat
     * @throws IOException Általános IO hiba
     */
    public byte[] compressWithPrefix(byte[] input, byte[] prefix) throws IOException {
        if (prefix.length != 4) {
            throw new IllegalArgumentException("Prefix must be a byte array of length 4");
        }

        byteArrayOutputStream.reset();
        byteArrayOutputStream.write(prefix);

        deflaterOutputStream.write(input);

        if(lastTileWritten == imageProcessingContext.tilingContext.numTiles-2){
            deflaterOutputStream.finish();
            deflaterOutputStream.close();
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Leveszi a queueból a következő képdarabot, ha az a következő a sorrendben
     * @return A levett képdarab
     */
    private ImageTile pollNextTile(){
        if(tilesReady.peek() == null) {
            return null;
        }

        if(tilesReady.peek().tileIndex == lastTileWritten + 1){
            return tilesReady.poll();
        }

        return null;
    }

    /**
     * Visszaadja, hogy az író kész van-e
     * @return Kész van-e ez az író
     */
    boolean isFinished() {
        return lastTileWritten == imageProcessingContext.tilingContext.numTiles-1;
    }

    /**
     * Visszaadja, hogy hány képdarab van még hátra a kép végéig.
     * Ez alapján döntjük el, hogy melyik képet írjuk legközelebb
     * @return Hány képdarab van hátra
     */
    public int tilesLeft() {
        return imageProcessingContext.tilingContext.numTiles - (lastTileWritten + 1);
    }

    /**
     * Félbeszakított kép esetén (mert pl. hibás volt a fájl vége, de ez csak akkor derült ki) bezárja a streamet és kitörli a hibás fájlt
     * @throws IOException Általános IO hiba
     */
    public void terminate() throws IOException {
        fileOutputStream.close();
        outputFile.delete();
    }
}
