package com.dscid.filesystemanalyzer.processors;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.dscid.filesystemanalyzer.App.ProcessFolder;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;
import com.dscid.filesystemanalyzer.analyzers.ItemCore;
import com.dscid.filesystemanalyzer.analyzers.ItemHash;
import com.dscid.filesystemanalyzer.analyzers.ItemSize;
import com.dscid.filesystemanalyzer.utils.ValueComparator;

public enum Similarity implements Processors, DBSingleStorage {
  INSTANCE;
//  private final DBLayer DBInstance;

//  private Similarity() {
//    DBInstance = DBLayer.getDbInstanceOf(this.getClass());
//  }

  @Override
  public void analyzeItems() {
    try {
      System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(ProcessFolder.logFolder + "/similar_folders.log")), true));
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // for each folder, build a "vector" based on its children hashes
    // 1 Get all folders
    Map<String, List<String>> entrySet = ItemCore.INSTANCE.getGroupedValues(1);
    List<String> dirs = entrySet.get("D");
    List<String> hasheshDictionary = ItemHash.INSTANCE.getDistinctHashes();
    Map<String, List<Integer>> vectorList = new HashMap<String, List<Integer>>();
    for (String dir : dirs) {
      if (dir.equals(ProcessFolder.watchDirectory)) {
        continue;
      }
      List<String> children = ItemHash.INSTANCE.getChildren(dir);
      //INlcude the size infoirmatrion
      long dirSize = ItemSize.INSTANCE.getValueOf(dir);
      int x = (int)(dirSize >> 32);
      int y = (int) dirSize;
      List<Integer> vec = new ArrayList<Integer>();
      vec.add(x);
      vec.add(y);
      for (String childHash : children) {
        //+1 to avoid devision by zero down the road
        Integer pos = hasheshDictionary.indexOf(childHash+1);
        vec.add(pos);
      }
      vectorList.put(dir, vec);
    }
    generateSimilarityMatrix(vectorList);
  }

  private void generateSimilarityMatrix(Map<String, List<Integer>> vectorList) {
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
        //Skip the other have of the matrix
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
//    System.out.println(sorted_results);
    for (Map.Entry<String, Double> result : sorted_results.entrySet()){
      System.out.println(String.format("%.2f=%s",result.getValue(), result.getKey()));
    }
  }

  /**
   *  The function assumes that the two vectors have the same length.
   *  @see http://stackoverflow.com/a/22913525
   * @param vectorA
   * @param vectorB
   * @return
   */
  public static double cosineSimilarity(List<Integer> vectorListA, List<Integer> vectorListB) {
    int sizeA = vectorListA.size();
    int sizeB = vectorListB.size();
    
    //Make vectors the same size by padding with zeros to the right the smaller one
    if (sizeA > sizeB) {
      vectorListB.addAll(new ArrayList<Integer>(Collections.nCopies(sizeA-sizeB, 0)));
    }
    else if (sizeA < sizeB) {
      vectorListA.addAll(new ArrayList<Integer>(Collections.nCopies(sizeB-sizeA, 0)));
    }
    Set<Integer> setA = new HashSet<Integer>(vectorListA);
    Set<Integer> setB = new HashSet<Integer>(vectorListB);
    Integer[] vectorA = null;
    Integer[] vectorB = null;
    //This is for empty folders vectors
    if (setA.size()==1 && setB.size() == 1) {
      return 1.0;
    } else if (setA.size()==1) {
      vectorListA.add(0,1);
      vectorListB.add(0);
    } else if (setB.size()==1) {
      vectorListB.add(0,1);
      vectorListA.add(0);
    } 
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
    double result = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    return result;
  }
}
