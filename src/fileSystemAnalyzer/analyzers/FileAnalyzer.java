package fileSystemAnalyzer.analyzers;

import java.util.Map;

import fileSystemAnalyzer.Analyzer;
import fileSystemAnalyzer.FileContext;
import fileSystemAnalyzer.DB.DBLayer;

/**
 * @author felix
 */
public interface FileAnalyzer extends Analyzer<FileContext, FileContext>{
	void analyzeItem(FileContext context);
	void analyzeItems(FileContext context);
}
