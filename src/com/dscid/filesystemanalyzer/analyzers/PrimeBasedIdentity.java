/*
 * analyzer:
 * store each line hash in the DB (dictionary DB), no duplicates, ,return reference INT
 * per TXT file: get list of line hashes path|SUM hash dictionary reference (INT), store value as rounded to the previous prime plus difference to actual value (DB strucure: path=previousPrime, path=path;value=sum of references - prime(parent))
 * Compare similarity:
 * get all with same parent value
 *
 */
package com.dscid.filesystemanalyzer.analyzers;

import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;
import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.PrimeRepresentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 *
 * @author felix
 */
public enum PrimeBasedIdentity implements FileAnalyzer, DBSingleStorage {

  INSTANCE;
  private final DBLayer DBInstance;

  PrimeBasedIdentity() {
    this.DBInstance = DBLayer.getDbInstanceOf(this.getClass());
  }

  @Override
  public void analyzeItem(FileContext context) {
    String path = context.getPath().toString();
    PrimeRepresentation pr;
    try {
      pr = processPrimeBasedHash(context);
      Long remainder = pr.getValue() - pr.getPrime();
      DBInstance.updateValue(path, pr.getPrime().toString(), remainder.toString());
    } catch (IOException ex) {
    	System.err.println(ex);
    }
  }

  @Override
  public void analyzeItems(FileContext context) {
    String folderPath = context.getPath().toString();
   // System.out.print(String.format("Processing folder: %s ", folderPath));
    File folder = new File(folderPath);
    File[] IOItems = folder.listFiles();
    Long sum = 1L;
    for (final File path : IOItems) {
      String primeStr = "0";
      String remainderStr = "0";
      if (path.isFile()) {
        primeStr = DBInstance.getParentOf(path.toString());
        remainderStr = DBInstance.selectValueOf(path.toString());
      } else {
        primeStr = DBInstance.getParentOf(path.toString());
        remainderStr = DBInstance.selectValueOf(path.toString());
      }
      Long prime = Long.valueOf(primeStr);
      Long remainder = Long.valueOf(remainderStr);
      sum += prime + remainder;
    }
    PrimeRepresentation pr = new PrimeRepresentation(sum);
    Long remainder = pr.getValue() - pr.getPrime();
    DBInstance.updateValue(folderPath, pr.getPrime().toString(), remainder.toString());
   // System.out.println("... done! DB updated");
  }

  PrimeRepresentation processPrimeBasedHash(FileContext context) throws IOException {
    InputStream fis = null;
    if (context.getResourceStream() != null) {
      fis = context.getResourceStream();
    } else {
      fis = new FileInputStream(context.getPath().toString());
    }
    long sz= context.getFileAttributes().size();
    //byte[] bytes = context.getBytes();
    Long hc;// = fis.hashCode();//237938801
    hc = someNewHashCode(context);
    return new PrimeRepresentation(hc);
    //System.out.println(String.format("Path: %s; hash: %d; prime: %s",context.getPath().toString(), hc, pr));

  }

  private Long someNewHashCode(FileContext context) throws IOException {
    long sum=0;
    byte[] bytes = context.getBytes();
    int pos = 1;
    for (byte b : bytes){
      sum += b;
      sum +=(pos%3);
      pos++;
    }
    return sum;
  }

}
