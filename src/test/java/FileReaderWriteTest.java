import org.example.util.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileReaderWriteTest {
    @Test
    public void test() {
        //FileUtils.writeToFile("cached_cloud.txt", "Hello, World!\nHello, World!\nHello, World!");
        assertTrue(FileUtils.hasEmptyLines("cached_cloud.txt"));
    }
}
