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

package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.CyAttributesUtils;
import cytoscape.data.Semantics;
import giny.model.Node;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.EdgeTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GraphInteraction {
    
    private static final String defaultEdgeWeightName = "NONE (Default)";
    
    public static Set<String> getSelectedNodes() {

	CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
	Set<Node> nodesSet = currentNetwork.getSelectedNodes();
	HashSet<String> nodeNames = new HashSet();

	Iterator<Node> nodesIter = nodesSet.iterator();

	while (nodesIter.hasNext()) {
	    nodeNames.add(nodesIter.next().getIdentifier());
	}
	return nodeNames;
    }

    public static Set<String> getEdgeTypes(CyNetwork currentNetwork) {
	CyAttributes attributes = Cytoscape.getEdgeAttributes();
	Iterator<CyEdge> edgeIter = currentNetwork.edgesIterator();
	HashSet edgeTypes = new HashSet();

	while(edgeIter.hasNext()) {
	    CyEdge edge = edgeIter.next();
	    String id = edge.getIdentifier();
	    String edgeType = (String) attributes.getAttribute(id,
		                         Semantics.INTERACTION);
	    edgeTypes.add(edgeType);
	}
	return edgeTypes;
    }

    
    public static List<String> getEdgeAttributeNames(CyNetwork currentNetwork) {

        CyAttributes attributes = Cytoscape.getEdgeAttributes();
        ArrayList<String> allNames = new ArrayList<String>();
	Iterator<CyEdge> edgeIter = currentNetwork.edgesIterator();
	while(edgeIter.hasNext()) {
	    String edgeId = edgeIter.next().getIdentifier();
	    List<String> tmp = CyAttributesUtils.getAttributeNamesForObj(edgeId,
									 attributes);
	    allNames.addAll(tmp);
	}
              
        HashSet<String> usedNames = new HashSet<String>();
        Iterator<String> iter = allNames.iterator();
	while (iter.hasNext()) {
	    String attribute = iter.next();
	    if (attributes.getType(attribute) == CyAttributes.TYPE_FLOATING) {
		usedNames.add(attribute);
	    }
	}
        ArrayList<String> validNames = new ArrayList<String>();
	validNames.add(defaultEdgeWeightName);
        validNames.addAll(usedNames);
        return validNames;
    }
    
    public static Boolean verifyNode(String name) {
	CyNode node = Cytoscape.getCyNode(name);

	if (node != null)
	    return true;
	else
	    return false;
    }

    public static HashMap createCsr(EdgeTypes edgeTypes,
			            String weightParameter) {
        
        CsrAdjacencyMatrix M = new CsrAdjacencyMatrix(edgeTypes, 
                                                      weightParameter);
        return M.asMap();
    }
}
