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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class EdgeTypes {
    
    public Set<String> undirected;
    public Set<String> directed;
    public Set<String> ignored;

    public EdgeTypes(Set<String> undirected, 
                     Set<String> directed, 
                     Set<String> ignored) {
        this.undirected = undirected;
        this.directed = directed;
        this.ignored = ignored;
    }

    public EdgeTypes(Collection<String> undirected,
                     Collection<String> directed,
                     Collection<String> ignored) {
        
        this.undirected = makeSetWithNull(undirected);
        this.directed = makeSetWithNull(directed);
        this.ignored = makeSetWithNull(ignored);                
    }    

    private static Set<String> makeSetWithNull(Collection<String> c) {
        if (c == null) {
            return new HashSet<String>();
        }
        return new HashSet<String>(c);
    }
}
