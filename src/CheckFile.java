import java.io.*;

/**
 * CheckFile: Various ways to see if a file is sorted.
 * This assumes that each record is
 * a pair of 32-bit ints with the first int being the key value.
 *
 * @author CS3114 Instructor and TAs
 * @version Spring 2026
 */

public class CheckFile
{

    private static final int AVALUE = 0x20202041;
    private static final int ZVALUE = 0x20202041 + 25;
    private static final int SPACEVALUE = 0x20202020;
    private static final int BLOCKSIZE = 4096;
    /**
     * This is an empty constructor for a CheckFile object.
     */
    public CheckFile()
    {
        // empty constructor
    }


    /**
     * This method checks a file to see if it is properly sorted by key value.
     * It does nothing to confirm that the data fields have not been corrupted.
     *
     * @param filename
     *            a string containing the name of the file to check
     * @return true if the file is sorted, false otherwise
     * @param blocks Number of blocks required in file
     * @throws Exception
     *             either an IOException or a FileNotFoundException
     */
    public boolean checkFile(String filename, int blocks)
        throws Exception
    {
        File tempfile = new File(filename);
        if (tempfile.length() != (blocks * BLOCKSIZE)) {
            System.out.println("Bad file size");
            return false;
        }
        boolean isError = false;
        try (DataInputStream in =
            new DataInputStream(new BufferedInputStream(new FileInputStream(
                filename)))) {

            // Prime with the first record
            int key2 = in.readInt();
            int data = in.readInt();
            if ((key2 <= 0) || (data <= 0)) {
                isError = true;
            }
            int reccnt = 1;
            int totalrecs = blocks * BLOCKSIZE / 8;
            while ((reccnt < totalrecs) && (isError == false)) {
                int key1 = key2;
                key2 = in.readInt();
                data = in.readInt();
                reccnt++;
                if ((key2 <= 0) || (data <= 0)) {
                    System.out.println("Negative value in record");
                    isError = true;
                }
                if (key1 > key2)
                {
                    System.out.println("Records out of order");
                    isError = true;
                }
            }
            in.close();
        }
        return !isError;
    }

    /**
     * This method checks a file to see if it is properly sorted by key value.
     * This version does additional checks on the record values to verify that
     * they match the "a" format. In particular, the key value has to be in the
     * range 0x20202041 to 0x20202051, and the data value has to be 0x20202020.
     *
     * @param filename
     *            a string containing the name of the file to check
     * @param blocks Number of blocks required in file
     * @return true if the file is sorted and apparantly uncorrupted,
     *         false otherwise
     * @throws Exception
     *             either an IOException or a FileNotFoundException
     */
    public boolean checkFileA(String filename, int blocks)
        throws Exception
    {
        File tempfile = new File(filename);
        if (tempfile.length() != (blocks * BLOCKSIZE)) {
            System.out.println("Bad file size");
            return false;
        }
        boolean isError = false;
        try (DataInputStream in =
            new DataInputStream(new BufferedInputStream(new FileInputStream(
                filename)))) {
            // Prime with the first record
            int key2 = in.readInt();
            int data = in.readInt();
            if ((key2 <= 0) || (data <= 0)) {
                isError = true;
            }
            if ((key2 < AVALUE) || (key2 > ZVALUE)) {
                isError = true;
            }
            if (data != SPACEVALUE) {
                isError = true;
            }
            int reccnt = 1;
            int totalrecs = blocks * BLOCKSIZE / 8;
            while ((reccnt < totalrecs) && (isError == false)) {
                int key1 = key2;
                key2 = in.readInt();
                data = in.readInt();
                reccnt++;
                if ((key2 <= 0) || (data <= 0)) {
                    isError = true;
                }
                if (key1 > key2)
                {
                    isError = true;
                }
                if ((key2 < AVALUE) || (key2 > ZVALUE)) {
                    isError = true;
                }
                if (data != SPACEVALUE) {
                    isError = true;
                }
            }
            in.close();
        }
        return !isError;
    }
}
