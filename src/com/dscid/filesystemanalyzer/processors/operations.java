package com.dscid.filesystemanalyzer.processors;

public enum operations {
  Win7(OS.Win7), Linux(OS.Linux);
  final OS os;

  operations(OS os) {
    this.os = os;
  }

  public String getMove(String path) {
    switch (this.os) {
    case Win7:
      return "move % ";
    }
    return "";
  }
}
