package PngInput;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Az InflaterInputStream leszármaztatása annyi változtatással, hogy a fill() függvényt egy IDATLoader-rel látjuk el
 */
public class PngInflaterInputStream extends InflaterInputStream {
    IDATLoader idatLoader;

    public PngInflaterInputStream(InputStream in, Inflater inf, int size) throws IOException {
        super(in, inf, size);
        idatLoader = new IDATLoader(in);
        PngLogger.info("Png inflater initialized");
    }

    public PngInflaterInputStream(InputStream in, Inflater inf) throws IOException {
        super(in, inf);
        idatLoader = new IDATLoader(in);
    }

    public PngInflaterInputStream(InputStream in) throws IOException {
        super(in);
        idatLoader = new IDATLoader(in);
    }

    /**
     * Amikor az InflaterInputStream-be épített Inflaternek inputra van szüksége, akkor IDATLoaderből betölt
     * @throws IOException Ha az IDATLoader nem tud elég bájtot betölteni
     */
    @Override
    protected void fill() throws IOException {
        this.len = idatLoader.loadNBytes(this.buf, this.buf.length, 0);

        if (this.len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        } else {
            this.inf.setInput(this.buf, 0, this.len);
        }
    }
}
