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

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import java.awt.Color;
import java.util.Iterator;


public class EdgeStyling {
    
    protected CyNetwork subNetwork;
    protected String fwdAttr;
    protected String bwdAttr;

    
    public String edgeIdAttr = Semantics.CANONICAL_NAME;

    public EdgeStyling(CyNetwork subNetwork, String fwdAttr, String bwdAttr) {
        this.subNetwork = subNetwork;
        this.fwdAttr = fwdAttr;
        this.bwdAttr = bwdAttr;
    }
    
    public void setEdgeAppearances(EdgeAppearanceCalculator edgeAppCalc,
                                            VisualPropertyDependency propDep) {
        setDefaults(edgeAppCalc, propDep);
        setDirectional(edgeAppCalc);
    }    
    
    protected void setDefaults(EdgeAppearanceCalculator edgeAppCalc,
                               VisualPropertyDependency propDep) {
        propDep.set(VisualPropertyDependency.Definition.ARROW_COLOR_MATCHES_EDGE,
                   true);
        EdgeAppearance defAppr = edgeAppCalc.getDefaultAppearance();        
        defAppr.set(VisualPropertyType.EDGE_COLOR, new Color(102, 102, 102));        
        defAppr.set(VisualPropertyType.EDGE_LINE_WIDTH, 0.75);        
    }
    
    protected void setDirectional(EdgeAppearanceCalculator edgeAppCalc) {
        
        DiscreteMapping dmf = new DiscreteMapping(ArrowShape.class, edgeIdAttr);
        DiscreteMapping dmb = new DiscreteMapping(ArrowShape.class, edgeIdAttr);
        
        
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        for(Iterator<CyEdge> edgeIter = subNetwork.edgesIterator(); 
                             edgeIter.hasNext();) {
	    CyEdge edge = edgeIter.next();
            String edgeId = edge.getIdentifier();
            String edgeMapId = cyEdgeAttrs.getStringAttribute(edgeId,
                                                              edgeIdAttr);
            Double fwd = cyEdgeAttrs.getDoubleAttribute(edgeId, fwdAttr);
            Double bwd = cyEdgeAttrs.getDoubleAttribute(edgeId, bwdAttr);

            if (fwd == 0.0 || bwd == 0.0) {
                if (bwd == 0.0) {
                    dmf.putMapValue(edgeMapId, ArrowShape.ARROW);
                }
                else {
                    dmb.putMapValue(edgeMapId, ArrowShape.ARROW);                    
                }
            }
	}
        Calculator nlcb = new BasicCalculator("Edge Source Arrow Shape",
                                  dmb, VisualPropertyType.EDGE_SRCARROW_SHAPE);
        Calculator nlcf = new BasicCalculator("Edge Target Arrow Shape",
                                  dmf, VisualPropertyType.EDGE_TGTARROW_SHAPE);
        edgeAppCalc.setCalculator(nlcb);
        edgeAppCalc.setCalculator(nlcf);     
    }
}
