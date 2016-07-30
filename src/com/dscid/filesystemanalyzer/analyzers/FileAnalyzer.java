package com.dscid.filesystemanalyzer.analyzers;

import java.util.Map;

import com.dscid.filesystemanalyzer.Analyzer;
import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.DB.DBLayer;

/**
 * @author felix
 */
public interface FileAnalyzer extends Analyzer<FileContext, FileContext>{
	void analyzeItem(FileContext context);
	void analyzeItems(FileContext context);
}
