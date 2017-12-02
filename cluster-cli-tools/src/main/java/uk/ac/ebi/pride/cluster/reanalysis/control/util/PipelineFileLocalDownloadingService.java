package uk.ac.ebi.pride.cluster.reanalysis.control.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class PipelineFileLocalDownloadingService {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(PipelineFileLocalDownloadingService.class);

    /**
     * Downloads an entire folder locally
     *
     * @param pathToFolder the path (http, ftp or file based) to the folder
     * @param destFolder the local folder
     * @return the filled destination folder
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File downloadFolder(String pathToFolder, File destFolder) throws IOException, URISyntaxException {
        destFolder.mkdirs();
        URL folderURL = new URL(getCorrectFilePath(pathToFolder));
        File localFolder = downloadFolderFromLocalNetwork(folderURL, destFolder);
        return localFolder;
    }

    /**
     * Downloads a file locally
     *
     * @param destFolder the local folder
     * @return the downloaded file
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File downloadFile(String pathToFile, File destFolder) throws IOException {
        destFolder.mkdirs();
        URL fileURL = new URL(getCorrectFilePath(pathToFile));
        String fileName = fileURL.getFile().substring(fileURL.getFile().lastIndexOf("/"));
        File destFile = new File(destFolder, fileName);
        System.out.println("Saving to "+destFile.getAbsolutePath());
        copy(fileURL, destFile);
        return destFile;
    }

    /**
     * Downloads a file locally
     *
     * @param destFolder the local folder
     * @param newFileName a new name for the downloaded file
     * @return the downloaded file
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File downloadFile(String pathToFile, File destFolder, String newFileName) throws IOException {
        destFolder.mkdirs();
        URL fileURL = new URL(getCorrectFilePath(pathToFile));
        File destFile = new File(destFolder, newFileName);
        copy(fileURL, destFile);
        return destFile;
    }

    private static File downloadFolderFromLocalNetwork(URL folderURL, File destFolder) throws IOException, URISyntaxException {
        destFolder = new File(destFolder, folderURL.getFile().substring(folderURL.getFile().lastIndexOf("/")));
        destFolder.mkdirs();
        File networkDir = new File(folderURL.toURI());
        FileUtils.copyDirectory(networkDir, destFolder);
        return destFolder;
    }

    private static String getCorrectFilePath(String filePath) {
        if (!filePath.startsWith("ftp://") & !filePath.startsWith("http://")) {
            filePath = "file:///" + filePath;
        }
        return filePath;
    }

    private static void copy(URL fileURL, File destFile) throws IOException {
        try (OutputStream os = new FileOutputStream(destFile);
                InputStream is = fileURL.openStream()) {
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            os.flush();
        }
    }

}
