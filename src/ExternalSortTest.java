import student.TestCase;

/**
 * This class was designed to test the External Sort class.
 * Each tests generates random ascii and binary files of the specified size,
 * then sorts both and then checking each one with the file checker.
 *
 * @author CS3114/5040 Staff
 * @version Spring 2026
 */
public class ExternalSortTest extends TestCase
{
    private CheckFile fileChecker;

    /**
     * This method sets up the tests that follow.
     */
    public void setUp()
    {
        fileChecker = new CheckFile();
    }


    // ----------------------------------------------------------
    /**
     * Helper method for the tests: Run a test suite for a given size.
     * Creates two files (one "ascii" and one "binary") of the specified size,
     * then for each one, runs the sort and runs the checker.
     * @param fileSize Number of (4096 byte) blocks to test for
     * @throws Exception
     */
    public void sortHelper(int fileSize)
        throws Exception
    {

        FileGenerator it = new FileGenerator();
        String namea = "input" + fileSize + "asave.bin";
        String nameb = "input" + fileSize + "bsave.bin";
        it.generateFile(namea, fileSize, "a");
        it.generateFile(nameb, fileSize, "b");
        String[] args = new String[1];

        String testFilea = "testa" + fileSize + ".bin";
        args[0] = testFilea;
        SortUtils.copyFile(namea, testFilea);
        System.out.println("Sorting " + testFilea);
        ExternalSortProj.main(args);
        assertTrue(fileChecker.checkFileA(testFilea, fileSize));

        String testFileb = "testb" + fileSize + ".bin";
        args[0] = testFileb;
        SortUtils.copyFile(nameb, testFileb);
        System.out.println("Sorting " + testFileb);
        ExternalSortProj.main(args);
        assertTrue(fileChecker.checkFile(testFileb, fileSize));
    }


    // ----------------------------------------------------------
    /**
     * Test a file with 1 block
     * @throws Exception
     */
    public void test1()
        throws Exception
    {
        sortHelper(1);
    }
 // ----------------------------------------------------------
    /**
     * Test a file with 12 blocks.
     * This perfectly fills our 12-block (49,152 byte) Heapsort memory pool.
     * It should generate exactly 1 run and bypass the Multiway Merge.
     * @throws Exception
     */
    public void test12()
        throws Exception
    {
        sortHelper(12);
    }

    // ----------------------------------------------------------
    /**
     * Test a file with 13 blocks.
     * This exceeds our 12-block limit. It will force the program to 
     * generate 2 runs and trigger the Phase 2 Multiway Merge logic.
     * @throws Exception
     */
    public void test13()
        throws Exception
    {
        sortHelper(13);
    }

    // ----------------------------------------------------------
    /**
     * Test a file with 30 blocks.
     * A much larger file to stress-test the custom Min-Heap and 
     * buffer refilling logic during the Multiway Merge.
     * @throws Exception
     */
    public void test30()
        throws Exception
    {
        sortHelper(30);
    }
}
