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
import cytoscape.data.Semantics;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.settings.EdgeTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CsrAdjacencyMatrix {
    
    private ArrayList<String> nodes;
    private ArrayList<Integer> indPtr;
    private ArrayList<Double> data;
    private ArrayList<Integer> indices;

    private static final String defaultEdgeWeightName = "NONE (Default)";

    public CsrAdjacencyMatrix() {

        indPtr  = new ArrayList<Integer>();
	data    = new ArrayList<Double>();
	indices = new ArrayList<Integer>();
	nodes	= new ArrayList<String>();

    }

    public CsrAdjacencyMatrix(CyNetwork parentNetwork,
                              CyNetwork subNetwork,
                              EdgeTypes edgeTypes,
			      String weightParameter) {
        this();
        
        int[] internalIndices = subNetwork.getNodeIndicesArray();
        initialize(parentNetwork, internalIndices, edgeTypes, weightParameter);        
    }
    
    public CsrAdjacencyMatrix(EdgeTypes edgeTypes,
			      String weightParameter) {
        this();      
        CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        int[] internalIndices = currentNetwork.getNodeIndicesArray();
        initialize(currentNetwork, internalIndices, edgeTypes, weightParameter);
    }

    public HashMap asMap() {
        HashMap out = new HashMap();
        out.put("nodes", nodes);
        out.put("indptr", indPtr);
        out.put("indices", indices);
        out.put("data", data);
	return out;
    }

    public List<Object []> getAllEdgeWeights() {
        
        class tmpEdge {
            public int src;
            public int dest;

            public tmpEdge(int src, int dest) {
                this.src = src;
                this.dest = dest;
            }

            @Override
            public int hashCode() {
                return src + (dest >> 0x8);
            }

            @Override
            public boolean equals(Object o) {
                tmpEdge other = (tmpEdge) o;
                return (src == other.src && dest == other.dest);
            }
            
            
            
        }
        
        class tmpWeights {
            public Double fwd;
            public Double bwd;

            public tmpWeights(Double fwd, Double bwd) {
                this.fwd = fwd;
                this.bwd = bwd;
            }
        }
                
        HashMap<tmpEdge, tmpWeights> em = new HashMap<tmpEdge, tmpWeights>();        
        for (int i=0; i < indPtr.size()-1; i++) {
            for (int k=indPtr.get(i); k < indPtr.get(i+1); k++){
                int j = indices.get(k);
                tmpEdge pair = new tmpEdge(j, i);
                if (em.containsKey(pair)) {
                    tmpWeights wghts = em.get(pair);
                    wghts.bwd = data.get(k);                    
                }
                else {
                    pair = new tmpEdge(i, j);
                    tmpWeights wghts = new tmpWeights(data.get(k), 0.0);
                    em.put(pair, wghts);
                }                
            }
        }
        
        ArrayList<Object []> edgeDataList = new ArrayList<Object []>();
        Set<Map.Entry<tmpEdge, tmpWeights>> emSet = em.entrySet();
        for (Map.Entry<tmpEdge, tmpWeights> entry : emSet) {
            tmpEdge pair = entry.getKey();
            tmpWeights wghts = entry.getValue();
            if (wghts.fwd > 0.0 || wghts.bwd > 0.0) {
                edgeDataList.add(new Object [] {nodes.get(pair.src),
                                                nodes.get(pair.dest),
                                                wghts.fwd,
                                                wghts.bwd});   
            }
        }
        return edgeDataList;
    }
    
    private void initialize(CyNetwork network,
                            int [] internalIndices,
                            EdgeTypes edgeTypes,
			    String weightParameter) {
        
        Set<String> ignoredEdges = edgeTypes.ignored;
        Set<String> directedEdges = edgeTypes.directed;
        Set<String> undirectedEdges = edgeTypes.undirected;

        if (weightParameter.equals(defaultEdgeWeightName)) {
            weightParameter = null;
        }
        
	HashMap<Integer,Integer> rootIxToMatIx = new HashMap<Integer,Integer>();
	CyAttributes attributes = Cytoscape.getEdgeAttributes();
	int iptr = 0;

        // Collect node identifiers and map node root indices to our matrix indices
	for (int i = 0; i < internalIndices.length; i++) {
            int currentNodeRootIx = internalIndices[i];
	    CyNode currentNode = (CyNode) network.getNode(currentNodeRootIx);
	    String currentNodeName = currentNode.getIdentifier();
	    nodes.add(currentNodeName);
	    rootIxToMatIx.put(currentNodeRootIx, i);
	}

        // Main loop: construct compressed sparse arrays
        indPtr.add(iptr);

	for (int i = 0; i < internalIndices.length; i++) {

            int currentNodeRootIx = internalIndices[i];
	    HashMap<Integer, Double> neighborToWeight = new HashMap<Integer, Double>();

            neighborToWeight.put(i, 0.0); // Insert diagonal entry
	    int[] adjacentEdges =
		network.getAdjacentEdgeIndicesArray(currentNodeRootIx,
							 true,  // Get undirected edges
							 true, // Get incoming edges
							 true);  // Get outgoing edges


            // Get total weight of edges to each neighbor
	    for(int k = 0; k < adjacentEdges.length; k++) {

		CyEdge edge = (CyEdge) network.getEdge(adjacentEdges[k]);
                int sourceNodeRootIx = edge.getSource().getRootGraphIndex();
                int targetNodeRootIx = edge.getTarget().getRootGraphIndex();

                // Check directedness of the edge
                String edgeType = (String)attributes.getAttribute(edge.getIdentifier(),
                                                                  Semantics.INTERACTION);
                boolean skippedEdge = 
                    !rootIxToMatIx.containsKey(sourceNodeRootIx) ||
                    !rootIxToMatIx.containsKey(targetNodeRootIx) ||    
                    ignoredEdges.contains(edgeType) ||
                    (directedEdges.contains(edgeType) && 
                     currentNodeRootIx != sourceNodeRootIx);
                if ( !skippedEdge) {
                    int otherNodeRootIx = targetNodeRootIx;
                    if (otherNodeRootIx == currentNodeRootIx) {
                        otherNodeRootIx = sourceNodeRootIx;
                    }

                    int j = rootIxToMatIx.get(otherNodeRootIx);

                    Double edgeWeight;
                    if (weightParameter == null) {
                        if (i == j) {
                            edgeWeight = 2.0;
                        }
                        else {
                            edgeWeight = 1.0;
                        }
                    }
                    else {
                        String edgeId = edge.getIdentifier();
                        edgeWeight = attributes.getDoubleAttribute(edgeId,
                                                                   weightParameter); 
                    }

                    if (edgeWeight != null) {
                        Double oldWeight = neighborToWeight.get(j);
                        if (oldWeight == null) {
                            oldWeight = 0.0;
                        }

                        neighborToWeight.put(j, oldWeight + edgeWeight);
                    }
                }
	    }

            // Now store sorted indices and weights
            ArrayList<Integer> keys = new ArrayList(neighborToWeight.keySet());
            Collections.sort(keys);

            for (int j : keys) {
                iptr++;
                indices.add(j);
                data.add(neighborToWeight.get(j));
            }
            indPtr.add(iptr);
	}        
    }
    
}
