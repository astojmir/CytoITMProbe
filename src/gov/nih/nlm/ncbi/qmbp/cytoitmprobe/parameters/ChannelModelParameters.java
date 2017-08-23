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

package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.parameters;

import java.util.List;
import java.util.Map;

public class ChannelModelParameters extends GenericModelParameters{

    private Double da;
    private Double dr;
    private List source_nodes;
    private List sink_nodes;

    public ChannelModelParameters(List sinks,
				  List sources,
				  Map antisinks,
				  Map graph,
				  Double df,
				  Double da,
				  Double dr) {

	super(antisinks, graph,
	      df, "normalized-channel");
	this.source_nodes = sources;
	this.sink_nodes = sinks;
	this.da = da;
	this.dr = dr;
    }
}
