package org.example.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    public static boolean hasEmptyLines(String filename) {
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;
            }
            bufferedReader.close();
            System.out.println(lineCount + " lines found in file.");

            if (lineCount > 1) {
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return false;
    }

    public static void writeToFile(String filename, String content) {
        try {
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
