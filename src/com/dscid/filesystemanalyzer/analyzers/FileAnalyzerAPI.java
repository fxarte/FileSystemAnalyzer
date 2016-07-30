package com.dscid.filesystemanalyzer.analyzers;

import java.util.LinkedHashSet;
import java.util.Set;

//TODO: This may be the API for the whole package
// Exposes the analyzers that can be used by client code as name strings?
// The methods will be static and public, the only ones to be public in the package?
// Or receives request for analysis using File, Analyzer parameters also through static calls
public enum FileAnalyzerAPI {
	INSTANCE;
	Set<FileAnalyzer> al = new LinkedHashSet<FileAnalyzer>();
	public Set<FileAnalyzer> getAnalyzerProviders() {
		
		al.add(ItemCore.INSTANCE);
		
		al.add(ItemSize.INSTANCE);
		al.add(ItemHash.INSTANCE);
//		al.add(PrimeBasedIdentity.INSTANCE);
		return al;
	}
	
	/**
	 * register the providers 
	 */
	void setAnalyzerProviders() {
		
	}
}
