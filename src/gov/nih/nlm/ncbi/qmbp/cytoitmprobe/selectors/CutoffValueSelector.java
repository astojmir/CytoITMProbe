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

package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.selectors;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author stojmira
 */
public class CutoffValueSelector extends MaxCountSelector {

    private Double cutoffValue;

    public CutoffValueSelector(int maxNodes, Double cutoffValue) {
        super(maxNodes);
        this.cutoffValue = cutoffValue;
    }

    @Override
    public ArrayList<String> select(ArrayList data) {

        Collections.sort(data, cmp);
        ArrayList<String> selectedNodeIds = new ArrayList<String>();
        int n = data.size() > maxNodes ? maxNodes : data.size();
        for (int i = 0; i < n; i++) {
            Double value = (Double) ((Object [])data.get(i))[1];
            if (value >= cutoffValue) {
                selectedNodeIds.add((String) ((Object [])data.get(i))[0]);

            }
        }
        return selectedNodeIds;
    }
}
