package com.wyhCat;

import com.wyhCat.classloader.Resource;
import com.wyhCat.classloader.WebAppClassLoader;
import com.wyhCat.connector.HttpConnector;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarFile;


/**
 * @author nsh
 * @data 2025/4/7 10:57
 * @description wyhCat的启动类
 **/
public class Starter {

    final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception {
        String warFile = null;

        Options options = new Options();

        options.addOption(Option.builder("w").longOpt("war").argName("WebAppFile").hasArg().desc("Web文件位置").required().build());
        //用options来读取启动命令行，形如:
        //java -jar wyh-cat-1.0.jar --war 5mm.war
        //5mm.war和wyh-cat-1.0.jar放一起
        try {
            var parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            warFile = cmd.getOptionValue("war");
            //获取war参数的内容，war参数里面是web的相对路径
        } catch (Exception e) {
            System.err.println(e.getMessage());
            var help = new HelpFormatter();
            var jarname = Path.of(Starter.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName().toString();
            help.printHelp("java -jar " + jarname + " [options]", options);
            System.exit(1);
            return;
            //出现异常退出
        }
        new Starter().start(warFile);
    }

        void start(String warFile) throws IOException {
            Path warPath = Path.of(warFile).toAbsolutePath().normalize();
            //warFile只能用相对路径,这里的warPath就是WebApp的根目录
            if(!Files.isRegularFile(warPath) && !Files.isDirectory(warPath)) {
                System.err.println(warFile + " 不是一个文件");
                System.exit(1);
            }//找不到就退出
            Path[] paths = extractWar(warPath);
            //如果该war是目录，就会提取出class和lib路径返回
            //如果不是的话，就会将其解压到tmp文件夹下再返回
            String webRoot = paths[0].getParent().getParent().toString();
            log.info("WebApp根目录: {}",webRoot);

            WebAppClassLoader classLoader = new WebAppClassLoader(paths[0],paths[1]);

            List<Class<?>> servlets = new ArrayList<>();
            List<Class<?>> listeners = new ArrayList<>();
            List<Class<?>> filters = new ArrayList<>();
            Consumer<Resource> handler = r -> {
              if(r.name.endsWith(".class")) {
                  String className = r.name.substring(0,r.name.length()-6).replace('/', '.');
                  if(className.endsWith("module-info") || className.endsWith("package-info")) {
                      return;
                  }
                  Class<?> clazz;

                  try {
                      clazz = classLoader.loadClass(className);
                  } catch (ClassNotFoundException e) {
                      log.warn("加载类 '{}' 失败: {}: {}", className, e.getClass().getSimpleName(), e.getMessage());
                      return;
                  }

                  if (clazz.isAnnotationPresent(WebServlet.class)) {
                      log.info("加载 WebServlet: {}", clazz.getName());
                      servlets.add(clazz);
                  }
                  if (clazz.isAnnotationPresent(WebFilter.class)) {
                      log.info("加载 WebFilter: {}", clazz.getName());
                      filters.add(clazz);
                  }
                  if (clazz.isAnnotationPresent(WebListener.class)) {
                      log.info("加载 WebListener: {}", clazz.getName());
                      listeners.add(clazz);
                  }
              }
            };
            classLoader.scanJar(handler);
            classLoader.scanClassPath(handler);

            String host = "localhost";
            int port = 8080;
            try {
                HttpConnector connector = new HttpConnector(host,port,servlets,filters,listeners,classLoader);
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

        private Path[] extractWar(Path warPath) throws IOException {
            if(Files.isDirectory(warPath)) {
                log.info(warPath + " 已解压，直接使用 :{}",warPath);
                Path classPath = warPath.resolve("WEB-INF/classes");
                Path libPath = warPath.resolve("WEB-INF/lib");
                Files.createDirectories(classPath);
                Files.createDirectories(libPath);
                return new Path[] {classPath,libPath};
            }
            Path extractPath = Path.of("tmp").toAbsolutePath().normalize();
            //Path extractPath = Paths.get("E:\\tmp");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    deleteDir(extractPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //这里会在关闭服务器时删除tmp文件夹
            }));
            log.info("解压war '{}' 到 '{}'", warPath, extractPath);
            JarFile war = new JarFile(warPath.toFile());
            war.stream().sorted((e1, e2) -> e1.getName().compareTo(e2.getName())).forEach(e -> {
                //判断是不是文件，是文件就拷贝（空文件夹会忽略）
                if(!e.isDirectory()){
                    Path file = extractPath.resolve(e.getName());
                    //把文件名接到extractPath上，因为war里面的文件的文件名都是带着路径的
                    Path dir = file.getParent();
                    //获得该条目的父目录
                    if(!Files.isDirectory(dir)){
                        //父目录不存在就创建
                        try{
                            Files.createDirectories(dir);
                        }catch (IOException ex){
                            throw new UncheckedIOException(ex);
                        }
                    }
                    try (InputStream in = war.getInputStream(e)) {
                        Files.copy(in, file);
                        //拷贝文件
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            });
            Path classPath = extractPath.resolve("WEB-INF/classes");
            Path libPath = extractPath.resolve("WEB-INF/lib");
            Files.createDirectories(classPath);
            Files.createDirectories(libPath);
            //确保存在
            return new Path[] {classPath,libPath};
        }


        void deleteDir(Path p) throws IOException {
            Files.list(p).forEach(c -> {
                try {
                    if (Files.isDirectory(c)) {
                        deleteDir(c);
                    } else {
                        Files.delete(c);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            Files.delete(p);
        }
}
