/**
 * Analysis provider interface
 */
package com.dscid.filesystemanalyzer;

import com.dscid.filesystemanalyzer.analyzers.FileAnalyzer;

public interface FileAnalysisProvider {
	public FileAnalyzer newAnalyzer();
}
