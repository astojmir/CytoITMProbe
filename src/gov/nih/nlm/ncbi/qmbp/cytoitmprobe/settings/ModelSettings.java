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
// Code author:  Aleksandar Stojmirovic
//

package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResults;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResultsFactory;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ResultsPanelManager;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.GraphInteraction;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.ItmProbeTools;
import java.util.*;




public abstract class ModelSettings extends InputSettings {
    
    public String modelName;    
    public boolean hasSources;    
    public boolean hasSinks;    
    public String [] dampingParamNames;    
    public String [] dampingParamDefaults;
    
    protected abstract String getJsonParams(String weightAttr,
                                         EdgeTypes currentEdgeTypes, 
                                         List<String> sources,
                                         List<String> sinks,
                                         Map<String, Double> antisinks,
                                         int dampingIndex,
                                         String dampingValue);
    
    public abstract String validate(String weightAttr,
                                    Set<String> currentEdgeTypes, 
                                    List<String> sources,
                                    List<String> sinks,
                                    Map<String, Double> antisinks,
                                    int dampingIndex,
                                    String dampingValString);

    public List<Integer> filterCustomCols(List<String> header) {
        return new ArrayList<Integer>();
    }

    public String queryItmProbe(String weightAttr,
                                EdgeTypes currentEdgeTypes, 
                                List<String> sources,
                                List<String> sinks,
                                Map<String, Double> antisinks,
                                int dampingIndex,
                                String dampingValue) {
        
        if (!hasSinks) {
            sinks = null;
        }
        if (!hasSources) {
            sources = null;
        }
     	ItmProbeQuery task = new ItmProbeQuery(weightAttr, 
                                               currentEdgeTypes, 
                                               sources, 
                                               sinks, 
                                               antisinks, 
                                               dampingIndex, 
                                               dampingValue);

        // Configure JTask Dialog Pop-Up Box
        JTaskConfig jTaskConfig = new JTaskConfig();
        jTaskConfig.setOwner(Cytoscape.getDesktop());
        jTaskConfig.displayCloseButton(true);
        jTaskConfig.displayCancelButton(true);

        jTaskConfig.displayStatus(true);
        jTaskConfig.setAutoDispose(true);
        jTaskConfig.displayTimeElapsed(true);
        jTaskConfig.displayTimeRemaining(false);
        
        // Execute Task in New Thread; pops open JTask Dialog Box.
        TaskManager.executeTask(task, jTaskConfig);
        return task.errMsg;
    }
    
    protected Double validateDampingValue(String dampingValString) {
        Double dampingValue;
        try {
	    dampingValue = new Double(dampingValString);
            
	}
	catch (Exception e) {
	    dampingValue = null;
	}
        return dampingValue;
    }
    
    protected String validateGraphParams(String weightAttr,
                                         Set<String> currentEdgeTypes, 
                                         List<String> sources,
                                         List<String> sinks,
                                         Map<String, Double> antisinks) {
        
        CyNetwork network = Cytoscape.getCurrentNetwork();

        // Validate edge types
        Set edgeTypes = GraphInteraction.getEdgeTypes(network);
	if (!currentEdgeTypes.equals(edgeTypes)) {
	    return "Edge types do not correspond to the current graph and must be refreshed.";
        }
       
        // Validate nodes
        Object [] selectedNodes = new Object [] {sources, 
                                                 sinks, 
                                                 antisinks.keySet()};
        for (int i=0; i < selectedNodes.length; i++) {
            Collection<String> nodes = (Collection<String>) selectedNodes[i];
            if (nodes == null) {
                continue;
            }
            for (String nodeId : nodes) {
                CyNode node = Cytoscape.getCyNode(nodeId);

                if (node == null) {
                    return String.format("Node %s does not exist in Cytoscape.",
                                         nodeId);
                }

                if (network.getNode(node.getRootGraphIndex()) == null) {
                    return String.format("Node %s does not exist in the current graph.",
                                         nodeId);
                }
            }
        }
        return null;      
    }

    
    public class ItmProbeQuery implements Task {
        
        private cytoscape.task.TaskMonitor taskMonitor;

        private String weightAttr;
        private EdgeTypes currentEdgeTypes; 
        private List<String> sources;
        private List<String> sinks;
        private Map<String, Double> antisinks;
        private int dampingIndex;
        private String dampingValue;
        
        public String errMsg = null;
        private boolean isHalted = false;
        
        public ItmProbeQuery(String weightAttr,
                             EdgeTypes currentEdgeTypes, 
                             List<String> sources,
                             List<String> sinks,
                             Map<String, Double> antisinks,
                             int dampingIndex,
                             String dampingValue) {
            
            this.weightAttr = weightAttr;
            this.currentEdgeTypes = currentEdgeTypes;
            this.sources = sources;
            this.sinks = sinks;
            this.antisinks = antisinks;
            this.dampingIndex = dampingIndex;
            this.dampingValue = dampingValue;
        }

        public void setTaskMonitor(TaskMonitor monitor)
                        throws IllegalThreadStateException {
            taskMonitor = monitor;
        }

        public void halt() {
            isHalted = true;
        }

        public String getTitle() {
            return "Running ITM Probe Query";
        }

        public void run() {
            
            try {

                taskMonitor.setPercentCompleted(0);
                taskMonitor.setStatus("Preparing input...");


                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(0);
                taskMonitor.setStatus("Preparing input...");
                String jsonOutput = getJsonParams(weightAttr, 
                                                  currentEdgeTypes, 
                                                  sources, 
                                                  sinks, 
                                                  antisinks, 
                                                  dampingIndex, 
                                                  dampingValue);

                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(10);
                taskMonitor.setStatus("Querying ITM Probe...");
                String itmProbeOutput;
                itmProbeOutput = ItmProbeTools.itmProbeRun(jsonOutput);

                // System.out.println("***" + itmProbeOutput);

                if (itmProbeOutput.startsWith("ERROR:")) {
                    errMsg = itmProbeOutput;
                    return;
                }


                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(60);
                taskMonitor.setStatus("Processing results...");
                HashMap<String, ArrayList<ArrayList>> data = parseOutput(itmProbeOutput);

                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(65);
                taskMonitor.setStatus("Loading ITM into query network...");                               
                CyNetwork parentNetwork = Cytoscape.getCurrentNetwork();               
                String queryPrefix = setNetworkAttrs(data,
                                                     parentNetwork,
                                                     sources,
                                                     sinks,
                                                     currentEdgeTypes,
                                                     weightAttr);
                
                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(70);
                taskMonitor.setStatus("Displaying ITM subgraph and tables...");
                ItmProbeResults results;
                results = ItmProbeResultsFactory.fromCyNetwork(parentNetwork,
                                                               queryPrefix);
                ResultsPanelManager.addResults(results);
            }
            catch (Exception ex) {
                taskMonitor.setException(ex, null);
            }
        }
    }
    

}
