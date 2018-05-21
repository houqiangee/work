package com.framework.util;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataStore extends ArrayList<DataObject>
{
  private static final long serialVersionUID = 1L;
  public static final String TYPE_STRING = "string";
  public static final String TYPE_NUMBER = "number";
  public static final String TYPE_DATE = "date";
  public static final String TYPE_BOOLEAN = "boolean";
  public static final String TYPE_CLOB = "clob";
  public static final String TYPE_BLOB = "blob";
  public static final String TYPE_NULL = "null";
  public static final String TYPE_STRING_AB = "s";
  public static final String TYPE_NUMBER_AB = "n";
  public static final String TYPE_DATE_AB = "d";
  public static final String TYPE_BOOLEAN_AB = "b";
  public static final String TYPE_NULL_AB = "l";
  public static final String TYPE_CLOB_AB = "clob";
  public static final String TYPE_BLOB_AB = "blob";
  private LinkedHashMap<String, String> columnTypeMap;

  public DataStore()
  {
    if (this.columnTypeMap == null)
      this.columnTypeMap = new LinkedHashMap();
  }

  public DataStore(Vector<DataObject> vector)
  {
    if (null == vector)
      return;
    for (Iterator localIterator = vector.iterator(); localIterator.hasNext(); ) { Object object = localIterator.next();
      addRow((DataObject)object);
    }
  }

  public DataStore(int rowInit) {
    super(rowInit);
    if (this.columnTypeMap == null)
      this.columnTypeMap = new LinkedHashMap();
  }

  public DataStore(List<DataObject> nds) {
    super(nds);
    if (this.columnTypeMap == null)
      this.columnTypeMap = new LinkedHashMap();
  }

  public boolean contains(Object o)
  {
    if (!(o instanceof DataObject)) {
      return false;
    }
    return super.contains(o);
  }

  @Deprecated
  public DataObject set(int index, DataObject o)
  {
    if (o != null) {
      try {
        checkDataObjectKeys(o);
      } catch (Exception e) {
        return null;
      }
    }
    return ((DataObject)super.set(index, o));
  }

  @Deprecated
  public boolean add(DataObject o)
  {
    if (o != null) {
      try {
        checkDataObjectKeys(o);
      } catch (Exception e) {
        return false;
      }
    }
    return super.add(o);
  }

  @Deprecated
  public void add(int index, DataObject o)
  {
    if (o != null) {
      try {
        checkDataObjectKeys(o);
      } catch (Exception e) {
        return;
      }
    }
    super.add(index, o);
  }

  @Deprecated
  public boolean addAll(Collection<? extends DataObject> c)
  {
    for (DataObject o : c) {
      try {
        checkDataObjectKeys(o);
      } catch (Exception e) {
        return false;
      }
    }
    return super.addAll(c);
  }

  @Deprecated
  public boolean addAll(int index, Collection<? extends DataObject> c)
  {
    for (DataObject o : c) {
      try {
        checkDataObjectKeys(o);
      } catch (Exception e) {
        return false;
      }
    }
    return super.addAll(index, c);
  }

  public final int rowCount() {
    return super.size();
  }

  public Object delRow(int row) throws Exception {
    checkRow(row);
    return super.remove(row);
  }

  public boolean containsItem(int row, String column) throws Exception {
    return getRow(row).containsKey(column);
  }

  public DataObject getRow(int row) throws Exception {
    checkRow(row);
    Object o = super.get(row);
    if (o instanceof DataObject) {
      return ((DataObject)o);
    }
    System.out.println("第[" + row + "]行取出的数据不能转换成DataObject");
    return null;
  }

  public void clear()
  {
    super.clear();
    this.columnTypeMap = null;
  }

  public void addRow() throws Exception {
    add(new DataObject());
  }

  public void addRow(DataObject o) {
    add(o);
  }

  public void insertRow(int row, DataObject o) throws Exception {
    if (row != rowCount()) {
      checkRow(row);
    }
    super.add(row, o);
    if (o != null)
      checkDataObjectKeys(o);
  }

  public void insertRow(int row) throws Exception
  {
    insertRow(row, new DataObject());
  }

  public void insertRowWithDefaultColumns(int row) throws Exception {
    insertRow(row, getRowWithDefaultColumns());
  }

  public void addRowWithDefaultColumns() throws Exception {
    addRow(getRowWithDefaultColumns());
  }

  public Object put(int row, String column, Object value) throws Exception {
    if (row == rowCount())
      addRow();
    else {
      checkRow(row);
    }
    DataObject dbo = getRow(row);
    Object tmp = dbo.put(column, value);

    checkAndSaveColumnType(column, value);
    return tmp;
  }

  public Object put(int row, String column, double value) throws Exception {
    return put(row, column, new Double(value));
  }

  public Object put(int row, String column, int value) throws Exception {
    return put(row, column, new Integer(value));
  }

  public Object put(int row, String column, boolean value) throws Exception
  {
    return put(row, column, new Boolean(value));
  }

  public Object getObject(int row, String column) throws Exception {
    return getRow(row).getObject(column);
  }

  public String getString(int row, String column) throws Exception {
    return getRow(row).getString(column);
  }

  public double getDouble(int row, String column) throws Exception {
    return getRow(row).getDouble(column);
  }

  public int getInt(int row, String column) throws Exception {
    return getRow(row).getInt(column);
  }

  public String getBigDecimalAsString(int row, String column) throws Exception {
    return getRow(row).getBigDecimalAsString(column);
  }

  public BigDecimal getBigDecimal(int row, String column) throws Exception {
    return getRow(row).getBigDecimal(column);
  }

  public Integer getIntClass(int row, String column) throws Exception {
    return getRow(row).getIntClass(column);
  }

  public boolean getBoolean(int row, String column) throws Exception {
    return getRow(row).getBoolean(column);
  }

  public Date getDate(int row, String column) throws Exception {
    return getRow(row).getDate(column);
  }

  public DataStore subDataStore(int beginRow, int endRow) throws Exception {
    if (rowCount() == 0)
      return null;
    checkRow(beginRow);
    if (endRow != rowCount()) {
      checkRow(endRow);
    }
    DataStore newDataStore = new DataStore(endRow - beginRow + 1);
    for (int i = beginRow; i < endRow; ++i) {
      newDataStore.addRow(getRow(i));
    }
    newDataStore.setTypeList(getTypeList());
    return newDataStore;
  }

  public DataStore combineDatastore(DataStore otherds) throws Exception {
    DataObject row = null;
    for (int i = 0; i < otherds.rowCount(); ++i) {
      row = otherds.getRow(i).clone();
      addRow(row);
    }
    return this;
  }

  public void setTypeList(HashMap<String, String> typeList)
  {
    LinkedHashMap typelist = new LinkedHashMap();
    for (String keyword : typeList.keySet()) {
      typelist.put(keyword, typeList.get(keyword));
    }
    this.columnTypeMap = typelist;
  }

  public void setTypeList(LinkedHashMap<String, String> typeList) {
    this.columnTypeMap = typeList;
  }

  public void setTypeList(String typeList) throws Exception {
    if ((typeList == null) || ("".equals(typeList))) {
      System.out.println("输入的typelist为空");
    }
    String[] tmpList = typeList.split(",");

    for (int i = 0; i < tmpList.length; ++i) {
      if (tmpList[i] == null) continue; if ("".equals(tmpList[i])) {
        continue;
      }
      if ((tmpList[i].split(":") == null) || 
        (tmpList[i]
        .split(":").length != 
        2)) {
        System.out.println("typeList的结构不对，正确的结构应该是:colName:coltype,colName:coltype");
      }
      String vcolName = tmpList[i].split(":")[0];
      String vcolType = tmpList[i].split(":")[1];

      if (this.columnTypeMap == null) {
        this.columnTypeMap = new LinkedHashMap();
      }
      this.columnTypeMap.put(vcolName.toLowerCase(), 
        convertTypeAbbreviation(vcolType));
    }
  }

  public String getTypeList() throws Exception
  {
    if (this.columnTypeMap == null)
      return null;
    StringBuffer sb = new StringBuffer();
    for (String colName : this.columnTypeMap.keySet()) {
      String colType = convertTypeAbbreviation(
        (String)this.columnTypeMap
        .get(colName));

      sb.append(colName).append(":").append(colType).append(",");
    }

    String typeList = sb.toString();

    if ((typeList != null) && (typeList.endsWith(","))) {
      typeList = typeList.substring(0, typeList.length() - 1);
    }
    return typeList;
  }

  public String[] getColumnName() throws Exception {
    if (this.columnTypeMap == null) {
      return null;
    }
    String[] colNames = new String[this.columnTypeMap.keySet().size()];
    int i = 0;
    for (String tmp : this.columnTypeMap.keySet()) {
      colNames[i] = tmp;
      ++i;
    }
    return colNames;
  }

  public String getColumnType(String colName)
    throws Exception
  {
    String columnType = "null";
    columnType = (String)this.columnTypeMap.get(colName.toLowerCase());

    if (("date".equals(columnType)) || ("d".equals(columnType)))
      columnType = "date";
    else if (("number".equals(columnType)) || 
      ("n"
      .equals(columnType)))
    {
      columnType = "number";
    } else if (("boolean".equals(columnType)) || 
      ("b"
      .equals(columnType)))
    {
      columnType = "boolean";
    } else if (("string".equals(columnType)) || 
      ("s"
      .equals(columnType)))
    {
      columnType = "string";
    } else if (("blob".equals(columnType)) || 
      ("blob"
      .equals(columnType)))
    {
      columnType = "blob";
    } else if (("clob".equals(columnType)) || 
      ("clob"
      .equals(columnType)))
    {
      columnType = "clob";
    }
    else {
      columnType = "string";
    }
    return columnType;
  }

  public LinkedHashMap<String, String> getTypeMap() throws Exception {
    return this.columnTypeMap;
  }

  public DataStore sort(String colName) throws Exception {
    if (rowCount() == 0)
      return this;
    Collections.sort(this, new DataObjectComparator(colName));
    return this;
  }

  public DataStore sortdesc(String column) throws Exception {
    if (rowCount() == 0)
      return this;
    Collections.sort(this, 
      Collections.reverseOrder(new DataObjectComparator(column)));

    return this;
  }

  public DataStore clone() {
    DataStore vd = new DataStore();
    try {
      vd.setTypeList(getTypeList());
      if (rowCount() == 0)
        return vd;
      for (int i = 0; i < size(); ++i)
        vd.add(getRow(i).clone());
    }
    catch (Exception e1)
    {
      return vd;
    }
    return vd;
  }

  @Deprecated
  public Object delItem(int row, String column) throws Exception
  {
    return getRow(row).remove(column);
  }

  public void delColumn(String columnName) throws Exception {
    int i = 0; for (int count = size(); i < count; ++i) {
      if (containsItem(i, columnName)) {
        delItem(i, columnName);
      }
    }
    if ((this.columnTypeMap == null) || 
      (!(this.columnTypeMap
      .containsKey(columnName))))
      return;
    this.columnTypeMap.remove(columnName);
  }

  public Collection<Object[]> ds2Collection() throws Exception
  {
    return ds2Collection(this);
  }

  public Collection<Object[]> ds2Collection(DataStore ds) throws Exception {
    if (ds == null)
      return null;
    Collection collection = new ArrayList();

    String[] colnames = ds.getColumnName();
    for (DataObject dataObject : ds) {
      Object[] objects = new Object[colnames.length];
      for (int j = 0; j < colnames.length; ++j) {
        if (!(dataObject.containsKey(colnames[j]))) {
          objects[j] = null;
        }
        else {
          objects[j] = dataObject.get(colnames[j]);
        }
      }
      collection.add(objects);
    }
    return collection;
  }

  public Collection<Object> getColumn(String column) throws Exception {
    ArrayList l = new ArrayList();
    for (int i = 0; i < rowCount(); ++i) {
      l.add(i, getObject(i, column));
    }
    return l;
  }

  public String expToString() throws Exception {
    StringBuffer vstr = new StringBuffer("");
    StringBuffer vline = null;
    for (DataObject vdo : this) {
      String[] colNames = getColumnName();
      vline = new StringBuffer("");
      for (int i = 0; i < colNames.length; ++i) {
        String vcolumnvalue = null;
        if (getColumnType(colNames[i]).equals("string")) {
          vcolumnvalue = vdo.getString(colNames[i]);
          if (vcolumnvalue == null)
            vcolumnvalue = "";
        }
        else if (getColumnType(colNames[i]).equals("date")) {
          Date vdate = vdo.getDate(colNames[i]);
          if (vdate == null)
            vcolumnvalue = "";
          else
            vcolumnvalue = DateUtil.FormatDate(vdate);
        }
        else if (getColumnType(colNames[i]).equals("number")) {
          double vdouble = vdo.getDouble(colNames[i]);
          vcolumnvalue = vdouble+"";
        } else {
          vcolumnvalue = vdo.get(colNames[i]).toString();
          if (vcolumnvalue == null) {
            vcolumnvalue = "";
          }
        }

        vcolumnvalue = vcolumnvalue.replaceAll("\n", "`n");
        vcolumnvalue = vcolumnvalue.replaceAll("\t", "`t");
        vcolumnvalue = vcolumnvalue.replaceAll("\r", "`r");
        if (i < colNames.length)
          vline = vline.append(vcolumnvalue + "\t");
        else {
          vline = vline.append(vcolumnvalue);
        }
      }
      vstr = vstr.append(vline.append("\n"));
    }
    return vstr.toString();
  }



  private void checkDataObjectKeys(DataObject o) throws Exception {
    if (o != null) {
      Iterator it = o.keySet().iterator();
      while (it.hasNext()) {
        String colName = (String)it.next();
        checkAndSaveColumnType(colName, o.get(colName));
      }
    }
  }

  private void checkAndSaveColumnType(String colName, Object colData) {
    if (this.columnTypeMap == null) {
      this.columnTypeMap = new LinkedHashMap();
    }
    colName = colName.toLowerCase();

    if ((this.columnTypeMap.containsKey(colName)) && 
      (!(((String)this.columnTypeMap
      .get(colName))
      .equals("null")))) return;
    this.columnTypeMap.put(colName, getObjectType(colData));
  }

  private void checkRow(int row) throws Exception
  {
    if ((row < 0) || (row >= rowCount()))
      System.out.println("无效行号:" + row + ",当前DataStore共有" + rowCount() + "行");
  }

  private static String getObjectType(Object o)
  {
    String type = "null";
    String cname = null;
    if (o == null) {
      return type;
    }
    cname = o.getClass().getName();
    if (cname.equals("java.lang.String"))
      type = "string";
    else if ((cname.equals("java.lang.Double")) || 
      (cname
      .equals("java.lang.Integer")))
    {
      type = "number";
    } else if (cname.equals("java.lang.Boolean"))
      type = "boolean";
    else if ((cname.equals("java.lang.Long")) || 
      (cname
      .equals("java.math.BigDecimal")))
    {
      type = "number";
    } else if ((cname.equals("java.util.Date")) || 
      (cname
      .equals("java.sql.Date")) || 
      (cname
      .equals("java.sql.Timestamp")))
    {
      type = "date";
    } else if (o instanceof Blob)
      type = "blob";
    else if (o instanceof Clob) {
      type = "clob";
    }
    return type;
  }

  private static String convertTypeAbbreviation(String colType) {
    if (colType == null) {
      return null;
    }
    if (colType.equals("string"))
      return "s";
    if (colType.equals("number"))
      return "n";
    if (colType.equals("date"))
      return "d";
    if (colType.equals("boolean"))
      return "b";
    if (colType.equals("null"))
      return "l";
    if (colType.equals("s"))
      return "string";
    if (colType.equals("n"))
      return "number";
    if (colType.equals("d"))
      return "date";
    if (colType.equals("b"))
      return "boolean";
    if (colType.equals("l"))
      return "null";
    if (colType.equals("blob"))
      return "blob";
    if (colType.equals("blob"))
      return "blob";
    if (colType.equals("clob"))
      return "clob";
    if (colType.equals("clob")) {
      return "clob";
    }
    return null;
  }

  private DataObject getRowWithDefaultColumns() throws Exception {
    DataObject dob = new DataObject();

    String[] cols = getColumnName();
    for (int i = 0; i < cols.length; ++i) {
      dob.put(cols[i], null);
    }
    return dob;
  }

  public String toJSON() throws Exception {
    try {
      JSONArray jArrData = new JSONArray();
      for (int i = 0; i < size(); ++i) {
        DataObject row = getRow(i);
        jArrData.put(new JSONObject(row.toJSON()));
      }
      return jArrData.toString();
    } catch (JSONException ex) {
      throw new Exception(ex);
    }
  }
}