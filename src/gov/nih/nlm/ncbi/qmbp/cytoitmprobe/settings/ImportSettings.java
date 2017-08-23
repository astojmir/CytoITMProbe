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
import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import java.util.List;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.config.Configuration;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResults;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ItmProbeResultsFactory;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.results.ResultsPanelManager;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.GraphInteraction;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.ItmProbeTools;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;



public class ImportSettings extends InputSettings {
    
    private CyNetwork parentNetwork = null;
    private HashMap<String, ArrayList<ArrayList>> importedData = null;

    private List<String> sources = null;
    private List<String> sinks = null;
    private EdgeTypes edgeTypes = null;
    private String weightAttr = null;
  
    private char modelType = 'X';
    private ArrayList<Integer> boundary = new ArrayList<Integer>();
    private ArrayList<Integer> vars = new ArrayList<Integer>();
    private ArrayList<Integer> custom = new ArrayList<Integer>();

    private List<String> undirected = new ArrayList<String>();
    private List<String> directed = new ArrayList<String>();
    private List<String> ignored = new ArrayList<String>();
    private List<String> boundaryCols = new ArrayList<String>();
    private List<String> varCols = new ArrayList<String>();
    
    private String errMsg = null;
    private String wrnMsg = null;
    
    
    public ImportSettings() {
        super();
    }
    
    public void importFromTab() {
        
        // Configuration is global but must be initialized from file before
        // usage. We make sure it is properly initialized just before running
        // a query, changing configuration or importing an ITM (here).
        errMsg = ItmProbeTools.checkConfig();
        if (errMsg == null) {
            parentNetwork = Cytoscape.getCurrentNetwork();
            String chosenFile = getImportFilename();
            if (chosenFile == null) {
                return;
            }

            TabImport task = new TabImport(chosenFile);

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
        }
        // HERE SHOW ERROR or WARNING MESSAGE
        if (errMsg != null) {        
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                                          errMsg,
                                          "Import ITM Probe Results",
                                          JOptionPane.ERROR_MESSAGE);
        }
        else if (wrnMsg != null) {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                                          wrnMsg,
                                          "Import ITM Probe Results",
                                          JOptionPane.WARNING_MESSAGE);            
        }

    }

    
    private String getImportFilename() {
        
        CyFileFilter filter = new CyFileFilter("tab");
        File chosenFile = FileUtil.getFile("Import ITM Probe Results",
                                           FileUtil.LOAD,
                                           new CyFileFilter[] { filter });
        if (chosenFile == null) {
            return null;
        }        
        return chosenFile.getAbsolutePath();
    }
    
    private String readFile(String chosenFile) 
            throws FileNotFoundException, UnsupportedEncodingException, 
                   IOException {
                
        FileInputStream fileStream = new FileInputStream(chosenFile);
        InputStreamReader reader = new InputStreamReader(fileStream, "US-ASCII");
        BufferedReader inStream = new BufferedReader(reader);

        StringBuilder outputBuffer = new StringBuilder();
        while (true) {
            String lineRead = inStream.readLine();
            if (lineRead == null){
                break;
            }
            outputBuffer.append(lineRead).append("\n");
        }               
        fileStream.close();
        return outputBuffer.toString();
    } 
    
    
    private void loadQueryData() throws ImportException {
        
        ArrayList<ArrayList> section = importedData.get("QUERY DATA");
        if (section == null) {
            throw new ImportException("Invalid file format");
        }
        for (ArrayList row : section) {
            if (row.size() < 2) {
                continue;
            }
            String key = (String) row.get(0);
            
            if (key.equals(ItmProbeResults.MODEL_TAG)) {
                try {
                    modelType = ((String) row.get(1)).charAt(0);
                }
                catch (IndexOutOfBoundsException exc) {
                    modelType = 'X';
                }
            }
            else if (key.equals(ItmProbeResults.BOUNDARY_TAG)) {
                boundaryCols = row.subList(1, row.size());            
            }
            else if (key.equals(ItmProbeResults.VARIABLE_TAG)) {
                varCols = row.subList(1, row.size());
            }
            else if (key.equals(ItmProbeResults.SOURCES_TAG)) {
                sources = row.subList(1, row.size());
            }
            else if (key.equals(ItmProbeResults.SINKS_TAG)) {
                sinks = row.subList(1, row.size());
            }
            else if (key.equals(ItmProbeResults.UNDIRECTED_TAG)) {
                undirected = row.subList(1, row.size());            
            }
            else if (key.equals(ItmProbeResults.DIRECTED_TAG)) {
                directed = row.subList(1, row.size());            
            }
            else if (key.equals(ItmProbeResults.IGNORED_TAG)) {
                ignored = row.subList(1, row.size());
            }
            else if (key.equals(ItmProbeResults.WEIGHT_TAG)) {
                weightAttr = (String) row.get(1);            
            }            
        }
        
        if (modelType != ItmProbeResultsFactory.ABSORBING_MODEL && 
            modelType != ItmProbeResultsFactory.EMITTING_MODEL &&
            modelType != ItmProbeResultsFactory.CHANNEL_MODEL) {
            throw new ImportException("Cannot reckognize model type.");
        }
        verifyMainSection();
        sources = filterInvalidIds(sources);
        sinks = filterInvalidIds(sinks);
        verifyEdges();
    }
    
    private void verifyMainSection() throws ImportException {
        
        final String nodeWrnFmt = 
        "The number of nodes specifed in the selected file and the selected " +
        "network do not match.\nThe file contains %d nodes, while %d nodes' " +
        "IDs match network nodes' IDs.\n\n";
        
        final String nodeErrMsg = 
        "The node IDs supplied by the selected file do not match any node " +
        "in the selected network.";        
        
    ArrayList<ArrayList> section = importedData.get("TOP RANKING NODES");
        if (section == null) {
            throw new ImportException("Invalid file format");
        }
        
        ArrayList<String> header = section.get(0);
        
        for (int i=3; i < header.size(); i++) {
            String item = header.get(i);
            if (boundaryCols.contains(item)) {
                boundary.add(i);
            }
            else if (varCols.contains(item)) {
                vars.add(i);
            }
            else {
                custom.add(i);
            }
        }        

        ArrayList<String> nodeIds = new ArrayList<String>();
        for (ArrayList<String> line : section.subList(1, section.size())) {
                String nodeId = line.get(1);
                nodeIds.add(nodeId);                
        }
        List<String> validIds = filterInvalidIds(nodeIds);
        if (validIds.isEmpty()) {
            throw new ImportException(nodeErrMsg);     
        }        
        if (nodeIds.size() != validIds.size()) {
            wrnMsg = String.format(nodeWrnFmt, nodeIds.size(), validIds.size()); 
        }                
    }
    
    private void verifyEdges() {
    
        final String edgeTypeWrn = 
        "Imported edge types do not fully match the network edge types.\n\n";
        
        final String edgeWghtWrn = 
        "Imported weight atribute is invalid.\n\n";
        
        boolean validEdgeTypes = true;
        boolean validEdgeWeights = true;
        
        Set<String> newUndirected = new HashSet(undirected);
        newUndirected.retainAll(GraphInteraction.getEdgeTypes(parentNetwork));
        if (newUndirected.size() != undirected.size()) {
            validEdgeTypes = false;
        }
        
        Set<String> newDirected = new HashSet(directed);
        newDirected.retainAll(GraphInteraction.getEdgeTypes(parentNetwork));
        if (newDirected.size() != directed.size()) {
            validEdgeTypes = false;
        }
        
        Set<String> newIgnored = new HashSet(ignored);
        newIgnored.retainAll(GraphInteraction.getEdgeTypes(parentNetwork));
        if (newIgnored.size() != ignored.size()) {
            validEdgeTypes = false;
        }
        
        edgeTypes = new EdgeTypes(newUndirected, newDirected, newIgnored);

        List<String> edgeAttrs = GraphInteraction.getEdgeAttributeNames(parentNetwork);
        if (!edgeAttrs.contains(weightAttr)) {
            weightAttr = edgeAttrs.get(0); // Default edge attribute
            validEdgeWeights = false;
        }
        
        if (!validEdgeTypes || !validEdgeWeights) {
            if (wrnMsg == null) {
                wrnMsg = "";
            }
            if (!validEdgeTypes) {
                wrnMsg += edgeTypeWrn;
            }    
            if (!validEdgeWeights) {
                wrnMsg += edgeWghtWrn;
            }                        
        }                        
    }
    
    private List<String> filterInvalidIds(List<String> Ids) {
        if (Ids == null) {
            return null;
        }
        ArrayList<String> newIds = new ArrayList<String>();
        for (String nodeId : Ids) {
           CyNode node = Cytoscape.getCyNode(nodeId, false); 
           if (node != null && parentNetwork.containsNode(node)) {
               newIds.add(nodeId);
           }            
        }
        return newIds;        
    }
    

    
    
    public String buildPrefix() {
        return String.format("ITM%c%03d", modelType, Configuration.getCount());
    }

    public List<Integer> filterBoundaryCols(List<String> header) {
        return boundary;
    }

    public List<Integer> filterSummaryCols(List<String> header) {
        return vars;
    }

    public List<Integer> filterCustomCols(List<String> header) {
        return custom;
    }

    
    public class TabImport implements Task {
        
        private cytoscape.task.TaskMonitor taskMonitor;
        private String chosenFile;        
        private boolean isHalted = false;
        
        public TabImport(String chosenFile) {            
            this.chosenFile = chosenFile;
        }

        public void setTaskMonitor(TaskMonitor monitor)
                        throws IllegalThreadStateException {
            taskMonitor = monitor;
        }

        public void halt() {
            isHalted = true;
        }

        public String getTitle() {
            return "Importing ITM from TAB-delimited File";
        }

        public void run() {
            
            try {

                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(10);
                taskMonitor.setStatus("Reading file...");
                String rawData = readFile(chosenFile);

                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(20);
                taskMonitor.setStatus("Parsing output...");
                importedData = parseOutput(rawData);
                

                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(20);
                taskMonitor.setStatus("Loading ITM into selected network...");
                loadQueryData();
                Configuration.incrementCount();
                String queryPrefix = setNetworkAttrs(importedData,
                                                     parentNetwork,
                                                     sources,
                                                     sinks,
                                                     edgeTypes,
                                                     weightAttr);
                if (isHalted) {
                    return;
                }
                taskMonitor.setPercentCompleted(50);
                taskMonitor.setStatus("Displaying ITM subgraph and tables...");
                ItmProbeResults results;
                results = ItmProbeResultsFactory.fromCyNetwork(parentNetwork,
                                                               queryPrefix);
                ResultsPanelManager.addResults(results);
            }
            catch (Exception ex) {
                errMsg = "Could not import from " + chosenFile + ".\n\n" +
                         ex.getMessage();
            }
        }
    }

       
}
