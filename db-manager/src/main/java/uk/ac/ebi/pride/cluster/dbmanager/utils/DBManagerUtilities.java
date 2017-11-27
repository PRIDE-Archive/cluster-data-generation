package uk.ac.ebi.pride.cluster.dbmanager.utils;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.protein.Header;
import org.apache.log4j.Logger;

import javax.sound.midi.Sequence;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class helps to Download databases from specific url.
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 24/11/2017.
 */
public class DBManagerUtilities {

    private static final Logger LOGGER = Logger.getLogger(DBManagerUtilities.class);

    /**
     * Download a file from an URL, the file is bulk into an absolute file path.
     * @param url URL fo the fhe file that would be download
     * @param absolutePath absolute path where the file where be bulk.
     * @param flagInformation flagInformation is used for Logger.
     * @return Downloaded file.
     * @throws IOException
     */
    public static BufferedOutputStream downloadURL(URL url, File absolutePath, String flagInformation) throws IOException{

        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

        java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
        java.io.FileOutputStream fos = new java.io.FileOutputStream(absolutePath);
        BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
        byte[] data = new byte[1024];
        long downloadedFileSize = 0;
        LOGGER.info("Starting Downloading the Proteome -- " + flagInformation);
        int x;
        while ((x = in.read(data, 0, 1024)) >= 0) {
            downloadedFileSize += x;
            bout.write(data, 0, x);
            ProgressBarConsole.updateProgress(downloadedFileSize);
        }
        bout.close();
        in.close();

        return bout;
    }

    /**
     * Different to other methods this method take the url and download to a folder
     * the file with the same name that is provided by the user.
     * @param url URL to download
     * @param absolutePath absolute path of the directory
     * @param taxonomy taxonomy
     * @return
     * @throws IOException
     */
    public static BufferedOutputStream downloadURLToDirectory(URL url, File absolutePath, String taxonomy) throws IOException{

        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

        java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());

        String fieldValue = httpConnection.getHeaderField("Content-Disposition");

        if (fieldValue == null || ! fieldValue.contains("filename="))
            throw new IOException("The file is not provided in the header, it can't be download with the original provided name.");

        if (! absolutePath.exists() || !absolutePath.isDirectory())
            throw new IOException("The file directory path provided do not exists!!");

        // parse the file name from the header field
        String filename = fieldValue.substring(fieldValue.indexOf("filename=") + 9, fieldValue.length());

        java.io.FileOutputStream fos = new java.io.FileOutputStream(absolutePath + File.separator + filename);
        BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
        byte[] data = new byte[1024];
        long downloadedFileSize = 0;
        LOGGER.info("Starting Downloading the Proteome -- " + taxonomy);
        int x;
        while ((x = in.read(data, 0, 1024)) >= 0) {
            downloadedFileSize += x;
            bout.write(data, 0, x);
            ProgressBarConsole.updateProgress(downloadedFileSize);
        }
        bout.close();
        in.close();

        return bout;
    }

    /**
     * This utility check the quality of a Fasta File that is correct.
     * @param fastaFile fasta File
     * @return status of the fasta file.
     */
    public static Boolean checkFastaIntegrity(File fastaFile)  {

        SequenceFactory factory = SequenceFactory.getInstance();
        try{
            factory.loadFastaFile(fastaFile);
            int aminoCount = 0;
            for(String accession: factory.getAccessions()){
                aminoCount += factory.getProtein(accession).getLength();
            }
            LOGGER.info("Number of AminoAcids for Fasta File -- " + aminoCount);
        }catch (Exception e){
            LOGGER.error("Error reading the Fasta File -- " + fastaFile);
            return false;
        }

        return true;
    }


    /**
     * This method merge to list of proteins coming from a files into one unique List. We should evaluate, if we can do this
     * in file in the future and not in memory. Because here it can be time consuming.
     * @param inputFile inputFile
     * @param fileToAppend append File with new proteins (e.g contaminants)
     * @return List of proteins.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static boolean mergeToFastaFileTemp(File inputFile, File fileToAppend, File resultFile) throws IOException, ClassNotFoundException {
       final SequenceFactory factory = SequenceFactory.getInstance();
       factory.loadFastaFile(inputFile);
       Map<Header, Protein> resultProteins = factory.getAccessions().stream().map(key -> {
           try {
               return new AbstractMap.SimpleEntry<>(factory.getHeader(key),factory.getProtein(key));
           } catch (IOException e) {
               e.printStackTrace();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           return null;
       }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

       factory.loadFastaFile(fileToAppend);
       factory.getAccessions().parallelStream().forEach( appendKey -> {
           try{
           if(!resultProteins.entrySet().contains(factory.getHeader(appendKey)))
               resultProteins.put(factory.getHeader(appendKey), factory.getProtein(appendKey));
           } catch (IOException e) {
               e.printStackTrace();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       });

       if(resultProteins != null && resultProteins.size() > 0 ){
           LOGGER.info("Appending Proteins to the new File -- " + resultFile);

           FileOutputStream fos = new FileOutputStream(resultFile);
           BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
           resultProteins.forEach( (header, protein) -> {
               try {
                   bw.write(header.toString());
                   bw.newLine();
                   bw.write(protein.getSequence());
                   bw.newLine();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           });
           bw.close();
       }

       return true;
    }
}
