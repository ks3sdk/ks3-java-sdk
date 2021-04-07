package com.ksyun.ks3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 下午4:23:11
 * 
 * @description 
 **/
public class RepeatableFileInputStream extends InputStream{
    private final File file;
    private FileInputStream fis = null;
    private long bytesReadPastMarkPoint = 0;
    private long markPoint = 0;
    public RepeatableFileInputStream(File file) throws FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        this.fis = new FileInputStream(file);
        this.file = file;
    }
    public File getFile() {
        return file;
    }

    @Override
    public void reset() throws IOException {
        this.fis.close();
        this.fis = new FileInputStream(file);
        long skipped = 0;
        long toSkip = markPoint;
        while (toSkip > 0) {
            skipped = this.fis.skip(toSkip);
            toSkip -= skipped;
        }
        this.bytesReadPastMarkPoint = 0;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) {
        this.markPoint += bytesReadPastMarkPoint;
        this.bytesReadPastMarkPoint = 0;
    }

    @Override
    public int available() throws IOException {
        return fis.available();
    }

    @Override
    public void close() throws IOException {
        fis.close();
    }

    @Override
    public int read() throws IOException {
        int byteRead = fis.read();
        if (byteRead != -1) {
            bytesReadPastMarkPoint++;
            return byteRead;
        } else {
            return -1;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = fis.skip(n);
        bytesReadPastMarkPoint += skipped;
        return skipped;
    }

    @Override
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        int count = fis.read(arg0, arg1, arg2);
        bytesReadPastMarkPoint += count;
        return count;
    }
}
