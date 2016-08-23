package com.dscid.filesystemanalyzer.processors;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.dscid.filesystemanalyzer.App.ProcessFolder;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;
import com.dscid.filesystemanalyzer.analyzers.ItemCore;
import com.dscid.filesystemanalyzer.analyzers.ItemHash;
import com.dscid.filesystemanalyzer.analyzers.ItemSize;
import com.dscid.filesystemanalyzer.utils.ValueComparator;

public enum Similarity implements Processors, DBSingleStorage {
  INSTANCE;
  private static final Logger logger = Logger.getLogger(Similarity.class.getName());
  // private final DBLayer DBInstance;

  // private Similarity() {
  // DBInstance = DBLayer.getDbInstanceOf(this.getClass());
  // }

  @Override
  public void analyzeItems() {
    String message = String.format("%s Processing Similarity ... ", ProcessFolder.commandsComment);
    logger.info(message);
    System.out.println(message);
    
    PrintStream stdout = null;
    
    try {
      stdout = ProcessFolder.toggleStdOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(ProcessFolder.logFolder + "/"+ProcessFolder.similarFoldersReport)), true));
      System.out.println(";echo 'Warining!! You are trying to run this file as a script. Please check before running it again.';\nexit(1)");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // for each folder, build a "vector" based on its children hashes
    // 1 Get all folders
    Map<String, List<String>> entrySet = ItemCore.INSTANCE.getGroupedValues(1);
    List<String> dirs = entrySet.get("D");
    dirs.remove(ProcessFolder.watchDirectory);
    List<String> hasheshDictionary = ItemHash.INSTANCE.getDistinctHashes();
    Map<String, List<Integer>> vectorList = new HashMap<String, List<Integer>>();
    for (String dir : dirs) {
      if (dir.equals(ProcessFolder.watchDirectory)) {
        continue;
      }
      List<String> children = ItemCore.INSTANCE.getChildrenPaths(dir);
      List<Integer> vec = new ArrayList<Integer>();
      long dirSize = ItemSize.INSTANCE.getValueOf(dir);
      int x = (int) (dirSize >> 32);
      int y = (int) dirSize;
      vec.add(x);
      vec.add(y);
      if (children.size() == 0) {
        vec.addAll(new ArrayList<>(Collections.nCopies(hasheshDictionary.size(), 0)));
      }

      else {
        List<Integer> hashBinary = new ArrayList<>(Collections.nCopies(hasheshDictionary.size(), 0));
        for (String child : children) {
          // "Word" embedding
          String childHash = ItemHash.INSTANCE.getValueOf(child);

          int i = hasheshDictionary.indexOf(childHash);
          if (i > -1) {
            hashBinary.set(i, 1);
          }

        }
        vec.addAll(hashBinary);
      }
      vectorList.put(dir, vec);
    }
    generateSimilarityMatrix(dirs, vectorList);
    ProcessFolder.toggleStdOut(stdout);
    System.out.println(String.format("%s %s", ProcessFolder.commandsComment, "Similarity done!"));
//    System.out.println(String.format("Drive space to recover: %s bytes", humanBytes));
  }

  private void generateSimilarityMatrix(List<String> dirs, Map<String, List<Integer>> vectorList) {
    HashMap<String, Double> results = new HashMap<String, Double>();
    ValueComparator bvc = new ValueComparator(results);
    TreeMap<String, Double> sorted_results = new TreeMap<String, Double>(bvc);

    for (int i = 0; i < dirs.size(); i++) {
      for (int k = i + 1; k < dirs.size(); k++) {
        String keyX = dirs.get(i);
        List<Integer> vectorA = vectorList.get(keyX);
        String keyY = dirs.get(k);
        
        List<Integer> vectorB = vectorList.get(keyY);

        if (vectorA.size() != vectorB.size()) {
          String message = "There was a problem while generating the similarity matrix. Found two vectors with different sizes.";
          logger.info(message);
          throw new IllegalStateException(message);
        }

        double similarity = cosineSimilarity(vectorA, vectorB);
        results.put(keyX + "|" + keyY, similarity);
      }
    }
    sorted_results.putAll(results);
    for (Map.Entry<String, Double> result : sorted_results.entrySet()) {
      System.out.println(String.format("%.2f=%s", result.getValue(), result.getKey()));
    }
  }

  private void _generateSimilarityMatrix(Map<String, List<Integer>> vectorList) {
    TreeMap<String, Double> results = new TreeMap<String, Double>();
    ValueComparator bvc = new ValueComparator(results);
    TreeMap<String, Double> sorted_results = new TreeMap<String, Double>(bvc);

    for (Map.Entry<String, List<Integer>> entryA : vectorList.entrySet()) {
      String dirA = entryA.getKey();
      List<Integer> vectorListA = entryA.getValue();
      for (Map.Entry<String, List<Integer>> entryB : vectorList.entrySet()) {
        String dirB = entryB.getKey();
        if (dirA.equals(dirB)) {
          continue;
        }
        // Skip the other have of the matrix
        if (results.containsKey(dirB + "|" + dirA)) {
          continue;
        }

        String key = dirA + "|" + dirB;
        List<Integer> vectorListB = entryB.getValue();
        double similarity = cosineSimilarity(vectorListA, vectorListB);
        results.put(key, similarity);
      }
    }
    sorted_results.putAll(results);
    // System.out.println(sorted_results);
    for (Map.Entry<String, Double> result : sorted_results.entrySet()) {
      System.out.println(String.format("%.2f=%s", result.getValue(), result.getKey()));
    }
  }

  /**
   * The function assumes that the two vectors have the same length.
   * 
   * @see http://stackoverflow.com/a/22913525
   * @param vectorA
   * @param vectorB
   * @return
   */
  public static double cosineSimilarity(List<Integer> vectorListA, List<Integer> vectorListB) {
    Integer[] vectorA = null;
    Integer[] vectorB = null;
    int sizeA = vectorListA.size();
    int sizeB = vectorListB.size();

    // Make vectors the same size by padding with zeros to the right the smaller
    // one
    if (sizeA != sizeB) {
      // vectorListB.addAll(new
      // ArrayList<Integer>(Collections.nCopies(sizeA-sizeB, 0)));
      String message = "ERROR. Something is not working as expected. System encounter vectors with different sizes while calculating cosine similarity";
      logger.severe(message);
      throw new IllegalStateException(message);
    }
    /*
     * else if (sizeA < sizeB) { vectorListA.addAll(new
     * ArrayList<Integer>(Collections.nCopies(sizeB-sizeA, 0))); } Set<Integer>
     * setA = new HashSet<Integer>(vectorListA); Set<Integer> setB = new
     * HashSet<Integer>(vectorListB); //This is for empty folders vectors if
     * (setA.size()==1 && setB.size() == 1) { return 1.0; } else if
     * (setA.size()==1) { vectorListA.add(0,1); vectorListB.add(0); } else if
     * (setB.size()==1) { vectorListB.add(0,1); vectorListA.add(0); }
     */
    vectorA = vectorListA.toArray(new Integer[vectorListA.size()]);
    vectorB = vectorListB.toArray(new Integer[vectorListB.size()]);
    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;
    for (int i = 0; i < vectorA.length; i++) {
      dotProduct += vectorA[i] * vectorB[i];
      normA += Math.pow(vectorA[i], 2);
      normB += Math.pow(vectorB[i], 2);
    }
    if (normA == 0 || normB == 0) {
      if (normA != normB) {
        return -1.0;
      } else {
        return 1.0;
      }
    }
    double result = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    return result;
  }
}
