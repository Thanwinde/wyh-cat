package com.wyhCat.classloader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
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

//这个类会根据URL来加载class和jar文件
public class WebAppClassLoader extends URLClassLoader {


    final Logger logger = LoggerFactory.getLogger(getClass());

    final Path classPath;
    final Path[] libJars;

    public WebAppClassLoader(Path classPath, Path libPath) throws IOException {
        super("WebAppClassLoader",createUrls(classPath,libPath),ClassLoader.getSystemClassLoader());
        //这里会构造一个名为“WebAppClassLoader”的类加载器，传入WebApp的class和jar的URL路径，会委派Application ClassLoader加载
        //其实classPath也能看成是一个Jar文件/路径，加载策略都是从中找到class文件
        //那为什么拆开来看呢？因为这样更好区分外来库和webApp本身
        this.classPath = classPath.toAbsolutePath().normalize();
        //toAbsolutePath会转换为绝对路径，normalize会规范路径（去掉类似..）
        this.libJars = Files.list(libPath).filter(p->p.toString().endsWith(".jar")).map(p->p.toAbsolutePath().normalize()).sorted().toArray(Path[]::new);
        //会获取到libPath里面所有以.jar结尾的文件路径，对其规范化并排序，放入libJars中
        logger.info("获取类路径 : {}", classPath);
        Arrays.stream(libJars).forEach(j->{
            logger.info("获取lib包路径:{}",j);
        });
    }

    //会把全部jar，class文件的Path转化成URL模式返回，对于classPath会返回class的目录，libPath会把每一个jar文件的路径返回
    static URL[] createUrls(Path classPath, Path libPath) throws IOException {
        List<URL> urls = new ArrayList<>();
        urls.add(toDirURL(classPath));
        Files.list(libPath).filter(p->p.toString().endsWith(".jar")).sorted().forEach(p->{
            urls.add(toJarURL(p));
        });
        return urls.toArray(URL[]::new);
    }

    //会把一个URL转化成指向本地目录的URL
    static URL toDirURL(Path p) {
        try{
            if(Files.isDirectory(p)){
                String abs = toAbsPath(p);
                if(!abs.endsWith("/")){
                    abs = abs + "/";
                    //结尾没有"/"会手动加上，确保是目录
                }
                return URI.create("file://" + abs).toURL();
            }
            throw new IOException("Path is not a directory: " + p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //同理，会返回一个jar的URL路径
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

    public void scanClassPath(Consumer<Resource> handler){
        scanClassPath0(handler,this.classPath,this.classPath);
    }

    void scanClassPath0(Consumer<Resource> handler, Path basePath, Path path){
        try{
            Files.list(path).sorted().forEach(p->{
                if(Files.isDirectory(p)){
                    scanClassPath0(handler,basePath,p);
                } else if (Files.isRegularFile(p)) {
                    Path subPath = basePath.relativize(p);
                    handler.accept(new Resource(p,subPath.toString().replace('\\','/')));
                    //这会递归扫描basePath下的所有文件，并接受一个Consumer方法来操作里面的文件
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
        //作用同scanClassPath0
        JarFile jarFile = new JarFile(jarPath.toFile());
        jarFile.stream().filter(entry-> !entry.isDirectory()).forEach(entry->{
            String entryName = entry.getName();
            handler.accept(new Resource(jarPath,entryName));
        });
    }




    static String toAbsPath(Path p) {
        //会把一个URL转化成绝对路径
        return p.toAbsolutePath().normalize().toString().replace('\\','/');
        //这样转化的原因是'/'在unix和windows都能识别，但unix不能识别'\'
    }
}
