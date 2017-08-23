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


public abstract class Digitizer {

    protected double [] bounds;
    public int numBins = 8;

    public int digitize(Double val) {
        int n = bounds.length;
        if (val == null || val <= bounds[0]) {
            return 0;            
        }
        if (val > bounds[n-1]) {
                return n;
        }
        int a = 0;
        int b = n-1;
        do {
            int c = (a+b) / 2;
            if (val > bounds[c]) {
                a = c;                
            }
            else {
                b = c;
            }
        }
        while (a < (b-1));        
        return b;
    }

    public double[] getBounds() {
        return bounds;
    }
    
    public abstract String getName();
    
    public abstract void resetBounds();
}
