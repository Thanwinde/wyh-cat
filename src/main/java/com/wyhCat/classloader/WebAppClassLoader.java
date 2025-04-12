package com.wyhCat.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarFile;

/**
 * @author nsh
 * @data 2025/4/12 19:12
 * @description
 **/
public class WebAppClassLoader extends URLClassLoader {


    final Logger logger = LoggerFactory.getLogger(getClass());

    final Path classPath;
    final Path[] libJars;

    public WebAppClassLoader(Path classPath, Path libPath) throws IOException {
        super("WebAppClassLoader",createUrls(classPath,libPath),ClassLoader.getSystemClassLoader());
        this.classPath = classPath.toAbsolutePath().normalize();
        this.libJars = Files.list(libPath).filter(p->p.toString().endsWith(".jar")).map(p->p.toAbsolutePath().normalize()).sorted().toArray(Path[]::new);
        logger.info("获取类路径 : {}", classPath);
        Arrays.stream(libJars).forEach(j->{
            logger.info("获取lib包路径:{}",j);
        });
    }

    public void scanClassPath(Consumer<Resource> handler){
        scanClassPath0(handler,this.classPath,this.classPath);
    }

    void scanClassPath0(Consumer<Resource> handler, Path basePath, Path path){
        try{
            Files.list(path).sorted().forEach(p->{
                if(Files.isDirectory(p)){
                    scanClassPath0(handler,basePath,p);
                } else if (Files.isRegularFile(p)) {
                    Path subPath = path.relativize(p);
                    handler.accept(new Resource(p,subPath.toString().replace('\\','/')));
                }
            });
        }catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public void scanJar(Consumer<Resource> handler){
        for(Path jar : libJars){
            try {
                scanJar0(handler,jar);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void scanJar0(Consumer<Resource> handler, Path jarPath) throws IOException {
        JarFile jarFile = new JarFile(jarPath.toFile());
        jarFile.stream().filter(entry-> !entry.isDirectory()).forEach(entry->{
            String entryName = entry.getName();
            handler.accept(new Resource(jarPath,entryName));
        });
    }

    static URL[] createUrls(Path classPath, Path libPath) throws IOException {
        List<URL> urls = new ArrayList<>();
        urls.add(toDirURL(classPath));
        Files.list(libPath).filter(p->p.toString().endsWith(".jar")).sorted().forEach(p->{
            urls.add(toJarURL(p));
        });
        return urls.toArray(URL[]::new);
    }

    static URL toDirURL(Path p) {
        try{
            if(Files.isDirectory(p)){
                String abs = toAbsPath(p);
                if(!abs.endsWith("/")){
                    abs = abs + "/";
                }
                return URI.create("file://" + abs).toURL();
            }
            throw new IOException("Path is not a directory: " + p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static URL toJarURL(Path p) {
        try {
            if(Files.isRegularFile(p)) {
                String abs = toAbsPath(p);
                return URI.create("jar:file://" + abs).toURL();
            }
            throw new IOException("Path is not a jar file: " + p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String toAbsPath(Path p) {
        return p.toAbsolutePath().normalize().toString().replace('\\','/');
    }
}
