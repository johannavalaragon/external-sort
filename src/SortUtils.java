import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


// -------------------------------------------------------------------------
/**
 *  Some utility methods to help with sorting and testing the results.
 *
 *  @author CS3114/5040 Staff
 *  @version Spring 2026
 */
public class SortUtils
{


/**
     * Copy a file
     * @param sourcePath source path
     * @param destinationPath dest path
     * @throws IOException
     */
    public static void copyFile(String sourcePath, String destinationPath)
        throws IOException {
        Files.copy(Paths.get(sourcePath),
            new FileOutputStream(destinationPath));
    }
}
