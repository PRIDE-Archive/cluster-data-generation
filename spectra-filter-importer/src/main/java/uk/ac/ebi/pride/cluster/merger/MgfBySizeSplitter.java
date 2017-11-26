package uk.ac.ebi.pride.cluster.merger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MgfBySizeSplitter implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(MgfBySizeSplitter.class);

    // the id of the splitter, this will be used as prefix to the output mgf file
    private final String id;

    // a list of queue mgf files to be processed
    private final Set<File> queuedFiles;

    // maximum size of each output file
    private final long maxFileSize;

    // output file folder
    private final File outputFolder;

    // count down for the thread pool, this can be null
    private final CountDownLatch countDown;

    // the size of data being written to an existing file
    private long writtenFileSize = 0;

    // the count of the output file
    private int fileCount = 0;

    // print writer for output
    private PrintWriter writer = null;


    public MgfBySizeSplitter(String id, Set<File> queuedFiles, long maxFileSize, File outputFolder) {
        this(id, queuedFiles, maxFileSize, outputFolder,null);
    }

    public MgfBySizeSplitter(String id,
                             Set<File> queuedFiles,
                             long maxFileSize,
                             File outputFolder,
                             CountDownLatch countDown) {
        this.id = id;
        this.queuedFiles = queuedFiles;
        this.maxFileSize = maxFileSize;
        this.outputFolder = outputFolder;
        this.countDown = countDown;
    }

    @Override
    public void run() {
        try {
            for (File queuedFile : queuedFiles) {
                logger.info("Merging MGF file {}", queuedFile.getAbsolutePath());
                readSpectraFromMgfFile(queuedFile);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }

            if (countDown != null) {
                countDown.countDown();
            }
        }
    }


    private void readSpectraFromMgfFile(File mgfFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mgfFile));
            if (writer == null) {
                writer = createNewFileWriter();
            }

            String batchOfSpectraReader;
            while (!(batchOfSpectraReader = readBatchOfSpectra(reader, 100)).equals("")) {
                int byteSize = batchOfSpectraReader.getBytes().length;
                writer.append(batchOfSpectraReader);
                writtenFileSize += byteSize;
                if (writtenFileSize > maxFileSize) {
                    writer.close();
                    writtenFileSize = 0;
                    writer = createNewFileWriter();
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error while splitting mgf files", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private String readBatchOfSpectra(BufferedReader reader, int numberOfSpectraToRead) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        int cnt = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
            if (line.equalsIgnoreCase("END IONS")) {
                cnt++;
                if (cnt == numberOfSpectraToRead) {
                    break;
                }
            }
        }

        return stringBuilder.toString();
    }

    private PrintWriter createNewFileWriter() throws IOException {
        fileCount++;
        return new PrintWriter(new BufferedWriter(new FileWriter(new File(outputFolder, id + "-" + fileCount + ".mgf"))));
    }
}
