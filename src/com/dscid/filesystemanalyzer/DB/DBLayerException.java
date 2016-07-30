package com.dscid.filesystemanalyzer.DB;

import com.almworks.sqlite4java.SQLiteException;

public class DBLayerException extends Exception {
	//
	public final static String NO_DB_FOUND = "NO fileSystemAnalyzer.DB found for that Plugin";
	public final static String INSTANCE_ALREADY_EXISTS = "The instance been created already exists";
	public final static String INSTANCE_DOESNOT_EXISTS = "The instance DOES NOT exists";
	public static final String UNABLE_TO_CREATE_DBLAYER_INSTANCE = "UNABLE_TO_CREATE_DBLAYER_INSTANCE";
	public DBLayerException(String message) {
        super(message);
    }
	public DBLayerException(String message,
			SQLiteException e) {
        super(message, e);
	}
}
