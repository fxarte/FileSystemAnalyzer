/*
 * analyzer:
 * store each line hash in the DB (dictionary DB), no duplicates, ,return reference INT
 * per TXT file: get list of line hashes path|SUM hash dictionary reference (INT), store value as rounded to the previous prime plus difference to actual value (DB strucure: path=previousPrime, path=path;value=sum of references - prime(parent))
 * Compare similarity:
 * get all with same parent value
 */

package fileSystemAnalyzer.processors;

import fileSystemAnalyzer.DB.DBSingleStorage;

/**
 *
 * @author felix
 */
public class SimilarObjects  implements Processors, DBSingleStorage {

  @Override
  public void analyzeItems() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
