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
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.CyAttributesUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author aleksand
 */
public class ItmProbeResultsFactory {
    
    public static final char CHANNEL_MODEL = 'C';
    public static final char EMITTING_MODEL = 'E';
    public static final char ABSORBING_MODEL = 'A';
    
    /**
     * Get an instance of ItmProbeResults from a given network.
     */
    public static ItmProbeResults fromCyNetwork(CyNetwork parentNetwork, 
                                                String queryPrefix) {

        ItmProbeResults results = null;
        
        // Get model type from queryPrefix - it has a format
        // "ITM%c%03d" - so the fourth letter gives the model.
        char modelType = queryPrefix.charAt(3);
        switch (modelType) {
            case ABSORBING_MODEL:
                results = new AbsorbingResults(parentNetwork, queryPrefix);
                break;
            case EMITTING_MODEL:
                results = new EmittingResults(parentNetwork, queryPrefix);
                break;
            case CHANNEL_MODEL:
                results = new ChannelResults(parentNetwork, queryPrefix);
                break;  
        }
        return results;
    }
    
    public static boolean hasStoredItms(CyNetwork parentNetwork,
                                        boolean excludeDisplayed) {
        String networkId = parentNetwork.getIdentifier();
        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        String [] attrNames = cyNodeAttrs.getAttributeNames();

        List<String> netAttrs = CyAttributesUtils.getAttributeNamesForObj(
                                 networkId, cyNetworkAttrs);
        for (int i=0; i < netAttrs.size(); i++) {
            String attr = netAttrs.get(i);
            if (isValidItmIndicator(attr, attrNames, cyNodeAttrs, 
                                    excludeDisplayed)) {
                return true;
            }
        }
        return false;
    }
    
    public static List<String []> findStoredItms(CyNetwork parentNetwork,
                                                 boolean excludeDisplayed) {
        ArrayList<String []> itmData = new ArrayList<String []>();

        String networkId = parentNetwork.getIdentifier();
        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        String [] attrNames = cyNodeAttrs.getAttributeNames();

        List<String> netAttrs = CyAttributesUtils.getAttributeNamesForObj(
                                 networkId, cyNetworkAttrs);
        for (int i=0; i < netAttrs.size(); i++) {
            String attr = netAttrs.get(i);
            if (isValidItmIndicator(attr, attrNames, cyNodeAttrs, 
                                    excludeDisplayed)) {
                String queryPrefix = attr.substring(0, 7);
                String modelName = "";
                char modelType = queryPrefix.charAt(3);
                switch (modelType) {
                    case ABSORBING_MODEL:
                        modelName = "Absorbing";
                        break;
                    case EMITTING_MODEL:
                        modelName = "Emitting";
                        break;
                    case CHANNEL_MODEL:
                        modelName = "Normalized Channel";
                        break;
                }
                String contextInfo;
                List<String> contextLine = cyNetworkAttrs.getListAttribute(
                                networkId, String.format("%sI03", queryPrefix));
                if (contextLine != null && contextLine.size() > 0) {
                    contextInfo = String.format(", %s", contextLine.get(1));                    
                }
                else {
                    contextInfo = "";
                }
                String [] line = {queryPrefix,
                                  String.format("%s {%s%s }", queryPrefix,
                                                modelName, contextInfo)};
                itmData.add(line);
            }
        }
       Comparator cmp =  new Comparator(){
            public int compare(Object o1, Object o2) {
                String [] objs1 = (String []) o1;
                String [] objs2 = (String []) o2;
                String p1 = objs1[0];
                String p2 = objs2[0];
                return p1.compareTo(p2);
            }
        };
       Collections.sort(itmData, cmp);
        return itmData;       
    }
    
    private static boolean isValidItmIndicator(String attr,
                                               String [] attrNames,
                                               CyAttributes cyNodeAttrs,
                                               boolean excludeDisplayed) {
        if (attr.length() == 10 && 
            attr.startsWith("ITM") && 
            attr.endsWith("B00")) {
            String queryPrefix = attr.substring(0, 7);
            if (excludeDisplayed && 
                ResultsPanelManager.hasResults(queryPrefix)) {
                return false;
            }
            char modelType = queryPrefix.charAt(3);
            if (modelType != ABSORBING_MODEL &&
                modelType != EMITTING_MODEL  &&
                modelType != CHANNEL_MODEL) {
                return false;
            }            
            for (int j=0; j < attrNames.length; j++) {
                String attrName = attrNames[j];
                byte attrType = cyNodeAttrs.getType(attrName);
                if (attrType == CyAttributes.TYPE_FLOATING && 
                    attrName.startsWith(queryPrefix)) {
                    return true;
                }
            }
        }        
        return false;
    }    
}
