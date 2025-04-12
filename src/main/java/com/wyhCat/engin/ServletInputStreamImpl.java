package com.wyhCat.engin;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;

/**
 * @author nsh
 * @data 2025/4/11 14:03
 * @description 自定义的读取输入流类
 **/
public class ServletInputStreamImpl extends ServletInputStream {

    //转换成字节数组的原数据
    private final byte[] data;

    //读取指针
    private int lastIndexRetrieved = -1;

    //设置ReadListener
    private ReadListener readListener = null;

    //构造函数，传入数据
    public ServletInputStreamImpl(byte[] data) {
        this.data = data;
    }

    @Override
    //读取到了文件最后以为那就是读取完了
    public boolean isFinished() {
        return lastIndexRetrieved == data.length - 1;
    }

    @Override
    //默认是打开
    public boolean isReady() {
        return true;
    }

    @Override
    //设置读入监听器（未实现）
    public void setReadListener(ReadListener readListener) {
        this.readListener = readListener;
        if (!isFinished()) {

            try {
                readListener.onDataAvailable();
            } catch (IOException e) {
                readListener.onError(e);
            }
        }else{
            try {
                readListener.onAllDataRead();
            } catch (IOException e) {
                readListener.onError(e);
            }
        }
    }

    @Override
    //读取，一次读取一个字节，返回int
    public int read() throws IOException {
        if(lastIndexRetrieved < data.length - 1){
            lastIndexRetrieved++;
            byte n = data[lastIndexRetrieved];
            if(readListener != null && isFinished()){
                try {
                    readListener.onAllDataRead();
                } catch (IOException e) {
                    readListener.onError(e);
                    throw e;
                }
            }
            return Byte.toUnsignedInt(n);
            //原本给出的代码中，n为int，但是因为解析文件时返回的byte范围是-128-128，不做处理的话会出现-1的情况而意外中断
            //所以需要将其转化成0-255的范围才能正常运作
            //在InputStream的接口说明提到了这一点
        }
        return -1;
    }


    @Override
    //如果读完了那肯定不可用
    public int available() throws IOException {
        return data.length - lastIndexRetrieved - 1;
    }

    @Override
    //关闭
    public void close() throws IOException {
        lastIndexRetrieved = data.length - 1;
    }
}
