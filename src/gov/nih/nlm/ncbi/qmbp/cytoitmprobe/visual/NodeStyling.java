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


package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.visual;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.PassThroughMapping;
import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;


public abstract class NodeStyling {
    
    protected CyNetwork subNetwork;
    public String labelAttr = Semantics.CANONICAL_NAME;
    public String nodeIdAttr = Semantics.CANONICAL_NAME;

    public NodeStyling(CyNetwork subNetwork) {
        this.subNetwork = subNetwork;
    }

    public abstract void setNodeAppearances(NodeAppearanceCalculator nodeAppCalc,
                                            VisualPropertyDependency propDep);
    
    protected void setDefaults(NodeAppearanceCalculator nodeAppCalc,
                               VisualPropertyDependency propDep) {
        
        propDep.set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED, false);
        propDep.set(VisualPropertyDependency.Definition.NODE_LABLE_COLOR_FROM_NODE_COLOR, false);
        
        NodeAppearance defAppr = nodeAppCalc.getDefaultAppearance();        
        
        defAppr.set(VisualPropertyType.NODE_SHAPE, NodeShape.ROUND_RECT);
        defAppr.set(VisualPropertyType.NODE_TOOLTIP, "A node");
        defAppr.set(VisualPropertyType.NODE_LABEL, "NODE");
        defAppr.set(VisualPropertyType.NODE_LABEL_COLOR, Color.BLACK);
        defAppr.set(VisualPropertyType.NODE_FILL_COLOR, Color.WHITE);
        defAppr.set(VisualPropertyType.NODE_WIDTH, 35.0);
        defAppr.set(VisualPropertyType.NODE_HEIGHT, 22.0);
        defAppr.set(VisualPropertyType.NODE_BORDER_COLOR, new Color(102, 102, 102));
        defAppr.set(VisualPropertyType.NODE_LINE_WIDTH, 0.75);
        defAppr.set(VisualPropertyType.NODE_FONT_SIZE, 11.0);
    }

    protected void setNodesSize(NodeAppearanceCalculator nodeAppCalc) {
        
        DiscreteMapping dm = new DiscreteMapping(Double.class,
                                                 labelAttr);
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        for(Iterator<CyNode> nodeIter = subNetwork.nodesIterator(); nodeIter.hasNext();) {
	    CyNode node = nodeIter.next();
            String nodeId = node.getIdentifier();
            String nodeLabel = cyNodeAttrs.getStringAttribute(nodeId,
                                                              labelAttr);            
	    double nodeWidth = 2.5 + 10 * nodeLabel.length();
            dm.putMapValue(nodeLabel, new Double(nodeWidth));
            
	}
        Calculator nlc = new BasicCalculator("Node Width Calculator",
                                             dm, VisualPropertyType.NODE_WIDTH);
        nodeAppCalc.setCalculator(nlc);
    }

    protected void setNodesLabel(NodeAppearanceCalculator nodeAppCalc) {

        PassThroughMapping pm = new PassThroughMapping(String.class,
                                                       labelAttr);
        Calculator nlc = new BasicCalculator("Node Label Calculator",
                                             pm, VisualPropertyType.NODE_LABEL);
        nodeAppCalc.setCalculator(nlc);

        pm = new PassThroughMapping(String.class, labelAttr);
        nlc = new BasicCalculator("Node Tooltip Calculator",
                                  pm, VisualPropertyType.NODE_TOOLTIP);
        nodeAppCalc.setCalculator(nlc);
    }

    public void setSpecialShapes(NodeAppearanceCalculator nodeAppCalc,
                                 Collection<String> sources,
                                 Collection<String> sinks,
                                 Collection<String> selfAdjacent) {
        
        // Set shapes
        DiscreteMapping dm = new DiscreteMapping(NodeShape.class, nodeIdAttr);
        insertMappingItems(selfAdjacent, dm, NodeShape.ELLIPSE);       
        insertMappingItems(sources, dm, NodeShape.HEXAGON);
        insertMappingItems(sinks, dm, NodeShape.OCTAGON);
        Calculator nlc = new BasicCalculator("Node Shape Calculator",
                                             dm, VisualPropertyType.NODE_SHAPE);
        nodeAppCalc.setCalculator(nlc);
        
        // Set node heights
        dm = new DiscreteMapping(Number.class, nodeIdAttr);
        insertMappingItems(sources, dm, 35.0);
        insertMappingItems(sinks, dm, 35.0);
        nlc = new BasicCalculator("Node Height Calculator",
                                  dm, VisualPropertyType.NODE_HEIGHT);
        nodeAppCalc.setCalculator(nlc);
        
    }
    
    private void insertMappingItems(Collection<String> nodeIds, 
                                    DiscreteMapping dm,
                                    Object item) {
        if (nodeIds == null) {
            return;
        }
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();        
        for(String nodeId : nodeIds) {
            String nodeMapId = cyNodeAttrs.getStringAttribute(nodeId, nodeIdAttr);                                                                   
            dm.putMapValue(nodeMapId, item);            
	}        
    }

}
