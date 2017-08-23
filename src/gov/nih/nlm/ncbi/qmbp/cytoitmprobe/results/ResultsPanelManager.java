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

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.main.OutputPanel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.SwingConstants;


public class ResultsPanelManager {
    
    private static HashMap<String, OutputPanel> panels;
    private static HashMap<String, Set<String>> parentNetworkMap;
    private static NetworkDestroyListener listener;
    
    public static void initResultsPanelManager() {
        panels = new HashMap<String, OutputPanel>();
        parentNetworkMap = new HashMap<String, Set<String>>();
        listener = new NetworkDestroyListener();
        Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
            Cytoscape.NETWORK_DESTROYED, listener);
    }
    
    public static void addResults(ItmProbeResults results) {

        OutputPanel outPanel = new OutputPanel(results);
        panels.put(results.queryPrefix, outPanel);

        String parentId = results.parentNetwork.getIdentifier();
        if (!parentNetworkMap.containsKey(parentId)) {
            parentNetworkMap.put(parentId, new HashSet<String>());
        }
        Set<String> queries = parentNetworkMap.get(parentId);
        queries.add(results.queryPrefix);
                
        CytoPanel resPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST);
        resPanel.add(results.getQueryPrefix(), outPanel);
        resPanel.setSelectedIndex(resPanel.indexOfComponent(outPanel));
        if (resPanel.getState() == CytoPanelState.HIDE) {
                resPanel.setState(CytoPanelState.DOCK);
        }
    }
    
    public static void removeResults(String queryPrefix) {
        OutputPanel outPanel = panels.get(queryPrefix);
        if (outPanel != null) {

            String parentId = outPanel.results.parentNetwork.getIdentifier();
            Set<String> queries = parentNetworkMap.get(parentId);
            queries.remove(queryPrefix);
            panels.remove(queryPrefix);
            
            CytoPanel resPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST);
            resPanel.remove(outPanel);
        }        
    }
        
    public static boolean hasResults(String queryPrefix) {
        return panels.containsKey(queryPrefix);        
    }

    private static String findQueryPrefix(String subNetworkId) {
       CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();
       String attr = cyNetworkAttrs.getStringAttribute(subNetworkId, 
                                                       "ItmQueryPrefix");
       return attr;        
    }
    
    private static class NetworkDestroyListener implements PropertyChangeListener {

        public NetworkDestroyListener() {
            super();
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equalsIgnoreCase(Cytoscape.NETWORK_DESTROYED)) {
                String networkId = (String) e.getNewValue();
                String queryPrefix;
                if (parentNetworkMap.containsKey(networkId)) {
                    Set<String> queries = parentNetworkMap.get(networkId);
                    for (Iterator<String> iter = queries.iterator();
                         iter.hasNext(); ) {
                        queryPrefix = iter.next();
                        removeResults(queryPrefix);
                    }
                }
                else {
                    queryPrefix = findQueryPrefix(networkId);
                    if (queryPrefix != null) {
                        removeResults(queryPrefix);
                    }
                }
            }
        }
    }
    
    
}
