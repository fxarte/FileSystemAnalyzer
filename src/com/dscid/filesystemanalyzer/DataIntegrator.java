package com.dscid.filesystemanalyzer;

import java.util.Map;

public abstract class DataIntegrator implements Analyzer {
	public abstract void synthetise();
	
	String toolName = "Default Data Integrator, useless";
	public String getToolName() {
		return this.toolName;
	}

	String toolGreeting = "Default Data Integrator, useless";
	public String getToolGreeting() {
		return this.toolName;
	}
	
	public Map<String, String> processDirectory(String dirPath, String parentDir) {
		// TODO Auto-generated method stub
		return null;
	}

	public void processDirectory(String dirPath) {
		// TODO Auto-generated method stub
	}

	public Boolean checkDB() {
		// TODO Auto-generated method stub
		return null;
	}

	public String checkDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * output message
	 */
	@Override
	public abstract String toString();
}
