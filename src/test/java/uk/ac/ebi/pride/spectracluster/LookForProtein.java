package uk.ac.ebi.pride.spectracluster;

import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.*;

import java.io.*;
import java.util.*;

/**
 * Main class for running mgf filter on individual PRIDE Archive project
 * uk.ac.ebi.pride.spectracluster.ArchiveProjectSpectraFilter
 *
 * @author Rui Wang
 * @version $Id$
 */
public class LookForProtein {

    /**Writes to nowhere*/
    public static class NullOutputStream extends OutputStream {
      @Override
      public void write(int b) throws IOException {
      }
    }

    public static final String[] GOOD_PROJECTS =
            {

"PRD000314",
"PXD000054",
"PRD000439",
"PXD000558",
"PXD000460",
"PRD000089",
"PRD000172",
"PRD000075",
"PRD000481",
"PRD000019",
"PRD000127",
"PXD000445",
"PRD000491",
"PRD000108",
"PRD000321",
"PXD000653",
"PRD000468",
"PRD000167",
"PRD000407",
"PRD000421",
"PXD000318",
"PXD000160",
"PXD000035",
"PRD000083",
"PRD000364",
"PXD000157",
"PRD000377",
"PRD000511",
"PRD000464",
"PRD000087",
"PRD000681",
"PXD000161",
"PRD000231",
"PXD000008",
"PXD000045",
"PRD000644",
"PRD000214",
"PRD000748",
"PRD000693",
"PRD000503",
"PXD000196",
"PRD000050",
"PRD000833",
"PXD000306",
"PRD000434",
"PXD000201",
"PRD000105",
"PRD000780",
"PXD000043",
"PRD000498",
"PRD000426",
"PXD000433",
"PRD000523",
"PXD000070",
"PRD000259",
"PRD000479",
"PXD000307",
"PRD000472",
"PXD000657",
"PRD000476",
"PXD000126",
"PRD000631",
"PRD000480",
"PRD000493",
"PRD000250",
"PXD000072",
"PRD000216",
"PRD000272",
"PRD000485",
"PRD000146",
"PRD000423",
"PRD000349",
"PXD000142",
"PRD000174",
"PRD000032",
"PRD000500",
"PRD000475",
"PRD000229",
"PRD000204",
"PXD000090",
"PRD000482",
"PXD000252",
"PRD000257",
"PRD000227",
"PXD000218",
"PRD000047",
"PRD000168",
"PRD000451",
"PRD000487",
"PRD000069",
"PRD000194",
"PXD000756",
"PRD000744",
"PRD000634",
"PXD000316",
"PRD000072",
"PXD000217",
"PXD000510",
"PRD000073",
"PRD000247",
"PXD000021",
"PRD000473",
"PXD000417",
"PRD000557",
"PRD000808",
"PXD000355",
"PXD000536",
"PRD000774",
"PXD001099",
"PRD000420",
"PXD000385",
"PRD000141",
"PRD000733",
"PRD000419",
"PRD000508",
"PXD000878",
"PRD000289",
"PRD000008",
"PRD000054",
"PRD000277",
"PRD000737",
"PXD000743",
"PXD000429",
"PRD000448",
"PXD000211",
"PRD000636",
"PRD000336",
"PRD000410",
"PXD000651",
"PXD000345",
"PRD000193",
"PRD000721",
"PRD000769",
"PRD000569",
"PRD000814",
"PRD000118",
"PRD000546",
"PRD000478",
"PRD000218",
"PXD000637",
"PRD000616",
"PXD000387",
"PXD000076",
"PRD000062",
"PRD000246",
"PRD000524",
"PRD000711",
"PXD000411",
"PXD000594",
"PRD000045",
"PRD000422",
"PRD000544",
"PRD000822",
"PRD000679",
"PRD000680",
"PRD000629",
"PXD000283",
"PRD000100",
"PRD000484",
"PRD000438",
"PXD000276",
"PXD000688",
"PXD000314",
"PRD000736",
"PRD000441",
"PXD000247",
"PRD000726",
"PRD000228",
"PRD000664",
"PRD000280",
"PXD000533",
"PRD000093",
"PRD000499",
"PRD000509",
"PRD000057",
"PXD000383",
"PXD000781",
"PRD000268",
"PXD000016",
"PRD000220",
"PXD000769",
"PRD000545",
"PXD000581",
"PXD000280",
"PXD000025",
"PRD000425",
"PXD000235",
"PRD000608",
"PXD000004",
"PRD000182",
"PRD000507",
"PRD000378",
"PRD000097",
"PXD000223",
"PRD000401",
"PRD000416",
"PRD000258",
"PRD000068",
"PRD000465",
"PRD000730",
"PRD000191",
"PRD000346",
"PXD000226",
"PXD000623",
"PXD000457",
"PRD000619",
"PRD000397",
"PRD000059",
"PRD000450",
"PXD000210",
"PXD000265",
"PXD000176",
"PXD000768",
"PXD000475",
"PXD000864",
"PXD000435",
"PRD000029",
"PRD000092",
"PXD000213",
"PRD000462",
"PRD000429",
"PRD000242",
"PRD000668",
"PRD000510",
"PXD000631",
"PRD000517",
"PRD000327",
"PRD000494",
"PXD000403",
"PRD000562",
"PRD000126",
"PRD000623",
"PRD000518",
"PRD000211",
"PRD000506",
"PRD000293",
"PXD000560",
"PRD000395",
"PXD000335",
"PRD000436",
"PXD000384",
"PXD000652",
"PRD000084",
"PRD000787",
"PRD000001",
"PRD000052",
"PXD000162",
"PRD000635",
"PRD000366",
"PRD000514",
"PRD000332",
"PRD000354",
"PXD000505",
"PRD000237",
"PXD000187",
"PXD000041",
"PRD000717",
"PRD000341",
"PRD000463",
"PXD000263",
"PRD000453",
"PRD000704",
"PXD000488",
"PRD000495",
"PRD000347",
"PXD000529",
"PXD000746",
"PXD000026",
"PXD000127",
"PRD000221",
"PXD000023",
"PXD000472",
"PXD000881",
"PXD000557",
"PRD000241",
"PRD000551",
"PRD000139",
"PRD000605",
"PRD000698",
"PXD000858",
"PRD000340",
"PXD000694",
"PXD000053",
"PRD000732",
"PXD000454",
"PRD000308",
"PRD000398",
"PRD000666",
"PRD000226",
"PRD000647",
"PXD000654",
"PRD000096",
"PXD000638",
"PXD000412",
"PXD000534",
"PRD000181",
"PXD000629",
"PXD000073",
"PRD000504",
"PXD000012",
"PXD000222",
"PRD000013",
"PXD000500",
"PXD000817",
"PRD000369",
"PRD000417",
"PRD000615",
"PRD000582",
"PRD000411",
"PRD000362",
"PRD000363",
"PRD000066",
"PRD000444",
"PXD000354",
"PXD000464",
"PRD000392",
"PRD000360",
"PRD000502",
"PXD000185",
"PRD000291",
"PXD000264",
"PXD000722",
"PXD000001",
"PXD000083",
"PRD000234",
"PRD000512",
"PRD000123",
"PRD000466",
"PXD000535",
"PRD000684",
"PRD000600",
"PRD000376",
"PXD000461",
"PXD000027",
"PRD000591",
"PRD000796",
"PXD000047",
"PRD000818",
"PRD000232",
"PRD000483",
"PXD000216",
"PRD000263",
"PRD000384",
"PRD000492",
"PXD000002",
"PRD000738",
"PRD000806",
"PRD000532",
"PRD000112",
"PXD000312",
"PRD000458",
"PRD000011",
"PRD000387",
"PRD000303",
"PRD000334",
"PRD000359",
"PRD000307",
"PRD000753",
"PRD000469",
"PXD000050",
"PRD000251",
"PRD000460",
"PRD000224",
"PXD000680",
"PRD000470",
"PRD000825",
"PRD000394",
"PRD000727",
"PRD000178",
"PRD000581",
"PRD000565",
"PRD000488",
"PRD000015",
"PRD000729",
"PRD000319",
"PRD000103",
"PXD000378",
"PRD000459",
"PRD000592",
"PRD000435",
"PXD000948",
"PRD000262",
"PRD000650",
"PRD000477",
"PRD000044",
"PRD000522",
"PXD000152",
"PXD000129",
"PXD000197",
"PRD000505",
"PRD000365",
"PRD000659",
"PRD000461",
"PRD000490",
"PRD000142",
"PXD000580",
"PRD000467",
"PRD000122",
"PRD000154",
"PXD000455",
"PRD000012",
"PRD000430",
"PRD000449",
"PRD000192",
"PXD001020",
"PXD000095",
"PXD000190",
"PXD000440",
"PXD000069",
"PRD000486",
"PXD000363",
"PRD000233",
"PXD000015",
"PRD000722",
"PXD000124",
"PRD000143",
"PXD000094",
"PRD000053",
"PRD000345",
"PRD000607",
"PRD000520",
"PXD000504",
"PRD000176",
"PRD000590",
"PRD000414",
"PXD000655",
"PXD000532",
"PRD000288",
"PRD000067",
"PRD000271",
"PRD000471",
"PRD000489",
"PRD000065",
"PRD000677",
"PRD000151",
"PRD000281",
"PRD000156",
"PRD000351",
"PRD000180",
"PRD000731",
"PXD000656",
"PRD000276",
"PRD000055",
"PXD000013",
"PRD000655",
"PXD000603",
"PXD000579",
"PRD000165",
"PRD000593",
"PRD000541",
"PXD000119",
"PRD000443",
// "PRD000474",   might be really big
"PXD000905",
"PXD000107",
"PXD000624",
"PRD000150",
"PRD000676",
"PXD000375",
"PRD000269",
"PRD000497",
"PRD000418",
"PRD000046",
"PRD000285",
"PRD000665",
"PRD000020",
"PXD000426",
"PXD000189",
"PRD000124",
"PRD000496",
"PXD000509",
"PRD000594",
"PRD000554",
"PRD000437",
"PXD000625",
"PRD000513",
"PRD000651",
"PRD000400",
"PRD000270",
"PRD000531",

            };

    public static final Set<String> GOOD_SET = new HashSet<String>(Arrays.asList(GOOD_PROJECTS));

    public static final String BAD_PROTEIN = "Uniprot_Z_mays_20091203.fasta:tr|Q8W233|Q8W233_MAIZE Putative non-LTR retroelement reverse transcriptase OS=Z";
    public static final String BAD_ACCESSION= "Uniprot_Z_mays_20091203";

    private static void validateTopLevelDirectory(final File pDir, PrintWriter out) {
        for (File file : pDir.listFiles()) {
            for (File file1 : file.listFiles()) {
                if (file1.isDirectory())
                    validateDirectory(file1, out);
            }
            System.out.println(".");
        }
    }

    private static void validateDirectory(final File pFile1, PrintWriter out) {
        File projectInternalPath = new File(pFile1, "internal");
        if (!projectInternalPath.exists()) {
             return;
        }
        String name = pFile1.getName();
        if(!GOOD_SET.contains(name))
            return;
        boolean hasMZTab = false;
        for (File mzTab : projectInternalPath.listFiles()) {
            // searching for mztab file
             if (mzTab.getName().endsWith(ArchiveProjectSpectraFilter.PRIDE_MZTAB_SUFFIX)) {
                hasMZTab = true;
                 try {
                     MZTabFileParser mzTabFileParser = null;
                     try {
                         mzTabFileParser = new MZTabFileParser(mzTab,new NullOutputStream());
                     } catch (Exception e) {
                         return;
                     }
                     MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();

                     if(mzTabFile == null)
                          continue;

                     Collection<Protein> proteins = mzTabFile.getProteins();
                     if(proteins == null)
                         continue;
                     for(Protein p : proteins)  {
                         String accession = p.getAccession();
                         String description = p.getDescription();
                         if(description == null)
                             continue;
                          if( accession.startsWith(BAD_ACCESSION) ||
                                   description.contains("Q8W233"))
                         {
                             out.println(mzTab.getAbsolutePath());
                             out.println(p.getDescription());
                             out.println(p.toString() );
                             System.out.println();
                             System.out.println(mzTab.getAbsolutePath());
                             System.out.println(p.getDescription());
                             System.out.println(p.toString() );

                          }
                     }
                 } catch (Exception e) {
                     return;
                 }
                 System.out.println(pFile1);
             }
        }

     }


    public static final int START_DIR = 2012; // 2005
    public static final int END_DIR = 2014; // 2005

    private static void validatePrideArchive(final PrintWriter pOut) {
        for (int i = START_DIR; i < END_DIR + 1; i++) {
            File dir = new File(Integer.toString(i));
            validateTopLevelDirectory(dir, pOut);
            System.out.println("Completed Directory " + i);
         }

        pOut.close();
    }

    private static void printUsage() {
        System.out.println("Usage:   [Output file containing the log] [ optional test dir - otherwise all of pride]");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }
        // output file for the filtered results
        PrintWriter out = new PrintWriter(new FileWriter(args[0]));

        // pass in a directory validate that
        if (args.length > 1) {
            validateDirectory(new File(args[1]), out);
        }
        else {
            validatePrideArchive(out);
        }

    }


}
