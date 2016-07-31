/**
 * Wraps the Path and the BasicFileAttributes 
 */
package com.dscid.filesystemanalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.IOUtils;

/**
 * @author felix
 *
 */
public class FileContext {
  private final Path path;
  private final BasicFileAttributes fileAttrs;
  private final InputStream stream;
  private byte[] fileContentBytes;
  private boolean inMemoryFileContent = false;

  private FileContext(Path path, BasicFileAttributes FileAttrs, InputStream stream) throws IOException {
    this.path = path;
    this.fileAttrs = FileAttrs;
    this.stream = stream;
    this.fileContentBytes = null;
    // cache ONLY SMALL files
    if (this.fileAttrs.size()<50000000){
      this.inMemoryFileContent  = true;
      try {
        this.fileContentBytes = IOUtils.toByteArray(stream);
      } catch (Exception e) {
        System.err.printf("Error while reading %s", path);
        e.printStackTrace();
      }
    }
    // this.inMemoryFileContent = null;
    // with: 11460 8244 8133 8227 (21599,15662,15520)
    // null: 8220 8219 8286 8227 (
  }

  private FileContext(Path path, BasicFileAttributes FileAttrs) {
    this.path = path;
    this.fileAttrs = FileAttrs;
    stream = null;
    fileContentBytes = null;
  }

  /**
   * This approach assumes that the attributes of the files will not change
   * during processing
   * 
   * @param path
   * @param FileAttrs
   * @return
   */
  public static FileContext valueOf(Path path, BasicFileAttributes FileAttrs, InputStream inMem) throws IOException {
    return new FileContext(path, FileAttrs, inMem);
  }

  public static FileContext valueOf(Path path, BasicFileAttributes FileAttrs) {
    return new FileContext(path, FileAttrs);
  }

  public Path getPath() {
    return this.path;
  }

  public byte[] getBytes() {
    return fileContentBytes;
  }

  public BasicFileAttributes getFileAttributes() {
    return this.fileAttrs;
  }

  public InputStream getResourceStream() {
    return this.stream;
  }

  void closeResource() throws IOException {
    this.stream.close();
  }
  public boolean isFileContentInMemory(){
    return this.inMemoryFileContent;
  }

}
