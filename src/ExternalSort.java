import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

// The External Sort implementation
// -------------------------------------------------------------------------
/**
 * Main class for the External Sort project. Orchestrates the 50,000-byte memory
 * pool to perform a two-phase external sort.
 * 
 * @author Johanna
 * @version Spring 2026
 */
public class ExternalSort
{

    /**
     * The working memory available to the program: 50,000 bytes
     */
    private static final int MEMBYTES = 50000;

    /**
     * The size of a single disk block in bytes.
     */
    private static final int BLOCK_SIZE = 4096;

    /**
     * Number of blocks we can fit in memory for the Heapsort phase. 12 blocks
     * 4096 bytes = 49152 bytes.
     */
    private static final int BLOCKS_PER_RUN = 12;

    /**
     * The number of 8-byte records in a single run. (49,152 bytes / 8 bytes per
     * record = 6144 records)
     */
    private static final int RECORDS_PER_RUN =
        (BLOCKS_PER_RUN * BLOCK_SIZE) / 8;

    /**
     * Create a new ExternalSort object.
     * 
     * @param theFileName
     *            The name of the file to be sorted
     * @throws IOException
     *             Error in accessing the files
     */
    public static void sort(String theFileName)
        throws IOException
    {
        RandomAccessFile dataFile = new RandomAccessFile(theFileName, "rw");
        RandomAccessFile tempFile = new RandomAccessFile("run_temp.bin", "rw");

        // Allocate 50,000 bytes of working memory
        byte[] workingMem = new byte[MEMBYTES];

        // Generate runs and write them to the temp file
        int numRuns = generateRuns(dataFile, tempFile, workingMem);

        // Merge the runs back into the original data file
        if (numRuns == 1)
        {
            // If the whole file fit into one run, just copy temp back to data
            copyFileContents(tempFile, dataFile, workingMem);
        }
        else if (numRuns > 1)
        {
            // After multiple runs, performs the multiway merge
            mergeRuns(dataFile, tempFile, workingMem, numRuns);
        }

        dataFile.close();
        tempFile.close();

        // Delete the temporary file to prevent pollution between
        // tests
        File f = new File("run_temp.bin");
        if (f.exists())
        {
            f.delete();
        }
    }


    /**
     * Reads blocks from the input file into the memory pool, performs an
     * in-place Heapsort, and writes the sorted run to the temporary file.
     * 
     * @param dataFile
     *            the original input file containing unsorted data
     * @param tempFile
     *            the temporary file to store the sorted runs
     * @param pool
     *            the 50,000-byte working memory array
     * @return the total number of runs generated
     * @throws IOException
     *             if file I/O fails
     */
    private static int generateRuns(
        RandomAccessFile dataFile,
        RandomAccessFile tempFile,
        byte[] pool)
        throws IOException
    {
        int runCount = 0;
        long totalLength = dataFile.length();
        long currentPos = 0;

        dataFile.seek(0);
        tempFile.setLength(0); // Safe to truncate own temp file
        tempFile.seek(0);

        while (currentPos < totalLength)
        {
            // Determine how many bytes to read
            int bytesToRead = (int)Math
                .min(BLOCKS_PER_RUN * BLOCK_SIZE, totalLength - currentPos);

            // Read directly into the byte array
            dataFile.readFully(pool, 0, bytesToRead);
            currentPos += bytesToRead;

            int numRecords = bytesToRead / 8;

            // Sort the records in place within the byte array
            heapSort(pool, numRecords);

            // Write the sorted run directly to the temporary file
            tempFile.write(pool, 0, bytesToRead);
            runCount++;
        }

        return runCount;
    }


    /**
     * Merges the sorted runs from the temporary file back into the original
     * data file by dividing the memory pool into K input buffers and 1 output
     * buffer. Uses a custom Min-Heap to track the smallest active record.
     * 
     * @param dataFile
     *            the original input file (will be overwritten with sorted data)
     * @param tempFile
     *            the temporary file containing the sorted runs
     * @param pool
     *            the 50,000-byte working memory array
     * @param numRuns
     *            the total number of runs to merge
     * @throws IOException
     *             if file I/O fails
     */
    private static void mergeRuns(
        RandomAccessFile dataFile,
        RandomAccessFile tempFile,
        byte[] pool,
        int numRuns)
        throws IOException
    {
        int totalBuffers = numRuns + 1;
        int rawBufferSize = MEMBYTES / totalBuffers;
        int bufferSizeBytes = (rawBufferSize / 8) * 8;

        int outBufferOffset = numRuns * bufferSizeBytes;
        int outBufferPos = 0;

        long[] fileRunPos = new long[numRuns];
        long[] fileRunRemaining = new long[numRuns];
        int[] bufferOffsets = new int[numRuns];
        int[] bufferPositions = new int[numRuns];
        int[] bufferRemaining = new int[numRuns];

        long totalTempLength = tempFile.length();

        for (int i = 0; i < numRuns; i++)
        {
            fileRunPos[i] = (long)i * (BLOCKS_PER_RUN * BLOCK_SIZE);
            fileRunRemaining[i] = Math.min(
                BLOCKS_PER_RUN * BLOCK_SIZE,
                totalTempLength - fileRunPos[i]);

            bufferOffsets[i] = i * bufferSizeBytes;

            int toRead = (int)Math.min(bufferSizeBytes, fileRunRemaining[i]);
            tempFile.seek(fileRunPos[i]);
            tempFile.readFully(pool, bufferOffsets[i], toRead);

            fileRunPos[i] += toRead;
            fileRunRemaining[i] -= toRead;
            bufferPositions[i] = 0;
            bufferRemaining[i] = toRead;
        }

        int[] heap = new int[numRuns];
        int heapSize = numRuns;
        for (int i = 0; i < numRuns; i++)
        {
            heap[i] = i;
        }

        ByteBuffer byteBuf = ByteBuffer.wrap(pool);

        for (int i = (heapSize / 2) - 1; i >= 0; i--)
        {
            siftDownMin(
                heap,
                i,
                heapSize,
                byteBuf,
                bufferOffsets,
                bufferPositions);
        }

        dataFile.seek(0);

        while (heapSize > 0)
        {
            int minRunID = heap[0];

            int srcIdx = bufferOffsets[minRunID] + bufferPositions[minRunID];
            int destIdx = outBufferOffset + outBufferPos;

            System.arraycopy(pool, srcIdx, pool, destIdx, 8);

            bufferPositions[minRunID] += 8;
            bufferRemaining[minRunID] -= 8;
            outBufferPos += 8;

            if (outBufferPos == bufferSizeBytes)
            {
                dataFile.write(pool, outBufferOffset, bufferSizeBytes);
                outBufferPos = 0;
            }

            if (bufferRemaining[minRunID] == 0)
            {
                if (fileRunRemaining[minRunID] > 0)
                {
                    int toRead = (int)Math
                        .min(bufferSizeBytes, fileRunRemaining[minRunID]);
                    tempFile.seek(fileRunPos[minRunID]);
                    tempFile.readFully(pool, bufferOffsets[minRunID], toRead);

                    fileRunPos[minRunID] += toRead;
                    fileRunRemaining[minRunID] -= toRead;
                    bufferPositions[minRunID] = 0;
                    bufferRemaining[minRunID] = toRead;

                    siftDownMin(
                        heap,
                        0,
                        heapSize,
                        byteBuf,
                        bufferOffsets,
                        bufferPositions);
                }
                else
                {
                    heap[0] = heap[heapSize - 1];
                    heapSize--;
                    if (heapSize > 0)
                    {
                        siftDownMin(
                            heap,
                            0,
                            heapSize,
                            byteBuf,
                            bufferOffsets,
                            bufferPositions);
                    }
                }
            }
            else
            {
                siftDownMin(
                    heap,
                    0,
                    heapSize,
                    byteBuf,
                    bufferOffsets,
                    bufferPositions);
            }
        }

        if (outBufferPos > 0)
        {
            dataFile.write(pool, outBufferOffset, outBufferPos);
        }
    }


    /**
     * Helper to copy the temporary file back to the main file when there is
     * only 1 run.
     * 
     * @param source
     *            the file to copy from
     * @param dest
     *            the file to copy to
     * @param pool
     *            the memory pool used as a transfer buffer
     * @throws IOException
     *             if file I/O fails
     */
    private static void copyFileContents(
        RandomAccessFile source,
        RandomAccessFile dest,
        byte[] pool)
        throws IOException
    {
        source.seek(0);
        dest.seek(0);

        long remaining = source.length();
        while (remaining > 0)
        {
            int toRead = (int)Math.min(MEMBYTES, remaining);
            source.readFully(pool, 0, toRead);
            dest.write(pool, 0, toRead);
            remaining -= toRead; // Fixed: toRead instead of to_read
        }
    }


    /**
     * Performs an in-place max-heapsort on the records stored in the byte
     * array. By building a max-heap and repeatedly moving the max element to
     * the end, the array becomes sorted in ascending order.
     * 
     * @param pool
     *            the byte array containing the raw data
     * @param numRecords
     *            the total number of 8-byte records currently loaded in the
     *            pool
     */
    private static void heapSort(byte[] pool, int numRecords)
    {
        ByteBuffer byteBuf = ByteBuffer.wrap(pool);
        IntBuffer intBuf = byteBuf.asIntBuffer();

        // Build the initial Max-Heap
        for (int i = (numRecords / 2) - 1; i >= 0; i--)
        {
            siftDown(intBuf, i, numRecords);
        }

        // Extract Max and place at the end of the unsorted partition
        for (int i = numRecords - 1; i > 0; i--)
        {
            swapRecords(intBuf, 0, i);
            siftDown(intBuf, 0, i);
        }
    }


    /**
     * Sifts a record down to its proper place in the max-heap to maintain the
     * heap property.
     * 
     * @param intBuf
     *            the buffer view of the memory pool
     * @param recordIndex
     *            the conceptual index of the 8-byte record to sift down
     * @param heapSizeRecords
     *            the current number of records in the active heap partition
     */
    private static
        void
        siftDown(IntBuffer intBuf, int recordIndex, int heapSizeRecords)
    {
        while (!isLeaf(recordIndex, heapSizeRecords))
        {
            int leftChildRecordIndex = (2 * recordIndex) + 1;
            int rightChildRecordIndex = (2 * recordIndex) + 2;
            int largerChildRecordIndex = leftChildRecordIndex;

            // Check if right child exists and is greater than the left child
            if (rightChildRecordIndex < heapSizeRecords)
            {
                int leftKey = intBuf.get(leftChildRecordIndex * 2);
                int rightKey = intBuf.get(rightChildRecordIndex * 2);

                if (rightKey > leftKey)
                {
                    largerChildRecordIndex = rightChildRecordIndex;
                }
            }

            int currentKey = intBuf.get(recordIndex * 2);
            int largerChildKey = intBuf.get(largerChildRecordIndex * 2);

            // If the current node is already greater than its largest child, we
            // are done
            if (currentKey >= largerChildKey)
            {
                return;
            }

            swapRecords(intBuf, recordIndex, largerChildRecordIndex);
            recordIndex = largerChildRecordIndex; // Continue sifting down
        }
    }


    /**
     * Swaps two full 8-byte records (both the key and the value) within the
     * buffer.
     * 
     * @param intBuf
     *            the buffer view of the memory pool
     * @param recordIndexA
     *            the conceptual index of the first record
     * @param recordIndexB
     *            the conceptual index of the second record
     */
    private static
        void
        swapRecords(IntBuffer intBuf, int recordIndexA, int recordIndexB)
    {
        int keyIndexA = recordIndexA * 2;
        int valIndexA = keyIndexA + 1;

        int keyIndexB = recordIndexB * 2;
        int valIndexB = keyIndexB + 1;

        // Temporarily store Record A
        int tempKey = intBuf.get(keyIndexA);
        int tempVal = intBuf.get(valIndexA);

        // Move Record B into Record A's spot
        intBuf.put(keyIndexA, intBuf.get(keyIndexB));
        intBuf.put(valIndexA, intBuf.get(valIndexB));

        // Move stored Record A into Record B's spot
        intBuf.put(keyIndexB, tempKey);
        intBuf.put(valIndexB, tempVal);
    }


    /**
     * Determines if a given record index is a leaf node in the heap.
     * 
     * @param recordIndex
     *            the conceptual index of the record
     * @param heapSizeRecords
     *            the current number of records in the heap
     * @return true if the node has no children, false otherwise
     */
    private static boolean isLeaf(int recordIndex, int heapSizeRecords)
    {
        return recordIndex >= (heapSizeRecords / 2)
            && recordIndex < heapSizeRecords;
    }


    /**
     * Helper to maintain the Min-Heap property for the multiway merge. Compares
     * the actual key values residing in the memory pool buffers to sort the Run
     * IDs.
     * 
     * @param heap
     *            the array acting as the Min-Heap, storing Run IDs
     * @param index
     *            the current heap node index to sift down
     * @param heapSize
     *            the current number of active runs in the heap
     * @param byteBuf
     *            a ByteBuffer wrapping the memory pool for easy int extraction
     * @param bufferOffsets
     *            array tracking where each run's buffer starts in the pool
     * @param bufferPositions
     *            array tracking the current read index within each run's buffer
     */
    private static void siftDownMin(
        int[] heap,
        int index,
        int heapSize,
        ByteBuffer byteBuf,
        int[] bufferOffsets,
        int[] bufferPositions)
    {
        while (true)
        {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;

            if (leftChild < heapSize)
            {
                int leftRunID = heap[leftChild];
                int minRunID = heap[smallest];

                int leftKey = byteBuf.getInt(
                    bufferOffsets[leftRunID] + bufferPositions[leftRunID]);
                int minKey = byteBuf.getInt(
                    bufferOffsets[minRunID] + bufferPositions[minRunID]);

                if (leftKey < minKey)
                {
                    smallest = leftChild;
                }
            }

            if (rightChild < heapSize)
            {
                int rightRunID = heap[rightChild];
                int minRunID = heap[smallest];

                int rightKey = byteBuf.getInt(
                    bufferOffsets[rightRunID] + bufferPositions[rightRunID]);
                int minKey = byteBuf.getInt(
                    bufferOffsets[minRunID] + bufferPositions[minRunID]);

                if (rightKey < minKey)
                {
                    smallest = rightChild;
                }
            }

            if (smallest != index)
            {
                int temp = heap[index];
                heap[index] = heap[smallest];
                heap[smallest] = temp;

                index = smallest;
            }
            else
            {
                break;
            }
        }
    }
}
