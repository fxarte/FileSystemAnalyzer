package com.dscid.filesystemanalyzer.analyzers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.dscid.filesystemanalyzer.Analyzer;
import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public enum ItemHash implements FileAnalyzer, DBSingleStorage {

  INSTANCE;
  private final DBLayer DBInstance;
  private final Set<Class<? extends FileAnalyzer>> dependencies;

  ItemHash() {
    this.DBInstance = DBLayer.getDbInstanceOf(ItemHash.class);
    this.dependencies = new LinkedHashSet<Class<? extends FileAnalyzer>>();
    dependencies.add(ItemSize.class);
  }

  Set<Class<? extends Analyzer>> getDependencies() {
    // TODO Auto-generated method stub
    Set<Class<? extends Analyzer>> defensiveCopyMap = new LinkedHashSet<Class<? extends Analyzer>>();
    defensiveCopyMap.addAll(dependencies);
    return defensiveCopyMap;
  }

  /**
   * Calculates the sha1 of a file
   *
   * @param context
   * @return the Base64 string representation of the SHA1
   * @throws Exception
   */
  private String createFileSha1(FileContext context) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    InputStream fis;
//    if (context.getInMemoryFileContent() != null) {
      fis = context.getInMemoryFileContent();
      //TODO This approach will require lots for memory for big files
      byte[]  bytes = context.getBytes();
//    } else {
//      fis = new FileInputStream(context.getPath().toString());
//    }
/*
BasicFileAttributes arg1 = context.getFileAttributes();
Path file = context.getPath();

        if (arg1.isSymbolicLink()) {
            String syml = Files.readSymbolicLink(file).toString();
            fis = new ByteArrayInputStream(syml.getBytes(StandardCharsets.UTF_8));
        } else {
            fis = new FileInputStream(file.toFile());
        }
*/


    int n = 0;
    byte[] buffer = new byte[8192];
    while (n != -1) {
      n = fis.read(buffer);
      if (n > 0) {
        digest.update(buffer, 0, n);
      }
    }
//     fis.close();
//    byte[] hash = digest.digest();
    byte[] hash = digest.digest(bytes);
    
    String stringToStore = Base64.encodeBase64String(hash);
    return stringToStore;
  }

  private String createFolderSha1(Path dirPath) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    // a zero to mark folders
    digest.update((byte) 0);

    DBLayer sizes = DBLayer.getDbInstanceOf(ItemSize.class);

    String dbSizeValue = sizes.selectValueOf(dirPath.toString());
    Long dirPathSize = Long.valueOf(dbSizeValue);
    /*
     * if (dirPathSize.equals(0L)) { return "<empty>"; }
     */
    // adding the folder size
    digest.update((byte) dirPathSize.byteValue());

    File folder = new File(dirPath.toString());

    File[] IOItems = folder.listFiles();
    try {
      for (final File path : IOItems) {
        if (path.isFile()) {

          InputStream fis = new FileInputStream(path);
          int n = 0;
          byte[] buffer = new byte[8192];
          while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
              digest.update(buffer, 0, n);
            }
          }
          fis.close();
        } else {
          // TOOD: lets add a 0 per sub dir for now
          String folderHash = DBInstance.selectValueOf(path.toString());
          digest.update(Base64.decodeBase64(folderHash));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    byte[] hash = digest.digest();
    String stringToStore = Base64.encodeBase64String(hash);
		// gB9qBtZdxFB33Gtm4uTlIgwuO0U=
    //byte[] restoredBytes = Base64.decodeBase64(stringToStore);
    // [-128, 31, 106, 6, -42, 93, -60, 80, 119, -36, 107, 102, -30, -28,
    // -27, 34, 12, 46, 59, 69]
    // [-128, 31, 106, 6, -42, 93, -60, 80, 119, -36, 107, 102, -30, -28,
    // -27, 34, 12, 46, 59, 69]

    return stringToStore;
  }

  public void analyzeItem(FileContext context) {
    Path filePath = context.getPath();
    BasicFileAttributes arg1 = context.getFileAttributes();
    String path = filePath.toString();
    String parent = filePath.getParent().toString();

    String value;
    try {
      value = createFileSha1(context);
//      if (arg1.isSymbolicLink()) {
//        System.out.printf(">>>>: %d\n", context.getBytes().length);
//      } else {
//        System.out.printf("++++: %d\n", context.getBytes().length);
//      }
//System.out.printf("                  '%s': %s\n",context.getPath().toString(), value );

      DBInstance.updateValue(path, parent, value);
      // System.out.format("processing File: %s\n", filePath);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    /*
     String extension = "";

     int i = path.lastIndexOf('.');
     if (i > 0) {
     extension = path.substring(i + 1).toLowerCase();
     }
     // TODO: Incomplete
     if (extension.equals("jpg") && false)
     calculateBRIEF(filePath, arg1);
     */
  }

  public void analyzeItems(FileContext context) {
		// com.tangosol.util Class HashHelper ?
    // public static int hash(java.util.Collection col,int hash)
    // get all files hashes
    // calculate folder hash
    String value = "";
    try {
      value = createFolderSha1(context.getPath());
      DBLayer core = DBLayer.getDbInstanceOf(ItemCore.class);

      DBInstance.updateValue(context.getPath().toString(), context
              .getPath().getParent().toString(), value);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  /*
   private void calculateBRIEF(Path filePath, BasicFileAttributes arg1) {
   // TODO Auto-generated method stub
   String path = filePath.toString();
   // The JRE is 64 bits and the jar file is 32bits, we have to load the
   // library manually ;/
   System.load("C:\\Users\\felix\\workspace\\opencv\\opencv\\build\\java\\x64\\opencv_java248.dll");
   // FeatureDetector star = FeatureDetector.create(FeatureDetector.STAR);
   try {
   Mat image = Highgui.imread(path);

   int w = image.width();
   int h = image.height();
   System.out.printf("height: %i; width:%i%n", w, h);
   // star.detect(image, keypoints);
   } catch (Exception ex) {
   System.out.println("error while processing image");
   }
   }
   */

  public Map<String, List<String>> getGroupedValues(int minCount) {
    return DBInstance.selectGroupedValues(minCount);
  }

    /**
   * Returns the hash for a given path
   * 
   * @param path
   * @return
   */
  public String getValueOf(String path) {
    String sizeString = DBInstance.selectValueOf(path);
    return sizeString;
  }
}
