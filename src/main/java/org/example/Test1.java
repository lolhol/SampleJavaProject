package org.example;

import org.example.util.FileUtils;

public class Test1 {
    public static void main(String[] args) {
        FileUtils.writeToFile("test.txt", "Hello, World! \n Hello, World! \n Hello, World!");
        FileUtils.hasEmptyLines("test.txt");
    }
}
