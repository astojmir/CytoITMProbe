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

package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public abstract class InputSettings {
    
    
    public abstract String buildPrefix();
    
    public abstract List<Integer> filterBoundaryCols(List<String> header);
    
    public abstract List<Integer> filterSummaryCols(List<String> header);
    
    public abstract List<Integer> filterCustomCols(List<String> header);
    
    
    
    protected static HashMap<String, ArrayList<ArrayList>> parseOutput(String itmProbeOutput) {

	HashMap<String, ArrayList<ArrayList>> parsedOutput = new HashMap<String, ArrayList<ArrayList>>();
	ArrayList<ArrayList> sectionList = null;

	String[] lines = itmProbeOutput.split("\n");
	String currentSectionName = new String();
	String currentSectionBuf = new String();
	int hashCount = 0;

	for(String line : lines) {
	    if (line.startsWith("#")){
		hashCount++;
		if (hashCount == 2) {
		    if (sectionList != null){
			// Add the previous section to output
			// only if this current section is not
			// the first one.
			parsedOutput.put(currentSectionName, sectionList);
		    }
		    // This cuts the # from the line
		    currentSectionBuf = line.substring(2);
		}
		else if (hashCount == 3) {
		    currentSectionName = currentSectionBuf;
		    sectionList = new ArrayList<ArrayList>();
		}
		else if (hashCount == 4) {
                    hashCount = 1;
                }
	    }
	    else {
		hashCount = 0;
		List<String> lineList = Arrays.asList(line.split("\t"));
		sectionList.add(new ArrayList<String>(lineList));
	    }
	}

	// After we reach the end of the output, we need to
	// manually add the last vector.
	parsedOutput.put(currentSectionName, sectionList);
	return parsedOutput;
    }

    protected String setNetworkAttrs(HashMap<String, ArrayList<ArrayList>> data,
                                     CyNetwork parentNetwork,
                                     List<String> sources,
                                     List<String> sinks,
                                     EdgeTypes currentEdgeTypes,
                                     String weightAttr) {

        CyAttributes netAttrs = Cytoscape.getNetworkAttributes();

        // Get main section - node results    
        ArrayList<ArrayList> section = data.get("TOP SCORING NODES (PER SOURCE)");
        if (section == null) {
            section = data.get("TOP RANKING NODES");
        }

	// The first line is the header information.
        ArrayList<String> header = section.get(0);
        
        // MODEL SPECIFIC - filtered columns plus queryPrefix
        // Extract filtered columns - others are ignored
        List<Integer> boundaryIxs = filterBoundaryCols(header);
        List<Integer> varsIxs = filterSummaryCols(header);
        List<Integer> customIxs = filterCustomCols(header);
        String queryPrefix = buildPrefix();
                
        netAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                  String.format("%sB00", queryPrefix),
                                  extractItems(header, boundaryIxs));       
        netAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                  String.format("%sV00", queryPrefix),
                                  extractItems(header, varsIxs));       
        fillNodeAttributes(parentNetwork, section, header, boundaryIxs, queryPrefix);
        fillNodeAttributes(parentNetwork, section, header, varsIxs, queryPrefix);
        fillNodeAttributes(parentNetwork, section, header, customIxs, queryPrefix);
                        
        // Summary, Input parameters as Network attributes
	ArrayList<ArrayList> summaryVector = data.get("SUMMARY");
        summaryVector.remove(0); // remove header

        String excludedNodes;
        ArrayList<ArrayList> excludedLines = data.get("EXCLUDED NODES");
        if (excludedLines == null) {
            excludedNodes = "";
        }
        else {
            excludedNodes =  (String) excludedLines.get(0).get(0);
        }      
        
        setParamsAsNetworkAttrs(summaryVector, 'S', parentNetwork, queryPrefix);
        setParamsAsNetworkAttrs(data.get("INPUT PARAMETERS"), 'I', 
                                parentNetwork, queryPrefix);
        
        netAttrs.setAttribute(parentNetwork.getIdentifier(), 
                              String.format("%sE00", queryPrefix),
                              excludedNodes);
        if (sources != null) {
            netAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                      String.format("%sB01", queryPrefix),
                                      sources);       
        }
        if (sinks != null) {
            netAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                      String.format("%sB02", queryPrefix),
                                      sinks);
        }
        netAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                  String.format("%sB03", queryPrefix),
                                  new ArrayList(currentEdgeTypes.undirected));
        netAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                  String.format("%sB04", queryPrefix),
                                  new ArrayList(currentEdgeTypes.directed));
        netAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                  String.format("%sB05", queryPrefix),
                                  new ArrayList(currentEdgeTypes.ignored));
        netAttrs.setAttribute(parentNetwork.getIdentifier(), 
                              String.format("%sB06", queryPrefix),
                              weightAttr);


        return queryPrefix;
    }

    private static ArrayList extractItems(ArrayList x, List<Integer> indices) {
        ArrayList y = new ArrayList(indices.size());
        for (Integer j : indices) {
            y.add(x.get(j));
        }
        return y;
    }
    
    private static void fillNodeAttributes(CyNetwork parentNetwork,
                                           ArrayList<ArrayList> section, 
                                           ArrayList<String> header,
                                           List<Integer> indices,
                                           String queryPrefix) {

       	CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        for (ArrayList<String> line : section.subList(1, section.size())) {
                String nodeId = line.get(1);
                CyNode node = Cytoscape.getCyNode(nodeId, false);
                if (node == null || !parentNetwork.containsNode(node)) {
                    continue;
                }
                for (Integer k : indices) {
                    String attr = String.format("%s[%s]", 
                                                queryPrefix, 
                                                header.get(k));
                    cyNodeAttrs.setAttribute(nodeId, attr, 
                                             new Double(line.get(k)));                   
                }
        }       
    }
    
    private static void setParamsAsNetworkAttrs(List paramData, 
                                                char paramType,
                                                CyNetwork parentNetwork,
                                                String queryPrefix) {
        
        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();
        for (int i=0; i < paramData.size(); i++) {
            String attrName = String.format("%s%c%02d", queryPrefix, 
                                            paramType, i);
            cyNetworkAttrs.setListAttribute(parentNetwork.getIdentifier(), 
                                            attrName, 
                                            (List) paramData.get(i));            
        }       
    }

    
    
}
