package uk.ac.ebi.pride.cluster.tools;

import org.apache.commons.cli.*;
import uk.ac.ebi.pride.cluster.ArchiveExporter;
import uk.ac.ebi.pride.cluster.archive.importer.filters.SpectrumPredicateParser;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.function.Functions;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;
import uk.ac.ebi.pride.spectracluster.util.function.spectrum.RemoveSpectrumEmptyPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.predicate.IPredicate;

import java.io.*;


/**
 * Export spectra from PRIDE Archive to MGF files. This Script take a folder to tools the data.
 *
 * @author Yasset Perez-Riverol
 */
public class ArchiveSpectraImportTool {


    /**
     * usage outputDirectory filterFile directoryToProcess
     *
     * @param args
     */
    /*
        <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
        <properties>
            <comment>Properties can be used to define the Predicates and Functions</comment>
            <entry key="identified.spectrum">true</entry>
            <entry key="minimum.number.of.peaks">100</entry>
            <entry key="with.precursors">true</entry>
        </properties>
     */
    public static void main(String[] args) throws IOException, ParseException {

        File outputDirectory;
        IPredicate<ISpectrum> predicate;
        Boolean splitOutput = false;

        Options options = initOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        if(cmd.hasOption("o") && cmd.hasOption("c") && cmd.hasOption("i")){
            String outputPathName = cmd.getOptionValue("o");
            outputDirectory = new File(outputPathName);
            System.out.println("Output to: " + outputDirectory.getAbsolutePath());

            String filterFileName = cmd.getOptionValue("c");
            File filtersFile = new File(filterFileName);
            predicate = SpectrumPredicateParser.parse(filtersFile);

            if(cmd.hasOption("s")){
                splitOutput = true;
            }

            RemoveSpectrumEmptyPeakFunction removeEmptyPeakFunction = new RemoveSpectrumEmptyPeakFunction();
            IFunction<ISpectrum, ISpectrum> condition = Functions.condition(removeEmptyPeakFunction, predicate);

            String inputFolder = cmd.getOptionValue("i");
            File dir = new File(inputFolder);
            ArchiveExporter exp = new ArchiveExporter(condition);
            exp.export(dir, outputDirectory, splitOutput);
            System.out.println("exported " + dir);

        }else{
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "ant", options );
        }


    }

    /**
     * Return the list of options for the commandline tool
     * @return Options
     */

    public static Options initOptions(){
        Options options = new Options();
        options.addOption("o", "output-folder", true, "Output path where all projects will be exported");
        options.addOption("c", "config", true, "Config file to filter the spectra from the project");
        options.addOption("s", "split-assay", false, "Split the output into Project Folders, <PXD00XXXXX>/PXD-AssayID");
        options.addOption("i", "input-folder", true, "Input folder that contains the original files in PRIDE Path");
        return options;
    }
}
