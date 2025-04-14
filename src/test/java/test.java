import jakarta.servlet.http.Cookie;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author nsh
 * @data 2025/4/10 20:28
 * @description
 **/
public class test {
    public static void main(String[] args) {
        Path warPath = Path.of("warFile").toAbsolutePath().normalize();
        if(!Files.isRegularFile(warPath)) {
            System.out.println(warPath + " is not a war file");
        }
        System.out.println(warPath);
    }
}
