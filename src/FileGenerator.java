import java.io.*;
import java.util.*;

/**
 * Generate a test data file. The size is a multiple of 4096 bytes.
 * Records each contain two 32-bit ints, corresponding to a key and
 * a data value.
 * Depending on the options, you can generate two types of output.
 * With option "-a", the output will be set so that when interpreted as
 * ASCII characters, it will look like a series of:
 * [space][space][space][letter][space][space][space][space].
 * With option "-b", the records are ints, with each int (key and data value)
 * a positive integer between 1 and 2,000,000,000.
 *
 * @author CS3114/5040 Staff
 * @version Spring 2026
 */
public class FileGenerator {
    // Number of records/block
    // Records contain two 32-bit ints (8 bytes total) and blocks are 4096 bytes
    private static final int NUM_RECS = 512;

    /** Initialize the random variable */
    static private Random value = new Random(); // Hold the Random class

    /**
     * This function generates a random number.
     *
     * @param n
     *            the ceiling
     * @return a random number
     */
    public int random(int n) {
        return Math.abs(value.nextInt()) % n;
    }


    /**
     * This method generates a file.
     *
     * @param filename
     *            The name of the file to create
     * @param numBlocks
     *            The number of (2048-byte) blocks to make the file
     * @param format
     *            Should be either "a" or "b"
     * @throws IOException
     */
    public void generateFile(String filename, int numBlocks, String format)
            throws IOException {
        int keyval;
        int dataval;
        try (DataOutputStream file =
            new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(filename)))) {
            if (format.equals("a")) { // Write out ASCII-readable values
                for (int i = 0; i < numBlocks; i++) {
                    for (int j = 0; j < NUM_RECS; j++) {
                        // Key: 3 spaces and a random capital letter
                        file.writeInt(0x20202041 + random(26));
                        // Data: 4 spaces
                        file.writeInt(0x20202020);
                    }
                }
            }
            if (format.equals("b")) { // Write out random key values
                for (int i = 0; i < numBlocks; i++) {
                    for (int j = 0; j < NUM_RECS; j++) {
                        // Write a random int for the key,
                        // and the record number for the data
                        dataval = random(2000000000) + 1;
                        keyval = random(2000000000) + 1;
                        file.writeInt(keyval);
                        file.writeInt(dataval);
                    }
                }
            }
            file.flush();
            file.close();
        }
    }
}
