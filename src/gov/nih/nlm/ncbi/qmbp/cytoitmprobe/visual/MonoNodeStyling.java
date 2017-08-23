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
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import java.awt.Color;
import java.util.Iterator;


public class MonoNodeStyling extends NodeStyling {

    private static Digitizer [] scalings = {new LogUpperDigitizer(),
                                            new LinearDigitizer()};
    private static int defaultScalingIndex = 0;
    private static int [][][] colormaps = BrewerColors.colormaps;
    private static String [] colormapNames = BrewerColors.colormapNames;
    private static int defaultColormapIndex = 0;

    public double minBrightness = 0.34;
    
    private String coloringAttr;
    private int scalingIndex;
    private int colormapIndex;
    
    
    public MonoNodeStyling(CyNetwork subNetwork) {
        super(subNetwork);
    }
    
    public void setParams(String coloringAttr,
                           int scalingIndex, int colormapIndex) {
        this.coloringAttr = coloringAttr;
        this.scalingIndex = scalingIndex;
        this.colormapIndex = colormapIndex;        
    }

    public static String [] getScalingNames() {
        String [] names = new String [scalings.length];
        for (int i=0; i < scalings.length; i++) {
            names[i] = scalings[i].getName();
        }
        return names;
    }

    public static int getDefaultScalingIndex() {
        return defaultScalingIndex;
    }
    
    public static String [] getColormapNames() {
        return colormapNames;
    }

    public static int getDefaultColormapIndex() {
        return defaultColormapIndex;
    }
       
    public void setNodeAppearances(NodeAppearanceCalculator nodeAppCalc,
                                   VisualPropertyDependency propDep) {
        
        setDefaults(nodeAppCalc, propDep);
        setNodesLabel(nodeAppCalc);
        setNodesSize(nodeAppCalc);
        setNodesColor(nodeAppCalc);
    }
    
    protected void setNodesColor(NodeAppearanceCalculator nodeAppCalc) {
        
        DiscreteMapping dm = new DiscreteMapping(Color.class, coloringAttr);
        DiscreteMapping dmf = new DiscreteMapping(Color.class, coloringAttr);
        
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        for(Iterator<CyNode> nodeIter = subNetwork.nodesIterator(); 
            nodeIter.hasNext();) {
	    CyNode node = nodeIter.next();
            String nodeId = node.getIdentifier();
            Double nodeVal= cyNodeAttrs.getDoubleAttribute(nodeId, coloringAttr);
            if (nodeVal == null) {
                continue;
            }
            int colorIndex = scalings[scalingIndex].digitize(nodeVal);
            int r = colormaps[colormapIndex][colorIndex][0];
            int g = colormaps[colormapIndex][colorIndex][1];
            int b = colormaps[colormapIndex][colorIndex][2];
            double brightness = (r + g + b) / 3.0 / 256.0;
            if (brightness < minBrightness) {
                dmf.putMapValue(nodeVal, Color.WHITE);
            }
            dm.putMapValue(nodeVal, new Color(r, g, b));
            
	}
        Calculator nlc = new BasicCalculator("Node Color Calculator",
                               dm, VisualPropertyType.NODE_FILL_COLOR);
        nodeAppCalc.setCalculator(nlc);
        nlc = new BasicCalculator("Node Text Color Calculator",
                                  dmf, 
                                  VisualPropertyType.NODE_LABEL_COLOR);
        nodeAppCalc.setCalculator(nlc);
    }
}
