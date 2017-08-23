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

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;


public class ListManager {
    public static void loadExclusions(JList exclusionsList, File exclusionsFile) {
	Scanner fileScanner;
	DefaultListModel model = new DefaultListModel();
        
        // Prepare a map of canonical names to node IDs. This will obviously
        // not work if canonical names are not unique       
        CyNetwork network = Cytoscape.getCurrentNetwork();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        HashMap<String, String> nameToId = new HashMap<String, String>();
        HashSet<String> nodeIds = new HashSet<String>();
        
        // Could have used nodesIterator here but this also works
        int[] internalIndices = network.getNodeIndicesArray();
	for (int i = 0; i < internalIndices.length; i++) {
            int currentNodeRootIx = internalIndices[i];
	    CyNode node = (CyNode) network.getNode(currentNodeRootIx);
	    String nodeId = node.getIdentifier();
            String nodeName = cyNodeAttrs.getStringAttribute(nodeId,
                                Semantics.CANONICAL_NAME);
            nameToId.put(nodeName, nodeId);
            nodeIds.add(nodeId);
	}
              
	try {
	    fileScanner = new Scanner(exclusionsFile);
	    fileScanner.useDelimiter("[\\s,:]+");

	    while(fileScanner.hasNext()) {
                String nodeName = fileScanner.next();
                // Check if the node with this ID exists in the current network
                // or a node with such cannonicalName exists
                if (nodeIds.contains(nodeName)) {
                    model.addElement(nodeName);
                }
                else if (nameToId.containsKey(nodeName)) {
                    model.addElement(nameToId.get(nodeName));
                }
	    }
	    exclusionsList.setModel(model);
	}
	catch (FileNotFoundException e) {}
    }

    public static void appendSet(JList list, Set<String> set){
	HashSet<String> newSet = new HashSet<String>(set);
	ListModel oldModel = list.getModel();
	DefaultListModel model;

	// Get all entries from the previous list model and
	// put them into the new set.

	for (int i = 0; i < oldModel.getSize(); i++) {
	    newSet.add((String) oldModel.getElementAt(i));
	}

	model = new DefaultListModel();
        for (String name : newSet) {
	    model.addElement(name);            
        }
	list.setModel(model);
    }

    public static void removeNodes(JList list){
	DefaultListModel model = (DefaultListModel)list.getModel();
	int[] selectedIndices = list.getSelectedIndices();

	// Remove selected items in decreasing order.
	for (int i = selectedIndices.length-1; i >= 0; i--) {
	    model.remove(selectedIndices[i]);
	}
	list.setModel(model);
    }

    public static ArrayList<String> getNodesFromList(ListModel model) {
	ArrayList<String> nodes = new ArrayList();
	for (int i = 0; i < model.getSize(); i++) {
	    String element = (String)model.getElementAt(i);
	    nodes.add(element);
	}
	return nodes;
    }

    public static HashMap<String, Double> getAntiSinks(ListModel model) {
	HashMap<String, Double> antiSinks = new HashMap<String, Double>();
	ArrayList<String> nodes = getNodesFromList(model);
        for (String nodeId : nodes) {
            antiSinks.put(nodeId, new Double(0.0));            
        }
	return antiSinks;
    }
}
