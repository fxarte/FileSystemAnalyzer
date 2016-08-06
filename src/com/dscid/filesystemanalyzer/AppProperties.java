/**
 * 
 */
package com.dscid.filesystemanalyzer;

/**
 * @author felix
 * Singleton for the App Properties
 */
public class AppProperties {
	private static AppProperties instance = null;
	protected AppProperties() {
		
	}
	public static AppProperties getInstance() {
		if (instance == null) {
			instance = new AppProperties();
		}
		return instance;
	}
}
