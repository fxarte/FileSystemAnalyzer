/**
 * Analysis provider interface
 */
package fileSystemAnalyzer;

import fileSystemAnalyzer.analyzers.FileAnalyzer;

public interface FileAnalysisProvider {
	public FileAnalyzer newAnalyzer();
}
