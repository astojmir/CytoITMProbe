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


import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.config.Configuration;
import gov.nih.nlm.ncbi.qmbp.cytoitmprobe.config.InvalidConfigException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class ItmProbeTools {

    public static String itmProbeRun(String jsonInput) {
	
        String output = checkConfig();
        if (output == null) {
            if (Configuration.isLocal()) {
                output = localItmProbeRun(jsonInput);
            } else {
                output = webItmProbeRun(jsonInput);
            }
            output = checkOutput(output);

        }
	return output;
    }

    public static String checkConfig() {
        
        // Validate configuration
        Configuration config;
	try {
	    config = new Configuration();
	}
	catch (InvalidConfigException e) {
	    String message =
		"ERROR: There is a problem with your ITM Probe configuration file." +
		"\n\nYou may fix this either by editing the file by hand\n" +
		"or by clicking the 'CONFIG' button and saving your changes." +
		"\n\nThe following error occured: \n";
            return message + e.getMessage();
	}
        return null;
    }
    
    private static String checkOutput(String output) {

        if (output.length() == 0) {
	    String message =
		"ERROR: No output from ITM probe. \n\n" +
		"Please verify your configuration\nand input parameters.";
	    return message;
	}
        Configuration.incrementCount();
        return output;
    }
    

    
    public static String localItmProbeRun(String jsonInput) {

	Process itmProbeExec = null;
	int exitValue;

	PrintWriter outStream = null;

	BufferedReader inStream = null;
	BufferedReader errStream = null;

	StringBuffer outputBuffer = new StringBuffer();
	String PATH_TO_EXEC = Configuration.getExecPath();
	String lineRead;

	try {
	    itmProbeExec = Runtime.getRuntime().exec(PATH_TO_EXEC +
						   " standalone-run");
	}
	catch(IOException e) {
	    outputBuffer.append("ERROR: ").append(e.getMessage());
	    return outputBuffer.toString();
	}

	try {

	    inStream = new BufferedReader(new InputStreamReader
					  (itmProbeExec.getInputStream()));
	    errStream = new BufferedReader(new InputStreamReader
					   (itmProbeExec.getErrorStream()));
	    outStream = new PrintWriter(itmProbeExec.getOutputStream());

	    outStream.print(jsonInput);

	    outStream.flush();
	    outStream.close();

	    while (true) {
		lineRead = inStream.readLine();

		if (lineRead == null) {
		    break;
		}

		outputBuffer.append(lineRead).append("\n");
	    }

	    lineRead = errStream.readLine();

	    if (lineRead != null) {
		outputBuffer = new StringBuffer();
		outputBuffer.append("ERROR:");
		outputBuffer.append(lineRead).append("\n");

		while(true) {
		    lineRead = errStream.readLine();
		    if (lineRead == null) {
			break;
		    }
		    outputBuffer.append(lineRead).append("\n");
		}

	    }
	}

	catch (IOException e) {
	    outputBuffer = new StringBuffer();
	    outputBuffer.append("ERROR: Could not communicate with the program.");
	}

	return outputBuffer.toString();

    }

    public static byte[] gzipString(String jsonInput) {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	GZIPOutputStream gzos = null;

	try {
	    gzos = new GZIPOutputStream(baos);
	    gzos.write(jsonInput.getBytes("UTF-8"));
	}
	catch (Exception ignore) {}
	finally {
	    if (gzos != null) {
		try {
		    gzos.close();
		}
		catch (IOException ignore) {}
	    }
	}


	byte[] jsonGzippedBytes = baos.toByteArray();

	return jsonGzippedBytes;
    }

    public static String webItmProbeRun(String jsonInput) {

	byte[] jsonGzippedBytes = gzipString(jsonInput);

	try {
	    MultipartEntity entity = new MultipartEntity();
	    entity.addPart("input_data", new ByteArrayBody(jsonGzippedBytes,
							   "inputfile.gz"));
	    try {
		entity.addPart("view", new StringBody("5"));
	    } catch (Exception i){}

	    HttpPost post = new HttpPost(Configuration.getUrl());
	    post.setEntity(entity);

	    HttpClient client = new DefaultHttpClient();


	    HttpResponse response = client.execute(post);

	    HttpEntity responseEntity = response.getEntity();
	    GzipDecompressingEntity gzEntity = new GzipDecompressingEntity(responseEntity);

	    StringBuilder output = new StringBuilder();

	    if (gzEntity != null) {
		BufferedReader queryResponse = new BufferedReader(new InputStreamReader(gzEntity.getContent()));


		String buf;

		while((buf = queryResponse.readLine()) != null) {
		    output.append(buf).append("\n");
		}
		return output.toString();
	    }

	}
	catch (IOException e) {}
	return new String();
    }
}


