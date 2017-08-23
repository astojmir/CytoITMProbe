//
// ===========================================================================
//
//                            PUBLIC DOMAIN NOTICE
//               National Center for Biotechnology Information
//
//  This software/database is a "United States Government Work" under the
//  terms of the United States Copyright Act.  It was written as part of
//  the author's official duties as a United States Government employee and
//  thus cannot be copyrighted.  This software/database is freely available
//  to the public for use. The National Library of Medicine and the U.S.
//  Government have not placed any restriction on its use or reproduction.
//
//  Although all reasonable efforts have been taken to ensure the accuracy
//  and reliability of the software and data, the NLM and the U.S.
//  Government do not and cannot warrant the performance or results that
//  may be obtained by using this software or data. The NLM and the U.S.
//  Government disclaim all warranties, express or implied, including
//  warranties of performance, merchantability or fitness for any particular
//  purpose.
//
//  Please cite the author in any work or product based on this material.
//
// ===========================================================================
//
// Code authors:  Aleksandar Stojmirovic, Alexander Bliskovsky
//

package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results;

import cytoscape.CyNetwork;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.selectors.CutoffValueSelector;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.selectors.MaxCountSelector;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.selectors.NodeSelector;
import java.util.ArrayList;


public class AbsorbingResults extends ItmProbeResults {

    public AbsorbingResults(CyNetwork parentNetwork, String queryPrefix) {
        super(parentNetwork, queryPrefix);
        String [] s = {"Cutoff Value", "Maximum Nodes"};
        selectionCriteria = s;
        defaultSelectionCriterion = 1;
        defaultNodeSelector = new MaxCountSelector(defaultMaxNodes);
    }
    
    @Override
    protected ArrayList<String> getColFullNames() {
        ArrayList<String> colFullNames = new ArrayList<String>();
        for (String attr : validBoundary) {
            String name = String.format("Absorbing probability to %s", attr);
            colFullNames.add(name);            
        }
        for (String attr : validVariables) {
            String name;
            if (attr.equals("Total")) {
                name = "Total Likelihood";
            }
            else {
                name = attr;
            }
            colFullNames.add(name);            
        }
        for (String attr : validCustom) {
            colFullNames.add(String.format("Custom attribute [%s]", attr));            
        }
        return colFullNames;       
    }

    @Override
    public NodeSelector getNodeSelector(int criterionIndex, Double cutoffValue,
                                        int maxNodes) {
        if (criterionIndex == 0) {
            return new CutoffValueSelector(maxNodes, cutoffValue);
        }
        else if (criterionIndex == 1) {
            return new MaxCountSelector(maxNodes);
        }
        return null;
    }

}
