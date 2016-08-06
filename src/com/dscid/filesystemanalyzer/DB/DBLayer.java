/**
 *
 */
package com.dscid.filesystemanalyzer.DB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.dscid.filesystemanalyzer.AnalyzerStorage;
import com.dscid.filesystemanalyzer.App.ProcessFolder;

/**
 * Use singleton here C:\Users\felix\workspace\sqlite4java-282>java -jar
 * sqlite4java.jar sqlite4java 282 SQLite 3.7.10 Compile-time options:
 * ENABLE_COLUMN_METADATA ENABLE_FTS3 ENABLE_FTS3_PARENTHESIS
 * ENABLE_MEMORY_MANAGEMENT ENABLE_RTREE OMIT_DEPRECATED TEMP_STORE=1
 * THREADSAFE=1 Converted to an abstract class to provided common logic as much
 * as possible Each plug-in will have its own instance. This class will have the
 * collection of all instantiated plug-ins fileSystemAnalyzer.DB's and will
 * guarantee uniqueness and coherence
 *
 * @author felix
 *
 */
public final class DBLayer implements AnalyzerStorage {

  private static final Map<Class<? extends DBSingleStorage>, DBLayer> DBInstances = new HashMap<Class<? extends DBSingleStorage>, DBLayer>();
  private SQLiteConnection Connection = null;

  private final String mainTableName;

  private DBLayer(String pluginId) {
    this.mainTableName = pluginId.replace(".", "_");
  }

  /**
   * Updates the corresponding record and returns its id
   * 
   * @param path
   * @param parent
   * @param value
   */
  public void updateValue(String path, String parent, String value) {
    DBLayer.updateInstanceValueOf(this, path, parent, value);
  }

  /**
   * Updates the parent value
   *
   * @param string
   * @param string2
   * @return
   */
  public String updateParentId(String childPath, String parentId) {
    return DBLayer.updateInstanceParentId(this, childPath, parentId);
  }

  public String getParentOf(String path) {
    return selectInstanceValueOf(this, path, "parent");
  }

  public String selectValueOf(String path) {
    return selectInstanceValueOf(this, path, "value");
  }

  // TODO: move this to the static realm
  public List<String> selectPathsOf(String value) {

    String getPathSQL = "SELECT path FROM " + this.mainTableName + " WHERE value=?";
    SQLiteConnection conn = this.Connection;
    List<String> res = new ArrayList<String>();
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }
      SQLiteStatement stmt = conn.prepare(getPathSQL);
      stmt.bind(1, value);
      while (stmt.step()) {
        res.add(stmt.columnString(0));
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return res;
  }

  /**
   * Instances manipulators
   */
  // http://java.dzone.com/articles/singleton-design-pattern-%E2%80%93
  public static DBLayer unSet(String PluginId) throws DBLayerException {
    if (!DBInstances.containsKey(PluginId)) {
      throw new DBLayerException(DBLayerException.INSTANCE_DOESNOT_EXISTS);
    }
    return DBInstances.remove(PluginId);
  }

  public static DBLayer getDbInstanceOf(Class<? extends DBSingleStorage> PluginId) {
    if (DBInstances.containsKey(PluginId)) {
      return DBInstances.get(PluginId);
    } else {
      return getNew(PluginId);
    }
  }

  /**
   * Returns all children paths for a given parent
   * 
   * @param parent
   * @return
   */
  public List<String> getChildrenPaths(String parent) {
    List<String> results = new ArrayList<String>();

    String hashedValuesSQL = "SELECT path FROM " + this.mainTableName + " WHERE parent=? ";

    SQLiteConnection conn = this.Connection;
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }

      SQLiteStatement stmt = conn.prepare(hashedValuesSQL);
      stmt.bind(1, parent);

      while (stmt.step()) {
        results.add(stmt.columnString(0));
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return results;
  }

  /***
   * Returns all children values for a given parent
   * 
   * @param parent
   * @return
   */
  public List<String> getChildrenValues(String parent) {
    List<String> results = new ArrayList<String>();

    String hashedValuesSQL = "SELECT value FROM " + this.mainTableName + " WHERE parent=? ";

    SQLiteConnection conn = this.Connection;
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }

      SQLiteStatement stmt = conn.prepare(hashedValuesSQL);
      stmt.bind(1, parent);

      while (stmt.step()) {
        results.add(stmt.columnString(0));
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return results;
  }

  public List<String> getDistinctValues() {
    List<String> results = new ArrayList<String>();
    String hashedValuesSQL = "SELECT DISTINCT value FROM " + this.mainTableName;

    SQLiteConnection conn = this.Connection;
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }

      SQLiteStatement stmt = conn.prepare(hashedValuesSQL);
      while (stmt.step()) {
        results.add(stmt.columnString(0));
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return results;
  }

  public String concatValuesByParent(String parent) {
    String results = "";

    String hashedValuesSQL = "SELECT group_concat(value, '|') FROM " + this.mainTableName + " WHERE parent=? ";

    SQLiteConnection conn = this.Connection;
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }

      SQLiteStatement stmt = conn.prepare(hashedValuesSQL);
      stmt.bind(1, parent);
      while (stmt.step()) {
        results = stmt.columnString(0);
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return results;
  }

  public String sumValuesByParent(String parent) {
    String results = "";

    String hashedValuesSQL = "SELECT sum(value) FROM " + this.mainTableName + " WHERE parent=? ";

    SQLiteConnection conn = this.Connection;
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }

      SQLiteStatement stmt = conn.prepare(hashedValuesSQL);
      stmt.bind(1, parent);
      while (stmt.step()) {
        results = stmt.columnString(0);
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return results;
  }

  /**
   * Temporary solution for grouping SQLs
   *
   * @param Plugin
   * @return
   */
  // TODO: move this to the static realm
  public Map<String, List<String>> selectGroupedValues(Integer minCount) {
    String having = "";
    if (minCount != null && minCount.compareTo(0) > 0) {
      having = " HAVING count(*)>" + minCount.toString();
    }

    Map<String, List<String>> results = new HashMap<String, List<String>>();
    String hashedValuesSQL = "SELECT value, group_concat(path, '|') count FROM " + this.mainTableName + " GROUP BY value " + having;

    SQLiteConnection conn = this.Connection;
    List<Object> res = new ArrayList<Object>();
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }

      SQLiteStatement stmt = conn.prepare(hashedValuesSQL);
      while (stmt.step()) {
        String value = stmt.columnString(0);
        String concat = stmt.columnString(1);
        String[] path = concat.split("\\|");
        List<String> paths = new ArrayList<>();
        paths.addAll(Arrays.asList(path));
        results.put(value, paths);
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // return this.dbSelect(getValueSQL, new Dictionary<string,
    // string>() { { "path", path }
    // }).First().Values.First().ToString();
    return results;
  }

  private static DBLayer getNew(Class<? extends DBSingleStorage> plugin) {
    assert plugin != null;
    if (DBInstances.containsKey(plugin) && (DBInstances.get(plugin) instanceof DBLayer)) {
      return DBInstances.get(plugin);
    }
    DBLayer _db = null;
    String PluginId = buildID(plugin);
    SQLiteConnection conn = null;
    String DBFolder = "storage";
    StringBuilder dbPath = new StringBuilder();
    dbPath.append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append(DBFolder);
    File databaseFolder = new File(dbPath.toString());
    if (!databaseFolder.exists()) {
      databaseFolder.mkdir();
    }
    dbPath.append(System.getProperty("file.separator")).append(PluginId).append(".sqlite");
    File databasePath = new File(dbPath.toString());

    // TODO: Update logic to avoid delteing DB file unnecessary
    try {
      if (ProcessFolder.refreshDB) {
        Files.deleteIfExists(databasePath.toPath());
      }
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    if (Files.exists(databasePath.toPath())) {
      conn = new SQLiteConnection(databasePath);
      _db = new DBLayer(PluginId);
      _db.Connection = conn;
      DBInstances.put(plugin, _db);
    } else {
      conn = new SQLiteConnection(databasePath);
      if (!conn.isOpen()) {
        try {
          // TODO: On OSX we need to rename the libs file, see
          // http://stackoverflow.com/a/22099958/1599129
          // TODO: This worked: cp libsqlite4java-osx.jnilib
          // libsqlite4java-osx-amd64.dylib
          conn.open(true);
          _db = new DBLayer(PluginId);
          _db.Connection = conn;
          String CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS __table__ ( parent NVARCHAR(2048) NULL, path NVARCHAR(2048) NULL, value TEXT NULL)".replace("__table__", _db.mainTableName);
          conn.exec(CREATE_MAIN_TABLE);
          String INDEXES = "CREATE UNIQUE INDEX `unique_path` ON `__table__` (`path` ASC);".replace("__table__", _db.mainTableName);
          conn.exec(INDEXES);

          DBInstances.put(plugin, _db);
        } catch (SQLiteException e) {
          new DBLayerException(DBLayerException.UNABLE_TO_CREATE_DBLAYER_INSTANCE, e).printStackTrace();
        }
      }
    }
    System.out.println("Instantiating DBLAyer for pluggin " + PluginId + " hascode: " + _db.hashCode());
    // no instance found, lets create one
    return _db;
  }

  /**
   * Use to create the fileSystemAnalyzer.DB friendly ID
   *
   * @param c
   * @return
   */
  private static String buildID(Class<? extends DBSingleStorage> c) {
    return c.toString().split(" ")[1].split("\\$")[0];
  }

  // Database related operations, comun to all instance
  /**
   * Gets a value from the given fieldname and path
   *
   * @param dBInstance
   *
   * @param path
   * @param fieldName
   * @return
   */
  public static String selectInstanceValueOf(DBLayer dBInstance, String path, String dBFieldName) {
    // System.out.println("dBInstance hascode " +dBInstance.hashCode());

    String getValueSQL = "SELECT " + dBFieldName + " FROM " + dBInstance.mainTableName + " WHERE path=?";

    SQLiteConnection conn = dBInstance.Connection;
    List<Object> res = new ArrayList<Object>();
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }
      SQLiteStatement stmt = conn.prepare(getValueSQL);
      stmt.bind(1, path);
      while (stmt.step()) {
        res.add(stmt.columnValue(0));
      }
      stmt.dispose();
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // return this.dbSelect(getValueSQL, new Dictionary<string,
    // string>() { { "path", path }
    // }).First().Values.First().ToString();
    String ret = "";
    if (res.size() > 0 && res.get(0) != null) {
      ret = res.get(0).toString();
    }
    return ret;
  }

  /**
   * Same, but using Id instead, needs id's to be synchronized
   *
   * @param dBInstance
   * @param id
   * @param dBFieldName
   * @return
   */
  public static String selectInstanceValueOf(DBLayer dBInstance, Long id, String dBFieldName) {
    String SQLByID_SelectInstanceValueOf = "SELECT " + dBFieldName + " FROM " + dBInstance.mainTableName + " WHERE ID=?";
    SQLiteConnection conn = dBInstance.Connection;
    List<Object> res = new ArrayList<Object>();
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }
      SQLiteStatement stmt = conn.prepare(SQLByID_SelectInstanceValueOf);
      stmt.bind(1, id);
      while (stmt.step()) {
        res.add(stmt.columnValue(0));
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // return this.dbSelect(getValueSQL, new Dictionary<string,
    // string>() { { "path", path }
    // }).First().Values.First().ToString();
    String ret = "";
    if (res.size() > 0 && res.get(0) != null) {
      ret = res.get(0).toString();
    }
    return ret;
  }

  public static List<String> selectValuesOf(DBLayer dBInstance, String dBFieldName, List<String> paths) {
    List<String> res = new ArrayList<String>();
    if (paths.size() == 0) {
      return res;
    }
    String getValueSQL = "SELECT " + dBFieldName + " FROM " + dBInstance.mainTableName + " WHERE path in (";

    SQLiteConnection conn = dBInstance.Connection;
    try {
      if (!conn.isOpen()) {
        conn.open(true);
      }
      String paramList = "";
      String comma = "";
      for (String item : paths) {
        paramList = paramList.concat(comma).concat("?");
        comma = ",";
      }
      SQLiteStatement stmt = conn.prepare(getValueSQL + paramList + ")");
      int c = 1;
      for (String item : paths) {
        stmt.bind(c, item);
        c++;
      }
      while (stmt.step()) {
        res.add(stmt.columnString(0));
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // return this.dbSelect(getValueSQL, new Dictionary<string,
    // string>() { { "path", path }
    // }).First().Values.First().ToString();
    return res;
  }

  /**
   *
   * @param dBInstance
   * @param PluginClass
   * @param path
   * @param value
   * @return
   */
  private static void updateInstanceValueOf(DBLayer dBInstance, String path, String parent, String value) {

    String SQLString = "INSERT OR REPLACE INTO " + dBInstance.mainTableName + " (value, path, parent) VALUES (?,?,?)";

    // System.out.println("SQLString hascode " +SQLString.hashCode());
    SQLiteConnection conn = null;
    List<Object> res = new ArrayList<Object>();
    try {
      conn = dBInstance.Connection;
      synchronized (conn) {
        if (!conn.isOpen()) {
          conn.open();
        }
        SQLiteStatement stmt = conn.prepare(SQLString);
        stmt.bind(1, value);
        stmt.bind(2, path);
        stmt.bind(3, parent);
        // conn.exec(SQLString);
        while (stmt.step()) {
          res.add(stmt.columnValue(0));
        }
        stmt.dispose();
      }

      return;
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } finally {
      if (conn != null && conn.isOpen()) {
        // conn.dispose();
      }
    }

  }

  private static String updateInstanceParentId(DBLayer dBInstance, String path, String parentId) {
    String SQLString = "UPDATE " + dBInstance.mainTableName + " SET parent=? WHERE path=?";
    String id = "";
    SQLiteConnection conn = dBInstance.Connection;
    List<Object> res = new ArrayList<Object>();
    try {
      synchronized (conn) {
        if (!conn.isOpen()) {
          conn.open(true);
        }
        SQLiteStatement stmt = conn.prepare(SQLString);
        stmt.bind(1, parentId);
        stmt.bind(2, path);
        // conn.exec(SQLString);
        while (stmt.step()) {
          res.add(stmt.columnValue(0));
        }
      }
    } catch (SQLiteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } finally {
      if (conn != null && conn.isOpen()) {
        conn.dispose();
      }
    }

    id = selectInstanceValueOf(dBInstance, path, "id");
    // return this.dbSelect(getValueSQL, new Dictionary<string,
    // string>() { { "path", path }
    // }).First().Values.First().ToString();
    return id;
  }

  public static String join(List<String> list, String glue) {
    String jointed = "";
    if (list.size() > 0) {
      String comma = "";
      for (String item : list) {
        jointed = jointed.concat(comma).concat(item);
        comma = glue;
      }
    }
    return jointed;
  }

  public static String join(Set<String> list, String glue) {
    return join(new ArrayList<String>(list), glue);
  }

}
