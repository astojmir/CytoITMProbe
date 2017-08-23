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


package gov.nih.nlm.ncbi.qmbp.cytoitmprobe.config;

import cytoscape.CytoscapeInit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


public class Configuration {

    private static Properties config;

    private static String execPath;
    private static int count;
    private static String pathToFile;
    private static String url;
    private static Boolean localQuery;
    
    public static final String defaultUrl = 
        "https://www.ncbi.nlm.nih.gov/CBBresearch/Yu/mn/itm_probe/ITMProbe.cgi";

    public Configuration () throws InvalidConfigException{
	config = new Properties();

	String fileName = "ITMProbe.props";        
	File configFile = new File(CytoscapeInit.getConfigDirectory(), fileName);
        pathToFile = configFile.getPath();

	boolean loadConfig = true;
	while(loadConfig) {
	    try {
		loadConfiguration();
		loadConfig = false;
	    }
	    catch (FileNotFoundException e) {
		// Create a configuration file with the defaults.

		config.setProperty("execPath", "");
		config.setProperty("url", defaultUrl);
		config.setProperty("local", "false");
		config.setProperty("count", "0");

		// Write the file

		try {
		    FileOutputStream out = new FileOutputStream(pathToFile);
		    config.store(out, "Default values; automatically generated.");
		}
		catch (FileNotFoundException f) { /* Fail */ }
		catch (IOException f) { /* Fail */ }
	    }
	    catch (IOException e) {}
	}

	execPath = config.getProperty("execPath");
	url = config.getProperty("url");


	if (execPath == null) {
	    throw(new InvalidConfigException("'execPath' property not found."));
	}
	else if (url == null) {
	    throw(new InvalidConfigException("'url' property not found."));
	}

	try {
	    String countString = config.getProperty("count");
	    if (countString == null) {
		throw(new InvalidConfigException("'count' property not found."));
	    }
	    count = new Integer(config.getProperty("count"));
	}
	catch (NumberFormatException e) {
	    throw(new InvalidConfigException("Could not convert 'count' property to integer."));
	}

	String localString = config.getProperty("local");
	if (localString == null) {
	    throw(new InvalidConfigException("'local' property not found."));
	}
	localQuery = Boolean.valueOf(config.getProperty("local"));
    }

    public static void rewriteConfig(String execPath,
				     String url,
				     Boolean local){

	config.setProperty("execPath", execPath);
	config.setProperty("url", url);
	config.setProperty("local", local.toString());

	if (config.getProperty("count") == null) {
	    config.setProperty("count", "0");
	}

	try {
	    OutputStream out = new FileOutputStream(pathToFile);
	    config.store(out, null);
	}
	catch (FileNotFoundException e) {}
	catch (IOException e) {}


    }

    private static void loadConfiguration() throws FileNotFoundException,
						   IOException {
	InputStream readProp = new FileInputStream(pathToFile);
	config.load(readProp);
    }

    public static int getCount() {
	return count;
    }

    public static int incrementCount(){
	count++;

	if (count >= 1000) {
	    count = 1;
	}
	config.setProperty("count", new Integer(count).toString());
	try {
	    OutputStream writeProp = new FileOutputStream(pathToFile);

	    config.store(writeProp, null);
	}
	catch (FileNotFoundException e) {}
	catch (IOException e){}

	return count;
    }

    public static String getExecPath() {
	return execPath;
    }

    public static String getUrl() {
	return url;
    }

    public static Boolean isLocal() {
	return localQuery;
    }
}
