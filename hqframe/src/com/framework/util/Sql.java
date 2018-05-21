package com.framework.util;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

public class Sql implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int MAX_ROW_CAPACITY = 3000;
	private static final int JDBC_FETCH_SIZE = 1000;
	private String sqlString = null;
	private ArrayList<Object> para = null;
	private String dbName = null;
	private JdbcTemplate jdbcTemplate = null;
	private ArrayList<Object[]> batchParaList = null;
	private boolean batchFlag = false;
	private int paraCount = 0;

	public Sql() {
		this.dbName = "dataSource";
	}

	public Sql(String dbName) {
		this.dbName = dbName;
	}

	public void setSql(String sqlstmt) {
		this.sqlString = sqlstmt;
		this.para = new ArrayList();
		this.batchParaList = null;
		this.paraCount = 0;
	}

	public final String getSql() {
		return this.sqlString;
	}

	private void handlePara(int index, Object value) {
		if (index > this.para.size())
			this.para.add(index - 1, value);
		else
			this.para.set(index - 1, value);
	}

	public void setString(int index, String value) {
		if (value == null)
			setNull(index, 12);
		else
			handlePara(index, value);
	}

	@Deprecated
	public void setStringPara(String value) {
		this.paraCount += 1;
		setString(this.paraCount, value);
	}

	public void setBigDecimal(int index, BigDecimal value) {
		if (value == null)
			setNull(index, 12);
		else
			handlePara(index, value);
	}

	public void setInt(int index, int value) {
		handlePara(index, new Integer(value));
	}

	@Deprecated
	public void setIntPara(int value) {
		this.paraCount += 1;
		setInt(this.paraCount, value);
	}

	public void setInt(int index, Integer value) {
		if (value == null)
			setNull(index, 12);
		else
			handlePara(index, value);
	}

	@Deprecated
	public void setIntPara(Integer value) {
		this.paraCount += 1;
		setInt(this.paraCount, value);
	}

	public void setClob(int index, String value) {
		if (value == null)
			setNull(index, -1);
		else
			handlePara(index, new ClobValue(value));
	}

	@Deprecated
	public void setClobPara(String value) {
		this.paraCount += 1;
		setClob(this.paraCount, value);
	}

	public void setBlob(int index, String value) {
		if (value == null)
			setNull(index, -2);
		else
			handlePara(index, new BlobValue(value));
	}

	@Deprecated
	public void setBlobPara(String value) {
		this.paraCount += 1;
		setBlob(this.paraCount, value);
	}

	public void setBlob(int index, byte[] value) {
		if (value == null)
			setNull(index, -2);
		else
			handlePara(index, new BlobValue(value));
	}

	@Deprecated
	public void setBlobPara(byte[] value) {
		this.paraCount += 1;
		setBlob(this.paraCount, value);
	}

	public void setDouble(int index, double value) {
		handlePara(index, new Double(value));
	}

	@Deprecated
	public void setDoublePara(double value) {
		this.paraCount += 1;
		setDouble(this.paraCount, value);
	}

	public void setDouble(int index, Double value) {
		if (value == null)
			setNull(index, 12);
		else
			handlePara(index, value);
	}

	@Deprecated
	public void setDoublePara(Double value) {
		this.paraCount += 1;
		setDouble(this.paraCount, value);
	}

	public void setBoolean(int index, boolean value) {
		handlePara(index, Boolean.valueOf(value));
	}

	@Deprecated
	public void setBooleanPara(boolean value) {
		this.paraCount += 1;
		setBoolean(this.paraCount, value);
	}

	public void setDate(int index, java.util.Date value) {
		if (value == null)
			setNull(index, 91);
		else
			handlePara(index, new java.sql.Date(value.getTime()));
	}

	@Deprecated
	public void setDatePara(java.util.Date value) {
		this.paraCount += 1;
		setDate(this.paraCount, value);
	}

	public void setDateTime(int index, java.util.Date value) {
		if (value == null)
			setNull(index, 91);
		else
			handlePara(index, new Timestamp(value.getTime()));
	}

	@Deprecated
	public void setDateTimePara(java.util.Date value) {
		this.paraCount += 1;
		setDateTime(this.paraCount, value);
	}

	public void setTimestamp(int index, Timestamp value) {
		if (value == null)
			setNull(index, 93);
		else
			handlePara(index, value);
	}

	@Deprecated
	public void setTimestampPara(Timestamp value) {
		this.paraCount += 1;
		setTimestamp(this.paraCount, value);
	}

	public void setLongVarchar(int index, String value) {
		if (value == null)
			setNull(index, -1);
		else
			handlePara(index, new StringBuffer(value));
	}

	@Deprecated
	public void setLongVarcharPara(String value) {
		this.paraCount += 1;
		setLongVarchar(this.paraCount, value);
	}

	public void setNull(int index, int sqlType) {
		handlePara(index, new NullValue(sqlType));
	}

	@Deprecated
	public void setNullPara(int sqlType) {
		this.paraCount += 1;
		setNull(this.paraCount, sqlType);
	}

	private boolean isSqlContainsUpdate() {
		if (this.sqlString == null) {
			return false;
		}

		return (this.sqlString.contains("for update"));
	}

	private void startTraceSql()

	{
		StringBuffer paraStr = new StringBuffer();
		for (int i = 0; i < this.para.size(); ++i) {
			if (this.para.get(i) == null)
				paraStr.append("null,\n");
			else if (this.para.get(i) instanceof NullValue)
				paraStr.append("null,\n");
			else {
				paraStr.append(this.para.get(i).toString() + "\n");
			}
		}
	}

	public DataStore executeQuery() throws Exception {
		startTraceSql();

		DataStore ds = null;
		if (this.batchFlag == true) {
			throw new Exception("当前SQL类已经设置了Batch参数，不能执行executeQuery，请重新实例化SQL或执行ExecuteBatch或执行resetBatch方法。");
		}

		String beginDate = DateUtil.getCurrentDateToString("yyyy-MM-dd hh:mm:ss SSS");

		ds = executeSelectSQL(this.sqlString, this.para);

		String endDate = DateUtil.getCurrentDateToString("yyyy-MM-dd hh:mm:ss SSS");

		return ds;
	}

	private DataStore executeSelectSQL(String sql, ArrayList<Object> para) throws InvalidResultSetAccessException, Exception, SQLException {
		final ArrayList exePara = para;

		PreparedStatementSetter pss = new PreparedStatementSetter() {
			public void setValues(PreparedStatement pstmt) throws SQLException {
				ParameterMetaData pmd = pstmt.getParameterMetaData();
				int requiredParaCount = pmd.getParameterCount();
				Object[] paras = exePara.toArray();
				try {
					Sql.this.setParas(pstmt, paras, requiredParaCount);
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		};
		ResultSetExtractor resultSetExtractor = new ResultSetExtractor() {
			public DataStore extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				LinkedHashMap typelist = Sql.this.generateTypeListFromResultSetMetadata(rsmd);
				DataStore ds = new DataStore();
				ds.setTypeList(typelist);
				try {
					while (rs.next())
						for (int j = 0; j < columnCount; ++j) {
							String columnName = rsmd.getColumnName(j + 1).toLowerCase();

							if ("rowid".equals(columnName)) {
								Object rowId = rs.getObject(j + 1);
								ds.put(rs.getRow() - 1, columnName, rs.getString(j + 1));
							} else if (rsmd.getColumnType(j + 1) == -1) {
								String longVarCharValue = String.valueOf(rs.getByte(j + 1));

								ds.put(rs.getRow() - 1, columnName, longVarCharValue);
							} else if (rsmd.getColumnType(j + 1) == 93) {
								Object objValue = rs.getObject(j + 1);
								if ((objValue == null) || (objValue instanceof Timestamp)) {
									ds.put(rs.getRow() - 1, columnName, objValue);
								} else {
									throw new Exception("在往DataStore中存储TIMESTAMP类型数据时，从SQL中得到了不可识别的数据类型【" + objValue.getClass().getName() + "】!");
								}
							} else {
								Object a = rs.getObject(j + 1);
								ds.put(rs.getRow() - 1, columnName, a);
							}
						}
				} catch (Exception e) {
					throw new SQLException(e);
				}
				return ds;
			}

		};
		this.jdbcTemplate = DatabaseSessionUtil.getCurrentSession(this.dbName);
		this.jdbcTemplate.setFetchSize(1000);
		DataStore ds = (DataStore) this.jdbcTemplate.query(sql, pss, resultSetExtractor);
		return ds;
	}

	private LinkedHashMap<String, String> generateTypeListFromResultSetMetadata(ResultSetMetaData rsmd) throws SQLException {
		String[] column = new String[rsmd.getColumnCount()];
		LinkedHashMap typeList = new LinkedHashMap();
		for (int i = 0; i < column.length; ++i) {
			column[i] = rsmd.getColumnName(i + 1);
			column[i] = column[i].toLowerCase();
			int type = rsmd.getColumnType(i + 1);
			if ((type == 1) || (type == 12) || (type == -1)) {
				typeList.put(column[i], "string");
			} else if ((type == 2) || (type == 4))
				typeList.put(column[i], "number");
			else if ((type == 91) || (type == 92) || (type == 93)) {
				typeList.put(column[i], "date");
			} else if (type == 16)
				typeList.put(column[i], "boolean");
			else if (type == 2004)
				typeList.put(column[i], "blob");
			else if (type == 2005)
				typeList.put(column[i], "clob");
			else {
				typeList.put(column[i], "null");
			}
		}
		return typeList;
	}

	public int executeUpdate() {
		startTraceSql();
		int vi = 0;
		try {
			if (this.batchFlag == true) {
			}

			String beginDate = DateUtil.getCurrentDateToString("yyyy-MM-dd hh:mm:ss SSS");

			this.jdbcTemplate = DatabaseSessionUtil.getCurrentSession(this.dbName);
			Transaction trans = TransactionManager.getTransaction(this.dbName);
			if (!(trans.isUnderTransaction())) {
				System.out.println("当前数据库操作[" + this.sqlString + "]未正常开启事务，请联系开发人员处理。");
			}

			final Object[] exePara = new Object[this.para.size()];
			for (int i = 0; i < this.para.size(); ++i) {
				exePara[i] = this.para.get(i);
			}

			PreparedStatementSetter pss = new PreparedStatementSetter() {
				public void setValues(PreparedStatement pstmt) throws SQLException {
					ParameterMetaData pmd = pstmt.getParameterMetaData();
					int paraCount = pmd.getParameterCount();
					Object[] paras = exePara;
					try {
						Sql.this.setParas(pstmt, paras, paraCount);
					} catch (Exception e) {
						throw new SQLException(e);
					}
				}
			};
			vi = this.jdbcTemplate.update(this.sqlString, pss);

			String endDate = DateUtil.getCurrentDateToString("yyyy-MM-dd hh:mm:ss SSS");

			return vi;
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	private void setParas(PreparedStatement pstmt, Object[] para, int requiredParaCount) {
		if (para == null) {
			if (requiredParaCount > 0) {
				System.out.println("设置的参数个数[0]少于sql要求的参数个数[" + requiredParaCount + "]");
			}

			return;
		}

		if (para.length < requiredParaCount)
			System.out.println("设置的参数个数[" + para.length + "]少于sql要求的参数个数[" + requiredParaCount + "]");
		if (para.length > requiredParaCount)
			System.out.println("设置的参数个数[" + para.length + "]多于sql要求的参数个数[" + requiredParaCount + "]");
		try {
			for (int i = 0; i < para.length; ++i) {
				Object o = para[i];
				if (o instanceof Integer) {
					pstmt.setInt(i + 1, ((Integer) o).intValue());
				} else if (o instanceof Double) {
					pstmt.setDouble(i + 1, ((Double) o).doubleValue());
				} else if (o instanceof Boolean) {
					pstmt.setBoolean(i + 1, ((Boolean) o).booleanValue());
				} else if (o instanceof String) {
					pstmt.setString(i + 1, (String) o);
				} else if (o instanceof java.sql.Date) {
					pstmt.setDate(i + 1, (java.sql.Date) o);
				} else if (o instanceof Timestamp) {
					pstmt.setTimestamp(i + 1, (Timestamp) o);
				} else if (o instanceof java.util.Date) {
					pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) o).getTime()));
				} else if (o instanceof Blob) {
					pstmt.setBlob(i + 1, (Blob) o);
				} else if (o instanceof BlobValue) {
					BlobValue blobValue = (BlobValue) o;
					pstmt.setBinaryStream(i + 1, new ByteArrayInputStream(blobValue.getValue()), blobValue.getLength());
				} else if (o instanceof ClobValue) {
					ClobValue clobValue = (ClobValue) o;
					StringReader r = new StringReader(clobValue.getValue());
					pstmt.setCharacterStream(i + 1, r, clobValue.getLength());
				} else if (o instanceof StringBuffer) {
					StringBuffer longVarCharValue = (StringBuffer) o;
					StringReader reader = new StringReader(longVarCharValue.toString());
					pstmt.setCharacterStream(i + 1, reader, longVarCharValue.toString().length());
				} else if (o instanceof NullValue) {
					pstmt.setNull(i + 1, ((NullValue) o).getType());
				} else if (o instanceof BigDecimal) {
					pstmt.setBigDecimal(i + 1, (BigDecimal) o);
				} else if (o == null) {
					System.out.println("第" + (i + 1) + "个参数未定义");
				} else {
					System.out.println("第" + (i + 1) + "个参数类型不合法");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getSqlString()

	{
		Object[] args = new Object[this.para.size()];

		for (int i = 0; i < this.para.size(); ++i) {
			Object o = this.para.get(i);
			if (o instanceof Integer)
				args[i] = ((Integer) o).toString();
			else if (o instanceof Double)
				args[i] = ((Double) o).toString();
			else if (o instanceof Boolean)
				args[i] = ((Boolean) o).toString();
			else if (o instanceof String)
				args[i] = "'" + ((String) o).replaceAll("'", "''") + "'";
			else if (o instanceof Timestamp) {
				args[i] = "to_date('" + DateUtil.FormatDate((java.util.Date) o, "yyyyMMddHHmmss") + "','yyyymmddhh24miss')";
			} else if (o instanceof java.util.Date) {
				args[i] = "to_date('" + DateUtil.FormatDate((java.util.Date) o, "yyyyMMdd") + "','yyyymmdd')";
			} else if (o instanceof Blob)
				System.out.println("第" + (i + 1) + "个参数类型是Blob，不能转成String");
			else if (o instanceof BlobValue)
				System.out.println("第" + (i + 1) + "个参数类型是BlobValue，不能转成String");
			else if (o instanceof StringReader)
				System.out.println("第" + (i + 1) + "个参数类型是LongVarChar，不能转成String");
			else if (o instanceof BigDecimal)
				args[i] = ((BigDecimal) o).toString();
			else if (o instanceof NullValue)
				args[i] = "null";
			else if (o == null)
				System.out.println("第" + (i + 1) + "个参数未定义");
			else {
				System.out.println("第" + (i + 1) + "个参数类型不合法");
			}

		}

		String rawSqlStr = this.sqlString;
		StringBuffer resultSqlStr = new StringBuffer();

		int quoteMarkScanPos = 0;
		int beginQuoteMarkPos = -1;
		int endQuoteMarkPos = -1;
		int varReplaceNum = 0;
		while (rawSqlStr.indexOf("'", quoteMarkScanPos) != -1) {
			beginQuoteMarkPos = rawSqlStr.indexOf("'", quoteMarkScanPos);
			endQuoteMarkPos = rawSqlStr.indexOf("'", beginQuoteMarkPos + 1);

			String varQuestionMarksStr = rawSqlStr.substring(quoteMarkScanPos, beginQuoteMarkPos);
			String strQuestionMarksStr = rawSqlStr.substring(beginQuoteMarkPos, endQuoteMarkPos + 1);
			int questionMarkScanPos = 0;
			int questionMarkPos = -1;

			while (varQuestionMarksStr.indexOf("?", questionMarkScanPos) != -1) {
				questionMarkPos = varQuestionMarksStr.indexOf("?", questionMarkScanPos);

				resultSqlStr.append(varQuestionMarksStr.substring(questionMarkScanPos, questionMarkPos) + ((String) args[(varReplaceNum++)]));

				questionMarkScanPos = questionMarkPos + 1;
			}

			resultSqlStr.append(varQuestionMarksStr.substring(questionMarkScanPos) + strQuestionMarksStr);

			quoteMarkScanPos = endQuoteMarkPos + 1;
		}

		String varQuestionMarksStr = rawSqlStr.substring(quoteMarkScanPos);
		int questionMarkScanPos = 0;
		int questionMarkPos = -1;
		while (varQuestionMarksStr.indexOf("?", questionMarkScanPos) != -1) {
			questionMarkPos = varQuestionMarksStr.indexOf("?", questionMarkScanPos);

			resultSqlStr.append(varQuestionMarksStr.substring(questionMarkScanPos, questionMarkPos) + ((String) args[(varReplaceNum++)]));

			questionMarkScanPos = questionMarkPos + 1;
		}
		resultSqlStr.append(varQuestionMarksStr.substring(questionMarkScanPos));

		return resultSqlStr.toString();
	}

	private class ClobValue implements Serializable {
		private static final long serialVersionUID = -2900880620133106928L;
		private String value;

		public ClobValue(String paramString) {
			this.value = ((paramString == null) ? "" : paramString);
		}

		public String getValue() {
			return this.value;
		}

		public int getLength() {
			return this.value.length();
		}
	}

	private class BlobValue implements Serializable {
		private static final long serialVersionUID = 4987534051624333379L;
		private byte[] value;

		public BlobValue(byte[] paramArrayOfByte) {
			if (paramArrayOfByte == null) {
				paramArrayOfByte = new byte[0];
			}
			this.value = paramArrayOfByte;
		}

		public BlobValue(String value) {
			if (value == null)
				value = "";
			try {
				this.value = value.getBytes("GBK");
			} catch (Exception e) {
				this.value = value.getBytes();
			}
		}

		public byte[] getValue() {
			return this.value;
		}

		public int getLength() {
			return this.value.length;
		}
	}

	private class NullValue {
		private int type;

		public NullValue(int paramInt) {
			this.type = paramInt;
		}

		public int getType() {
			return this.type;
		}
	}
}