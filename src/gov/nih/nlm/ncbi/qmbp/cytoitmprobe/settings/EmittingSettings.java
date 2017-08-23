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

import java.util.List;
import java.util.Set;
import java.util.Map;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.config.Configuration;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.parameters.EmittingModelParameters;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.tools.GraphInteraction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;


public class EmittingSettings extends ModelSettings {

    private static final int DF = 0;
    private static final int DA = 1;

    public EmittingSettings() {
        modelName = "Emitting";
        hasSinks = false;
        hasSources = true;
        dampingParamNames = new String [] 
                            {"Termination (dissipating) probability",
                             "Average path length"};
        dampingParamDefaults = new String [] {"0.15", "5.0"};   
    }

    @Override
    public String buildPrefix() {
        return String.format("ITME%03d", Configuration.getCount());
    }

    @Override
    public List<Integer> filterBoundaryCols(List<String> header) {
        ArrayList<Integer> boundary = new ArrayList<Integer>(header.size()-4);
        for (int i=2; i < header.size()-2; i++) {
            boundary.add(i);
        }
        return boundary;
    }

    @Override
    public List<Integer> filterSummaryCols(List<String> header) {
        ArrayList<Integer> vars = new ArrayList<Integer>();
        if (header.size() != 5) {
            vars.add(header.size()-2);
            vars.add(header.size()-1);
        }
        return vars;
    }
    
    @Override
    public String getJsonParams(String weightAttr, 
                                EdgeTypes currentEdgeTypes, 
                                List<String> sources, 
                                List<String> sinks, 
                                Map<String, Double> antisinks, 
                                int dampingIndex, 
                                String dampingValue) {
        
        Gson gson = new GsonBuilder().serializeNulls().create();
        EmittingModelParameters params;

        Double df = null;
        Double da = null;
        Map graph = GraphInteraction.createCsr(currentEdgeTypes, weightAttr);
        Double val = new Double(dampingValue);

        if (dampingIndex == DF) {
            df = 1.0 - val;
        }
        else if (dampingIndex == DA) {
            da = val;
        }

        params = new EmittingModelParameters(sources, antisinks, graph, df, da);
        return gson.toJson(params);
    }

    @Override
    public String validate(String weightAttr, 
                           Set<String> currentEdgeTypes, 
                           List<String> sources, 
                           List<String> sinks, 
                           Map<String, Double> antisinks, 
                           int dampingIndex, 
                           String dampingValString) {

        String msg = validateGraphParams(weightAttr, currentEdgeTypes, sources, 
                                         null, antisinks);
        if (msg != null) {
            return msg;
        }
                
        if (sources.isEmpty()) {
            return "You must select at least one source.";
        }
        
        Double val = validateDampingValue(dampingValString);
        if (val == null) {
            return "Invalid dissipation criterion value.";
        }

        switch(dampingIndex) {
            case DF:
                if (val > 1.0 || val < 0.0) {
                    return "Termination probability must be between 0 and 1.0.";
                }
                break;
            case DA:
                if (val <= 0.0) {
                    return "Path length units must be positive.";
                }
                break;
        }
        return null;
    }
    
    
}
