package com.dscid.filesystemanalyzer.processors;

public enum SupportedProcessors {
  DUPLICATES,SIMILARITY;
  static public Processors getProcessor(SupportedProcessors p) {
    switch (p){
    case DUPLICATES:
      return Duplicates.INSTANCE;
    case SIMILARITY:
      return Similarity.INSTANCE;
    default:
      return null;
    }
  }
}
