package uk.ac.ebi.pride.cluster.tools.reanalysis.control.util;

import uk.ac.ebi.pride.cluster.tools.reanalysis.enums.AllowedPeptideShakerParams;
import uk.ac.ebi.pride.cluster.tools.reanalysis.enums.AllowedSearchGUIParams;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@ugent.be>
 */
public class CommandLineArgumentPrinter {

    public static void main(String[] args) {
        System.out.println("TO RUN A SEARCHGUI STEP : ");
        for (AllowedSearchGUIParams aParam : AllowedSearchGUIParams.values()) {
            if (aParam.isMandatory()) {
                System.out.println(aParam.id + "\t" + aParam.getDescription());
            }
        }
        System.out.println("ADDITIONALLY TO RUN A PEPTIDESHAKER STEP : ");
        for (AllowedPeptideShakerParams aParam : AllowedPeptideShakerParams.values()) {
            if (aParam.isMandatory()) {
                System.out.println(aParam.id + "\t" + aParam.getDescription());
            }
        }
    }
}
