package uk.ac.ebi.pride.spectracluster.export;

/**
 * uk.ac.ebi.pride.spectracluster.export.MSRunProcessor
 * User: Steve
 * Date: 8/6/2014
 */

// todo rewrite after new interface

import uk.ac.ebi.pride.jmztab.model.*;

import java.util.*;

/**
* uk.ac.ebi.pride.spectracluster.export.MSRunProcessor
*
* @author Steve Lewis
* @date 22/05/2014
*/
public class MSRunProcessor {

      private final Map<String,PSM>  spectrumToPSM = new HashMap<String, PSM>();

    public MSRunProcessor( ) {
      }

    public void addPSM(String spectrum,PSM added) {
        spectrumToPSM.put(spectrum,added);
    }


    public PSM getPSM(String spectrum ) {
        return spectrumToPSM.get(spectrum );
    }

}
