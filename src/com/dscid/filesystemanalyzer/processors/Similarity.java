package com.dscid.filesystemanalyzer.processors;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.*;

import java.util.Comparator;

import static java.util.Comparator.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Map.Entry.*;

import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import static java.util.stream.Collectors.*;

import java.util.stream.Stream;

import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.App.ProcessFolder;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;
import com.dscid.filesystemanalyzer.analyzers.ItemCore;
import com.dscid.filesystemanalyzer.analyzers.ItemHash;
import com.dscid.filesystemanalyzer.analyzers.ItemSize;
import com.dscid.filesystemanalyzer.analyzers.ItemVector;
import com.dscid.filesystemanalyzer.utils.ValueComparator;

/***
 * Calculates similarity between all folders found based on their component's hash values
 * Still not ready it takes up lots of memory and time
 * @author felix
 *
 */
public enum Similarity implements Processors, DBSingleStorage {
  INSTANCE;
  private static final Logger logger = Logger.getLogger(Similarity.class.getName());
  private final DBLayer DBInstance;

  private Similarity() {
    DBInstance = DBLayer.getDbInstanceOf(this.getClass());
  }

  static final String[] Folders;
  static final String[] uniqueFolderHashes;
  /// Loads into memory all folders and all known hashes
  static {
    Map<String, List<String>> entrySet = ItemCore.INSTANCE.getGroupedValues(1);
    List<String> dirs = entrySet.get("D");
    dirs.remove(ProcessFolder.watchDirectory);
    Folders = dirs.toArray(new String[dirs.size()]);
    uniqueFolderHashes = ItemHash.INSTANCE.getDistinctHashes();
  }
  
  
  @Override
  public void analyzeItems() {
    String message = String.format("%s Processing Similarity ... ", ProcessFolder.commandsComment);
    System.out.printf("Time from begining: %d msecs.\n", ProcessFolder.getTimeIntervalMillis());
    logger.info(message);
    System.out.println(message);

    PrintStream stdout = null;

    try {
      stdout = ProcessFolder.toggleStdOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(ProcessFolder.logFolder + "/" + ProcessFolder.similarFoldersReport)), true));
      System.out.println(";echo 'Warining!! You are trying to run this file as a script. Please check before running it again.';\nexit(1)");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // for each folder, build a "vector" based on its children hashes
    // 1 Get all folders

    
    Map<String, Long[]> vectorList = new HashMap<String, Long[]>();

    long t0 = ProcessFolder.getTimeIntervalMillis();
    
    
    //20245: Elapsed Time: 1956077 msecs.
    if (ProcessFolder.refreshDB) {
      Stream.of(Similarity.Folders).forEach(Similarity::buildFolderVectors);
    }
    
    
    long t1 = ProcessFolder.getTimeIntervalMillis();
    System.err.printf("AFTER collecting folder list %d: Elapsed Time: %d msecs.\n", Similarity.Folders.length, t1 - t0);

    System.out.printf("Folder list: Time from begining: %d msecs.\n", ProcessFolder.getTimeIntervalMillis());
    
    generateSimilarityMatrix();
    
    System.err.printf("AFTER Matrix generated %d: Elapsed Time: %d msecs.\n", Similarity.Folders.length, ProcessFolder.getTimeIntervalMillis() - t1);
    
    ProcessFolder.toggleStdOut(stdout);
    System.out.println(String.format("%s %s", ProcessFolder.commandsComment, "Similarity done!"));
    // System.out.println(String.format("Drive space to recover: %s bytes",
    // humanBytes));
  }

  static Map<String, int[]>  vectorsAsString = new HashMap<>();
  private void generateSimilarityMatrix( ) {
    Map<String, Long[]> vectorList = new HashMap<>();
    Map<int[], Double> results = new HashMap<int[], Double>();
//    ValueComparator bvc = new ValueComparator(results);
//    TreeMap<int[], Double> sorted_results = new TreeMap<int[], Double>(bvc);
//    TreeMap<int[], Double> sorted_results = new TreeMap<int[], Double>(bvc);
    
   
    long t0 = ProcessFolder.getTimeIntervalMillis();
    long t1;
    System.err.printf("Generating %d vectors\n",Similarity.Folders.length);
    Similarity.vectorsAsString = Stream.of(Similarity.Folders).collect(toMap(dir->dir, INSTANCE::getVectorValueOf));
    long t01 = ProcessFolder.getTimeIntervalMillis();
    System.err.printf("GeneratED in %d msecs\n",t01-t0);

    
    long totalCount = (long) Math.pow(Similarity.Folders.length,2)/2;
    int[][] combinationsList;
    int[][] combinationsListSecond = null;
    int combinationsSize1 = (int) totalCount;
    int combinationsSize2 = (int) (totalCount >> 32);
    combinationsList = new int[combinationsSize1][2];
    if (combinationsSize2>0){
      combinationsListSecond =  new int[combinationsSize2][2];
    }
    
    long progress = 0L;
    long tenth = totalCount/10L;
    
    /**
     * TODO Replace with this SQL statement: 187 rows=> 34782 combinations in 230 ms 
     * SELECT 
             a.path || '|' || b.path
            ,a.value
            , b.value 
        FROM 
            com_dscid_filesystemanalyzer_processors_Similarity a
        inner join com_dscid_filesystemanalyzer_processors_Similarity b
            on a.path <> b.path
     */
    System.err.printf("Generating %d combinations\n",totalCount);
    int index1 = 0;
    int index2 = 0;
    for (int i = 0; i < Similarity.Folders.length; i++) {
      for (int k = i + 1; k < Similarity.Folders.length; k++) {
        if (index1 < Integer.MAX_VALUE - 8) {
          combinationsList[index1] = new int[]{i,k};
          index1 ++;
        } else {
          combinationsListSecond[index2] = new int[]{i,k};
          index2 ++;
        }
        progress++;
        if (progress>tenth) {
          t1 = ProcessFolder.getTimeIntervalMillis();
          System.err.printf("   %d in %d\n",tenth, t1 - t0);
          progress = 0L;
          t0 = ProcessFolder.getTimeIntervalMillis();
        }
      }
    }
    
    int[][] combinations = combinationsList;
    t1 = ProcessFolder.getTimeIntervalMillis();
    System.err.printf("   Last %d in %d\n",tenth, t1 - t0);
    System.err.printf("Streaming combinations\n");
    t0 = ProcessFolder.getTimeIntervalMillis();
    //TODO Sort http://stackoverflow.com/a/28607266/1599129
    
    results = Stream.of(combinationsList).parallel().collect(toMap(p->p, Similarity::cosineSimilarity));
    
    t1 = ProcessFolder.getTimeIntervalMillis();
    System.err.printf("  DONE Streaming combinations in %d\n", t1 - t0);
    
//    String o ="o";
//    if (o.equals("o")){
//      return;
//    }
    
    System.err.printf("GeneratED %d combinations\n",combinationsList.length);
    /*
    
    System.err.printf("Processing %d combinations, printing every %d iterations\n", totalCount, tenth);
    for (int i = 0; i < Similarity.Folders.length; i++) {
      for (int k = i + 1; k < Similarity.Folders.length; k++) {
        String keyX = Similarity.Folders[i];
        int[] vectorA = getVectorValueOf(keyX);

        String keyY = Similarity.Folders[k];
        int[] vectorB = getVectorValueOf(keyY);

        if (vectorA.length != vectorB.length) {
          String message = "There was a problem while generating the similarity matrix. Found two vectors with different sizes.";
          logger.info(message);
          throw new IllegalStateException(message);
        }
        progress++;
        if (progress>tenth) {
          t1 = ProcessFolder.getTimeIntervalMillis();
          System.err.printf("   %d in %d\n",tenth, t1 - t0);
          progress = 0L;
          t0 = ProcessFolder.getTimeIntervalMillis();
        }
        double similarity = cosineSimilarity(vectorA, vectorB);
        results.put(String.format("%d|%d", i, k), similarity);
      }
    }
    t1 = ProcessFolder.getTimeIntervalMillis();
    System.err.printf("  LAST %d in %d\n",tenth, t1 - t0);
    */
    System.out.printf("Raw matrix done with %d pairs: Time from begining: %d msecs.\n", results.size(), ProcessFolder.getTimeIntervalMillis());
//    sorted_results.putAll(results);
    Map<int[], Double> sortedMap = results.entrySet().stream()
        .sorted(reverseOrder(comparing(Entry::getValue)))
        .collect(toMap(Entry::getKey, Entry::getValue,
              (e1, e2) -> e1, LinkedHashMap::new));

    sortedMap.entrySet().parallelStream().forEachOrdered(s->{
      
      String pathA = Similarity.Folders[s.getKey()[0]];//O(1)
      String pathB = Similarity.Folders[s.getKey()[1]];//O(1)
      System.out.printf("%.8f=%s|%s\n", s.getValue(), pathA, pathB);
    });
    
    
//    for (Map.Entry<String, Double> result : sorted_results.entrySet()) {
//      String[] keys = result.getKey().split("\\|");
//      String dirA = Similarity.Folders[Integer.parseInt(keys[0])];
//      String dirB = Similarity.Folders[Integer.parseInt(keys[1])];
//      System.out.println(String.format("%.8f=%s", result.getValue(), String.format("%s|%s", dirA, dirB)));
//
//      String hash1 = ItemHash.INSTANCE.getValueOf(dirA);
//      String hash2 = ItemHash.INSTANCE.getValueOf(dirB);
//      System.out.println(String.format("%s\n%s", hash1, hash2));
//      System.out.println();
//    }
    System.out.printf("Ordered matrix done: Time from begining: %d msecs.\n", ProcessFolder.getTimeIntervalMillis());

  }

  /**
   * The function assumes that the two vectors have the same length.
   * 
   * @see http://stackoverflow.com/a/22913525
   * @see https 
   *      ://commons.apache.org/sandbox/commons-text/jacoco/org.apache.commons
   *      .text.similarity/CosineSimilarity.java.html
   * @param vectorA
   * @param vectorB
   * @return
   */
  public static double cosineSimilarity(int[] vectorListA, int[] vectorListB) {
    int[] vectorA = null;
    int[] vectorB = null;
    int sizeA = vectorListA.length;
    int sizeB = vectorListB.length;

    // Make vectors the same size by padding with zeros to the right the smaller
    // one
    if (sizeA != sizeB) {
      String message = "ERROR. Something is not working as expected. System encounter vectors with different sizes while calculating cosine similarity";
      logger.severe(message);
      throw new IllegalStateException(message);
    }
    // vectorA = vectorListA.toArray(new Long[vectorListA.size()]);
    vectorA = vectorListA;
    // vectorB = vectorListB.toArray(new Long[vectorListB.size()]);
    vectorB = vectorListB;
    long dotProduct = 0L;
    double normA = 0.0;
    double normB = 0.0;
    for (int i = 0; i < vectorA.length; i++) {
      long dot = vectorA[i] * vectorB[i];
      // if (dot < 0) {
      // long f = 879631L * 23899L;
      // String dfd = String.format("%d; %d", dot, f);
      // System.out.println(dfd );
      // }

      dotProduct += dot;
      normA += Math.pow(vectorA[i], 2);
      normB += Math.pow(vectorB[i], 2);
    }
    double similarity;
    if (normA <= 0 || normB <= 0) {
      similarity = 0.0;
    } else {
      similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // double distance = 2*Math.acos(similarity)/Math.PI;
    // similarity = 1 - distance;
    return similarity;
  }
  
  public static Double cosineSimilarity(int[] vectorPair) {
    String pathA = Similarity.Folders[vectorPair[0]];//O(1)
    String pathB = Similarity.Folders[vectorPair[1]];//O(1)
    int[] vectorA = Similarity.vectorsAsString.get(pathA);//O(1)
    int[] vectorB = Similarity.vectorsAsString.get(pathB);//O(1)
    return cosineSimilarity(vectorA, vectorB);
    
//    String key = String.format("%s|%s", pathA, pathB);
//    Map<String, Double> entry = new HashMap<String, Double>();
//    entry.put(key, similarity);
//    return entry;

  }
  


  static void buildFolderVectors(String dir) {

    List<String> children = ItemCore.INSTANCE.getChildrenPaths(dir);
    List<Integer> vec = new ArrayList<>();

    // StringBuilder vecStrvalue = new StringBuilder();

    long dirSize = ItemSize.INSTANCE.getValueOf(dir);
    int x = (int) (dirSize >> 32);
    int y = (int) dirSize;
    vec.add(1); // avoids division by zero later on
    // vec.add(x);
    // vec.add(y);
    // vecStrvalue.append(1).append("\t");// avoids division by zero later on
    // vecStrvalue.append(x).append("\t");
    // vecStrvalue.append(y).append("\t");
    List<Integer> oneHotVector = new ArrayList<Integer>(Collections.nCopies(Similarity.uniqueFolderHashes.length, 0));
    // String hashBinaryStr = ItemVector.listNumericString(oneHotVector, "\t");

    if (children.size() == 0) {
      vec.addAll(oneHotVector);
      // vecStrvalue.append(hashBinaryStr);

    } else {
      for (String child : children) {
        // "Word" embedding
        String childHash = ItemHash.INSTANCE.getValueOf(child);

        // @see http://stackoverflow.com/a/3674188
        int i = Arrays.binarySearch(Similarity.uniqueFolderHashes, childHash);
        if (i > -1) {
          oneHotVector.set(i, 1);
        }
      }
      vec.addAll(oneHotVector);
      // vecStrvalue.append(ItemVector.listNumericString(oneHotVector, "\t"));
    }
    // Map<String, List<Integer>> vectorList = new HashMap<String,
    // List<Integer>>();
    // vectorList.put(dir, vec);
    String value = Similarity.listNumericString(vec, "\t");
    Similarity.INSTANCE.DBInstance.updateValue(dir, dir, value);
    // return vec.toArray(new Long[vec.size()]);
    // return vec;
  }

//  static Map<String, int[]> cachedVectors = new HashMap<>();
  public int[] getVectorValueOf(String path) {
//    if (!cachedVectors.containsKey(path)) {
      String sizeString = DBInstance.selectValueOf(path);
      String[] sizeNData = sizeString.split(":");
      String[] vecAsString = sizeNData[1].split("\t");
      int size = Integer.parseInt(sizeNData[0]);
      int[] vec = new int[size];
      
      for (String v: vecAsString) {
        int index = Integer.parseInt(v);
        vec[index]=1;
      }
//      cachedVectors.put(path, vec);
//    }
//    return cachedVectors.get(path);
    return vec;
  }
  
  /**
   * Builds a string representation similar to Weka's ARFF Sparse files @data section
   * {lengh:[positions where value == 1]}
   * @param list
   * @param glue
   * @return
   */
  static String listNumericString(List<Integer> list, String glue) {
    if (list.size()==0) {
      return "0";
    }
    StringBuilder strbul = new StringBuilder().append(list.size()).append(":");
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) == 1) {
        strbul.append(i).append(glue);
      }
    }
    return strbul.substring(0, strbul.length()-1);
  }
  static String listNumericString(int[] list, String glue) {
    if (list.length==0) {
      return "0";
    }
    StringBuilder strbul = new StringBuilder().append(list.length).append(":");
    for (int i = 0; i < list.length; i++) {
      if (list[i] == 1) {
        strbul.append(i).append(glue);
      }
    }
    return strbul.substring(0, strbul.length()-1);
  }
}
