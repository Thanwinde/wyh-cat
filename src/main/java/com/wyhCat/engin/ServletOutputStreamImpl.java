package com.wyhCat.engin;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author nsh
 * @data 2025/4/11 16:36
 * @description
 **/
public class ServletOutputStreamImpl extends ServletOutputStream {

    private final OutputStream output;
    private WriteListener writeListener = null;

    public ServletOutputStreamImpl(OutputStream output) {
        this.output = output;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void close() throws IOException {
        this.output.close();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        this.writeListener = writeListener;
        try {
            this.writeListener.onWritePossible();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            this.output.write(b);
        } catch (IOException e) {
            if (this.writeListener != null) {
                this.writeListener.onError(e);
            }
            throw e;
        }
    }
}
