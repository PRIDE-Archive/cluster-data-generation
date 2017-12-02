package uk.ac.ebi.pride.cluster.tools.importers;

import com.compomics.pridesearchparameterextractor.cmd.PrideSearchparameterExtractor;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.ArchiveExporter;
import uk.ac.ebi.pride.cluster.archive.importer.filters.SpectrumPredicateParser;
import uk.ac.ebi.pride.cluster.tools.ICommandTool;
import uk.ac.ebi.pride.cluster.tools.exceptions.ClusterDataImporterException;
import uk.ac.ebi.pride.cluster.tools.parameters.ArchiveExtractParameterTool;
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
public class ArchiveSpectraImportTool implements ICommandTool {


    private static final Logger LOGGER = Logger.getLogger(ArchiveSpectraImportTool.class);

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
        ArchiveSpectraImportTool tool = new ArchiveSpectraImportTool();
        Options options = tool.initOptions();
        try {
            tool.runCommand(options, args);
        } catch (ClusterDataImporterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the list of options for the commandline tool
     * @return Options
     */
    @Override
    public Options initOptions(){
        Options options = new Options();
        options.addOption("o", "output-folder", true, "Output path where all projects will be exported");
        options.addOption("c", "config", true, "Config file to filter the spectra from the project");
        options.addOption("s", "split-assay", false, "Split the output into Project Folders, <PXD00XXXXX>/PXD-AssayID");
        options.addOption("i", "input-folder", true, "Input folder that contains the original files in PRIDE Path");
        return options;
    }

    @Override
    public void runCommand(Options options, String[] args) throws ClusterDataImporterException {
        File outputDirectory;
        IPredicate<ISpectrum> predicate;
        Boolean splitOutput = false;

        CommandLineParser parser = new DefaultParser();
        try{
            CommandLine cmd = parser.parse( options, args);
            if(cmd.hasOption("o") && cmd.hasOption("c") && cmd.hasOption("i")){
                String outputPathName = cmd.getOptionValue("o");
                outputDirectory = new File(outputPathName);
                LOGGER.info("Analyzing the Project -- " + cmd.getOptionValue("i") + "Output to -- " + outputDirectory.getAbsolutePath());

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
                LOGGER.info("Project -- " + inputFolder + " Exported -- " + dir);

            }else{
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "ant", options );
            }
        }catch (ParseException | IOException e){
            throw new ClusterDataImporterException(e.getMessage(), e);
        }
    }
}
