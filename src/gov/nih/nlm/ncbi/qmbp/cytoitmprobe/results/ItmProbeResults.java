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

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.CyAttributesUtils;
import cytoscape.data.Semantics;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.layout.LayoutProperties;
import cytoscape.layout.Tunable;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualStyle;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.selectors.MaxCountSelector;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.selectors.NodeSelector;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.EdgeTypes;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.CsrAdjacencyMatrix;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.visual.EdgeStyling;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.visual.GlobalStyling;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.visual.MixedNodeStyling;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.visual.MonoNodeStyling;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public abstract class ItmProbeResults {
    
    public static final String MODEL_TAG = "model_type";
    public static final String BOUNDARY_TAG = "boundary_columns";
    public static final String VARIABLE_TAG = "variable_columns";
    public static final String SOURCES_TAG = "sources";
    public static final String SINKS_TAG = "sinks";
    public static final String UNDIRECTED_TAG = "undirected_edges";
    public static final String DIRECTED_TAG = "directed_edges";
    public static final String IGNORED_TAG = "ignored_edges";
    public static final String WEIGHT_TAG = "weight_attribute";

    protected CyNetwork subNetwork;
    protected CyNetwork parentNetwork;

    protected String queryPrefix;

    protected ArrayList<String> validBoundary = null;
    protected ArrayList<String> validVariables = null;
    protected ArrayList<String> validCustom = null;
    
    protected List<String> sources = null;
    protected List<String> sinks = null;
    protected EdgeTypes queryEdgeTypes;
    protected String weightAttr = null;
    protected ArrayList<String> selfAdjacent = null;
    
    protected String fwdEdgeAttr;
    protected String bwdEgdeAttr;
    
    protected NodeSelector defaultNodeSelector = null;
    protected String [] selectionCriteria = null;
    protected int defaultSelectionCriterion = 0;

    protected double defaultCutoffValue = 0.05;
    protected int defaultMaxNodes = 40;

    protected String [] monoScalings = MonoNodeStyling.getScalingNames();
    protected int defaultMonoScaling = MonoNodeStyling.getDefaultScalingIndex();
    protected String [] mixedScalings = MixedNodeStyling.getScalingNames();
    protected int defaultMixedScaling = MixedNodeStyling.getDefaultScalingIndex();

    protected String [] monoColormaps = MonoNodeStyling.getColormapNames();
    protected int defaultColormap = MonoNodeStyling.getDefaultColormapIndex();
    
    public ItmProbeResults(CyNetwork parentNetwork, String queryPrefix) {

	this.parentNetwork = parentNetwork;
        this.queryPrefix = queryPrefix;
        
        fwdEdgeAttr = String.format("%s{forwardWeight}", queryPrefix);
        bwdEgdeAttr = String.format("%s{backwardWeight}", queryPrefix);
        subNetwork = null;
        resetValidCols();
    }
      
    public CyNetwork getParentNetwork() {
        return parentNetwork;
    }

    public CyNetwork getSubNetwork() {
        return subNetwork;
    }

    public void setSubNetwork(CyNetwork subNetwork) {
        this.subNetwork = subNetwork;
    }
    
    public char getModelType() {
        char modelType = queryPrefix.charAt(3);
        return modelType;
    }

    public Vector<Vector> getInputParameters() {
        return getParamsAsNetworkAttrs('I');
    }

    public Vector<String> getInputParametersHeader() {
        return new Vector(Arrays.asList(new String [] {"Parameter", "Value"}));
    }

    public Vector<Vector> getSummary() {
        return getParamsAsNetworkAttrs('S');
    }

    public Vector<String> getSummaryHeader() {
        return new Vector(Arrays.asList(new String [] {"Quantity", "Value"}));
    }
    
    public String getExcludedNodes() {
        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();        
        String excludedNodes = cyNetworkAttrs.getStringAttribute(
                                          parentNetwork.getIdentifier(),
                                          String.format("%sE00", queryPrefix));
        if (excludedNodes == null) {
            excludedNodes = "";
        }
        return excludedNodes;
    }

    private ArrayList<String> getColAttributes() {
        ArrayList<String> colAttributes = new ArrayList<String>();
        for (String item : validBoundary) {
            colAttributes.add(String.format("%s[%s]", queryPrefix, item));            
        }
        for (String item : validVariables) {
            colAttributes.add(String.format("%s[%s]", queryPrefix, item));            
        }
        for (String item : validCustom) {
            colAttributes.add(String.format("%s[%s]", queryPrefix, item));            
        }
        return colAttributes;
    }

    private ArrayList<String> getColHeadings() {
        ArrayList<String> colHeadings = new ArrayList<String>();
        for (String item : validBoundary) {
            colHeadings.add(item);            
        }
        for (String item : validVariables) {
            colHeadings.add(item);            
        }
        for (String item : validCustom) {
            colHeadings.add(item);            
        }
        return colHeadings;
    }

    protected abstract ArrayList<String> getColFullNames();
    
    public int getDefaultMixedScaling() {
        return defaultMixedScaling;
    }

    public int getDefaultMonoScaling() {
        return defaultMonoScaling;
    }

    public NodeSelector getDefaultNodeSelector() {
        return defaultNodeSelector;
    }

    public int getDefaultSelectionCriterion() {
        return defaultSelectionCriterion;
    }

    public String[] getMixedScalings() {
        return mixedScalings;
    }

    public String[] getMonoScalings() {
        return monoScalings;
    }

    public String getQueryPrefix() {
        return queryPrefix;
    }

    public String[] getSelectionCriteria() {
        return selectionCriteria;
    }

    public double getDefaultCutoffValue() {
        return defaultCutoffValue;
    }

    public int getDefaultMaxNodes() {
        return defaultMaxNodes;
    }

    public String [] getRankingAttributes() {
        resetValidCols();
        ArrayList<String> colFullNames = getColFullNames();
        String [] s = new String [colFullNames.size()];
        return colFullNames.toArray(s);
    }

    public int getDefaultRankingAttribute() {
        return validBoundary.size() + validVariables.size() - 1;
    }

    public String [] getColoringAttributes() {
        ArrayList<String> colFullNames = getColFullNames();
        String [] s = new String [colFullNames.size()];
        return colFullNames.toArray(s);
    }

    public int getDefaultColoringAttribute() {
        return validBoundary.size() + validVariables.size() - 1;
    }
    
    public boolean isCutoffSelection(int index) {
        if (index == 0) {
            return true;
        }
        return false;
    }

    public int getDefaultColormap() {
        return defaultColormap;
    }

    public String[] getMonoColormaps() {
        return monoColormaps;
    }
    
   private Vector<Vector> getParamsAsNetworkAttrs(char paramType) {
        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();
        String networkId = parentNetwork.getIdentifier();
        Vector<Vector> paramData = new Vector<Vector>();
        String attrPrefix = String.format("%s%c", queryPrefix, paramType);
        List<String> attrNames = CyAttributesUtils.getAttributeNamesForObj(
                                   networkId, cyNetworkAttrs);
        ArrayList<String> validNames = new ArrayList();
        for (String attrName : attrNames) {
            if (attrName.startsWith(attrPrefix)) {
                validNames.add(attrName);
            }            
        }
        Collections.sort(validNames);
        for (String attrName :  validNames) {
            List row = cyNetworkAttrs.getListAttribute(networkId, attrName);
            paramData.add(new Vector(row));
        }
        return paramData;
    }
       
    private boolean hasNodeAttribute(String attribute) {
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        ArrayList<String> attrList= new ArrayList(Arrays.asList(cyNodeAttrs.getAttributeNames()));
        if (attrList.indexOf(attribute) == -1) {
            return false;
        }
        return true;
    }

    private void resetValidCols() {
        
        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        String [] attrNames = cyNodeAttrs.getAttributeNames();

        LinkedList<String> activeCols = new LinkedList<String>();
        for (String attrName : attrNames) {
            final byte attrType = cyNodeAttrs.getType(attrName);
            if (attrType == CyAttributes.TYPE_FLOATING && 
                attrName.startsWith(queryPrefix) ) {
                activeCols.add(attrName.substring(queryPrefix.length()+1, 
                                                   attrName.length()-1));
            }
        }
        String networkId = parentNetwork.getIdentifier();
        List boundary = cyNetworkAttrs.getListAttribute(networkId, 
                                         String.format("%sB00", queryPrefix));       
        List vars = cyNetworkAttrs.getListAttribute(networkId,
                                     String.format("%sV00", queryPrefix));
        
        validBoundary = extractValidCols(boundary, activeCols);
        validVariables = extractValidCols(vars, activeCols);
        validCustom = new ArrayList<String>(activeCols);
        
        sources = cyNetworkAttrs.getListAttribute(networkId, 
                                         String.format("%sB01", queryPrefix));
        sinks = cyNetworkAttrs.getListAttribute(networkId, 
                                         String.format("%sB02", queryPrefix));
        
        List<String> undirectedList = cyNetworkAttrs.getListAttribute(networkId, 
                                         String.format("%sB03", queryPrefix));
        List<String> directedList = cyNetworkAttrs.getListAttribute(networkId, 
                                         String.format("%sB04", queryPrefix));
        List<String> ignoredList = cyNetworkAttrs.getListAttribute(networkId, 
                                         String.format("%sB05", queryPrefix));
        
        queryEdgeTypes = new EdgeTypes(undirectedList, directedList, ignoredList);
        
        weightAttr = cyNetworkAttrs.getStringAttribute(networkId, 
                                           String.format("%sB06", queryPrefix));
    }
    
    private ArrayList extractValidCols(List possible, List valid) {
        ArrayList extracted = new ArrayList();
        if (possible != null && valid != null) {           
            for (int i=0; i < possible.size(); i++) {          
                int k = valid.indexOf(possible.get(i));
                if (k != -1) {
                    extracted.add(valid.get(k));
                    valid.remove(k);
                }
            }
        }
        return extracted;
    }
        
    private Object [] getNodeTableData(int attributeIndex,
                                       NodeSelector nodeSelector,
                                       boolean addNodeIds) {

        ArrayList<String> selectedNodeIds = new ArrayList<String>();
        Vector<String> header = new Vector<String>();
        Vector<Vector<String>> body = new Vector<Vector<String>>();

        header.add("Rank");
        if (addNodeIds) {
            header.add("NodeID");
        }
        header.add("Node");

        
        ArrayList<String> colAttributes = getColAttributes();
        ArrayList<String> colHeadings = getColHeadings();
        
        if (colAttributes.isEmpty()) {
            return new Object [] {selectedNodeIds, header, body};
        }
                    
        attributeIndex = attributeIndex % colAttributes.size();
        if (attributeIndex < 0) {
            attributeIndex =  colAttributes.size() + attributeIndex;
        }

	CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        String colAttr = colAttributes.get(attributeIndex);
        
        if (hasNodeAttribute(colAttr)) {
            // Extract attribute used for node selection
            Iterator nodeIter = parentNetwork.nodesIterator();
            ArrayList nodeItems = new ArrayList();
            while(nodeIter.hasNext()) {
                CyNode node = (CyNode) nodeIter.next();
                String nodeId = node.getIdentifier();
                Double value = cyNodeAttrs.getDoubleAttribute(nodeId, colAttr);
                if (value != null) {
                    nodeItems.add(new Object [] {nodeId, value});
                }
            }
            selectedNodeIds = nodeSelector.select(nodeItems);            
        }
        else {
            return new Object [] {selectedNodeIds, header, body};
        }

        // Construct header
        Vector<String> attributes = new Vector<String>();
        for (int j = 0; j < colAttributes.size(); j++) {
            String attr = colAttributes.get(j);
            if (hasNodeAttribute(attr)) {
                attributes.add(attr);
                header.add(colHeadings.get(j));
            }
        }

        // Construct body
        for (int i = 0; i < selectedNodeIds.size(); i++) {
            Vector row = new Vector();
            String nodeId = selectedNodeIds.get(i);
            row.add(i+1);
            if (addNodeIds) {
                row.add(nodeId);
            }
            row.add(cyNodeAttrs.getStringAttribute(nodeId, 
                                                   Semantics.CANONICAL_NAME));
            for (String attr : attributes) {
                row.add(cyNodeAttrs.getDoubleAttribute(nodeId, attr));
            }
            body.add(row);
        }

        // Pack result
        Object [] tableData = {selectedNodeIds, header, body};
        return tableData;
    }

    private void createSubNetwork(ArrayList<String> selectedNodeIds) {

        String title = "ITM Probe Query " + queryPrefix;
        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();
        if (subNetwork == null) {
            
            Set<CyNetwork> allNetworks = Cytoscape.getNetworkSet();
            for (CyNetwork net : allNetworks) {
                String netId = net.getIdentifier();
                String val = cyNetworkAttrs.getStringAttribute(netId, 
                                                     "ItmQueryPrefix");
                if ((val != null) && val.equals(queryPrefix)) {
                    Cytoscape.destroyNetwork(net);
                }
            }
            subNetwork = Cytoscape.createNetwork(title, false);
            cyNetworkAttrs.setAttribute(subNetwork.getIdentifier(),
                                "ItmQueryPrefix", queryPrefix);
        }        
        
	ArrayList nodeList = new ArrayList();
	for(String nodeId : selectedNodeIds) {
	    CyNode node = Cytoscape.getCyNode(nodeId, false);
	    subNetwork.addNode(node);
	    nodeList.add(node);
	}
        
        CsrAdjacencyMatrix M = new CsrAdjacencyMatrix(parentNetwork, 
                                                      subNetwork, 
                                                      queryEdgeTypes, 
                                                      weightAttr);
        
        selfAdjacent = new ArrayList<String>();

        CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
        
        List<Object []> edgeDataList = M.getAllEdgeWeights();                
        for (Object [] edgeData : M.getAllEdgeWeights()) {
            String srcNodeId = (String) edgeData[0];
            String destNodeId = (String) edgeData[1];
            Double fwd = (Double) edgeData[2];
            Double bwd = (Double) edgeData[3];
            
            CyNode srcNode = Cytoscape.getCyNode(srcNodeId, false);
            CyNode destNode = Cytoscape.getCyNode(destNodeId, false);
            
            if (srcNodeId.equals(destNodeId) && fwd > 0.0) {
                selfAdjacent.add(srcNodeId);
            }
            else {
                CyEdge edge = Cytoscape.getCyEdge(srcNode, destNode,
                                                  Semantics.INTERACTION,
                                                  queryPrefix + "Edge",
                                                  true);
                subNetwork.addEdge(edge);
                edgeAttributes.setAttribute(edge.getIdentifier(), 
                                            fwdEdgeAttr, fwd);
                edgeAttributes.setAttribute(edge.getIdentifier(), 
                                            bwdEgdeAttr, bwd);
            }
        }
    }

    private void showSubNetwork(boolean doLayout) {

        CyNetworkView outputView;
        String subNetworkId = subNetwork.getIdentifier();
        if (!Cytoscape.viewExists(subNetworkId)) {
            String title = "ITM Probe Query " + queryPrefix;
            outputView = Cytoscape.createNetworkView(subNetwork, title);
            doLayout = true;
        }
        else {
            outputView = Cytoscape.getNetworkView(subNetworkId);
        }
        if (doLayout) {
            CyLayoutAlgorithm layout = CyLayouts.getLayout("force-directed");
            LayoutProperties layoutProps = layout.getSettings();
            Tunable springLength = layoutProps.get("defaultSpringLength");
            if (springLength != null) {
                springLength.setValue(new Double(10.0));
            }
            layout.doLayout(outputView);
        }
    }

    protected abstract NodeSelector getNodeSelector(int criterionIndex, 
                                                    Double cutoffValue,
                                                    int maxNodes);

    public Object [] createSubNetworkView() {

        int attributeIndex = getDefaultRankingAttribute();
        Object [] data = getNodeTableData(attributeIndex, defaultNodeSelector,
                                          false);
        ArrayList<String> selectedNodeIds = (ArrayList<String>) data[0];
        createSubNetwork(selectedNodeIds);
        showSubNetwork(true);
        
        int [] coloringIndexes = new int [] {getDefaultColoringAttribute()};
        colorSubNetwork(coloringIndexes, 
                        getDefaultMonoScaling(),
                        getDefaultColormap());
        return data;
    }

    public Object [] updateSubNetworkView(int rankingIndex, 
                                          int criterionIndex,
                                          Double cutoffValue, 
                                          int maxNodes,
                                          int [] coloringIndexes,
                                          int scalingIndex, 
                                          int colormapIndex) {

        int attributeIndex = rankingIndex;
        NodeSelector selector = getNodeSelector(criterionIndex, cutoffValue,
                                                maxNodes);
        Object [] data = getNodeTableData(attributeIndex, selector,
                                          false);
        ArrayList<String> selectedNodeIds = (ArrayList<String>) data[0];
        List<CyNode> nodeList = subNetwork.nodesList();
        HashSet<String> newSet = new HashSet<String>(selectedNodeIds);
        HashSet<String> existingSet = new HashSet<String>();
        for (CyNode node : nodeList) {
            existingSet.add(node.getIdentifier());
        }
        if (!newSet.equals(existingSet)) {
            for (String nodeId : existingSet) {
                CyNode node = Cytoscape.getCyNode(nodeId, false);
                subNetwork.removeNode(node.getRootGraphIndex(), false);
            }
            createSubNetwork(selectedNodeIds);
        }
        showSubNetwork(true);
        colorSubNetwork(coloringIndexes, scalingIndex, colormapIndex);
        return data;

    }
    
    private void colorSubNetwork(int [] coloringIndexes,
                                 int scalingIndex, int colormapIndex) {
        
        String [] coloringAttrs = new String [coloringIndexes.length];
        for (int i=0; i < coloringIndexes.length; i++) {
            coloringAttrs[i] = getColAttributes().get(coloringIndexes[i]);
        }
        
        // get the VisualMappingManager and CalculatorCatalog
        VisualMappingManager manager = Cytoscape.getVisualMappingManager();
        CalculatorCatalog catalog = manager.getCalculatorCatalog();

        String vsName = queryPrefix + " Network Style";
        
        // check to see if a visual style with this name already exists
        VisualStyle visualStyle = catalog.getVisualStyle(vsName);
        if (visualStyle == null) {
                // if not, create it and add it to the catalog
                visualStyle = new VisualStyle(vsName);
                catalog.addVisualStyle(visualStyle);
        }
               
        VisualPropertyDependency propDep = visualStyle.getDependency();

        GlobalAppearanceCalculator globalAppCalc = visualStyle.getGlobalAppearanceCalculator();
        GlobalStyling globalStyling = new GlobalStyling();
        globalStyling.setGlobalAppearances(globalAppCalc);

        NodeAppearanceCalculator nodeAppCalc = visualStyle.getNodeAppearanceCalculator();
        if (coloringAttrs.length == 1) { 
            MonoNodeStyling monoNodeStyling = new MonoNodeStyling(subNetwork);
            monoNodeStyling.setParams(coloringAttrs[0], scalingIndex, 
                                      colormapIndex);
            monoNodeStyling.setNodeAppearances(nodeAppCalc, propDep);
            monoNodeStyling.setSpecialShapes(nodeAppCalc, sources, sinks,
                                             selfAdjacent);
        }
        else {
            MixedNodeStyling mixedNodeStyling = new MixedNodeStyling(subNetwork);
            mixedNodeStyling.setParams(coloringAttrs, scalingIndex);
            mixedNodeStyling.setNodeAppearances(nodeAppCalc, propDep);
            mixedNodeStyling.setSpecialShapes(nodeAppCalc, sources, sinks,
                                              selfAdjacent);
        }    
        
        EdgeAppearanceCalculator edgeAppCalc = visualStyle.getEdgeAppearanceCalculator();
        EdgeStyling edgeStyling = new EdgeStyling(subNetwork, 
                                                  fwdEdgeAttr, 
                                                  bwdEgdeAttr);
        edgeStyling.setEdgeAppearances(edgeAppCalc, propDep);
        
        
         // get the network and view
        String subNetworkId = subNetwork.getIdentifier();
        CyNetworkView outputView = Cytoscape.getNetworkView(subNetworkId);
        outputView.setVisualStyle(visualStyle.getName()); // not strictly necessary

        // actually apply the visual style
        manager.setVisualStyle(visualStyle);
        outputView.redrawGraph(true,true);

        
    }

    public void exportToTab(FileWriter writer) throws IOException {
        
        // Assume that there has been no changes after resetValidCols() was
        // called
        writeParams(writer, "INPUT PARAMETERS", getInputParameters(), null);
        writeParams(writer, "SUMMARY", getSummary(), getSummaryHeader());
        writeNodes(writer, "TOP RANKING NODES");
        writeExcluded(writer, "EXCLUDED NODES", getExcludedNodes());
        writeQueryData(writer, "QUERY DATA");
    }
    
    private void writeParams(FileWriter writer, String title, 
                             Vector<Vector> data, Vector<String> header)
                             throws IOException {

        writer.write(String.format("#\n# %s\n#\n", title));
        if (header != null) {
            writer.write(String.format("%s\t%s\n", 
                                       header.get(0), header.get(1)));
        }
        for (Vector line : data) {
            String key = (String) line.get(0);
            String val = (String) line.get(1);
            writer.write(String.format("%s\t%s\n", key, val));
        }        
    }
    
    private void writeExcluded(FileWriter writer, String title, String data)
                               throws IOException {
        
        writer.write(String.format("#\n# %s\n#\n", title));
        writer.write(data);
        writer.write("\n");        
    }
    
    private void writeNodes(FileWriter writer, String title)
                            throws IOException {
        
        int numNodes = parentNetwork.getNodeCount();
        MaxCountSelector selector = new MaxCountSelector(numNodes);
        int attributeIndex;

        // We try to sort on the last 'variable' attribute
        // If it cannot be found use the first existing attribute
        ArrayList<String> colAttributes = getColAttributes();
        String attr  = "";
        if (validVariables.isEmpty()){
            attributeIndex = 0;
        }
        else {        
            attributeIndex = validBoundary.size() + validVariables.size() - 1;
            attr = colAttributes.get(attributeIndex);
        }
                
        if (! hasNodeAttribute(attr)) {
            for (int i=0; i < colAttributes.size(); i++) {
                attr = colAttributes.get(i);
                if (hasNodeAttribute(attr)) {
                    attributeIndex = i;
                    break;
                }
            }
        }
                
        Object [] data = getNodeTableData(attributeIndex, selector, true);
        Vector<String> header = (Vector<String>) data[1];
        Vector<Vector> body = (Vector<Vector>) data[2];
        
        writer.write(String.format("#\n# %s\n#\n", title));
        writer.write(joinRow(header));
        for (Vector row : body) {
            List<String> fmtRow = formatRow(row);
            writer.write(joinRow(fmtRow));
        }        
    }
    
    private String joinRow(List<String> row) {        
        StringBuilder builder = new StringBuilder();
        builder.append(row.get(0));
        for (int i=1; i < row.size(); i++) {
            builder.append("\t");
            builder.append(row.get(i));
        }
        builder.append("\n");
        return builder.toString();
    }
    
    private List<String> formatRow(Vector row) {
        
        ArrayList<String> fmtRow = new ArrayList<String>();        
        fmtRow.add(String.format("%d", row.get(0))); // Rank
        fmtRow.add((String) row.get(1)); // Node ID
        fmtRow.add((String) row.get(2)); // Node Name
        for (int i=3; i < row.size(); i++) {
            fmtRow.add(String.format("%.2g", row.get(i)));
        }
        return fmtRow;        
    }
    
    private void writeQueryData(FileWriter writer, String title) 
                                throws IOException {
        
        writer.write(String.format("#\n# %s\n#\n", title));
        
        writer.write(MODEL_TAG);
        writer.write("\t");
        writer.write(getModelType());
        writer.write("\n");

        writer.write(joinDataRow(BOUNDARY_TAG, validBoundary));
        writer.write(joinDataRow(VARIABLE_TAG, validVariables));
        writer.write(joinDataRow(SOURCES_TAG, sources));
        writer.write(joinDataRow(SINKS_TAG, sinks));
        writer.write(joinDataRow(UNDIRECTED_TAG, 
                    new ArrayList<String>(queryEdgeTypes.undirected)));
        writer.write(joinDataRow(DIRECTED_TAG, 
                    new ArrayList<String>(queryEdgeTypes.directed)));
        writer.write(joinDataRow(IGNORED_TAG, 
                    new ArrayList<String>(queryEdgeTypes.ignored)));
        
        writer.write(WEIGHT_TAG);
        writer.write("\t");
        writer.write(weightAttr);
        writer.write("\n");
    }
    
    private String joinDataRow(String key, List<String> vals) {
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        if (vals == null || vals.isEmpty()) {
            builder.append("\n");
        }
        else {
            builder.append("\t");
            builder.append(joinRow(vals));
        }
        return builder.toString();        
    }
}