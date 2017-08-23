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


public class LogUpperDigitizer extends Digitizer {
    
    private double a;
    public double b = 0.0;
    public double step = 0.8;
    public double base = 2.0;

    public LogUpperDigitizer() {
        super();
        resetBounds();
    }
    
    @Override
    public void resetBounds() {

        int n = numBins-1;
        bounds = new double [n];
        a = b - step * numBins;
        for (int k=0; k < n; k++) {
            bounds[k] = Math.pow(base, a + step * (k+1));
        }        
    }

    @Override
    public String getName() {
        return "Logarithmic";
    }
    
}
