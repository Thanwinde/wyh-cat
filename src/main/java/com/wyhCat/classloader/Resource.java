package com.wyhCat.classloader;

import java.nio.file.Path;

/**
 * @author nsh
 * @data 2025/4/12 20:15
 * @description
 **/
public final class Resource {
    final Path path;
    final String name;

    public Resource(Path path, String name) {
        this.path = path;
        this.name = name;
    }
}
