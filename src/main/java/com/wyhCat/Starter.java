package com.wyhCat;

import com.wyhCat.connector.HttpConnector;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * @author nsh
 * @data 2025/4/7 10:57
 * @description wyhCat的启动类
 **/
@Slf4j
public class Starter {


    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        try {
            HttpConnector connector = new HttpConnector(host,port);
            log.info("创建HTTP服务器" );
            while(true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
                //对于没有被处理一直传上来的异常就关闭
            }

        } catch ( IOException e) {
            e.printStackTrace();
        }

    }
}
