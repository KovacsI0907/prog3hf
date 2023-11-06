package PngLoader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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

    @Override
    protected void fill() throws IOException {
        //TODO ensureOpen() is private, find replacement
        //TODO find out if close() is necessary in this class

        this.len = idatLoader.loadNBytes(this.buf, this.buf.length, 0);

        if (this.len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        } else {
            this.inf.setInput(this.buf, 0, this.len);
        }
    }
}
