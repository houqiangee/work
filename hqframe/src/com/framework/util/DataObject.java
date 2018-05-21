package com.framework.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class DataObject extends HashMap implements Serializable {
	private static final long serialVersionUID = 1466923519690153347L;

	public DataObject() {
	}

	public DataObject(HashMap map) {
		super(map);
	}

	public String[] getValueTypes() {
		if (size() == 0)
			return null;
		Collection values = values();
		String[] types = new String[values.size()];
		Object[] valuesArray = values.toArray();
		for (int i = 0; i < valuesArray.length; ++i) {
			types[i] = valuesArray[i].getClass().getName();
		}
		return types;
	}

	public DataObject clone() {
		return ((DataObject) super.clone());
	}

	public Object put(String name, Object value) {
		name = name.toLowerCase();
		if (value instanceof java.sql.Date) {
			value = new java.util.Date(((java.sql.Date) value).getTime());
		}
		if (value instanceof Timestamp) {
			value = new java.util.Date(((Timestamp) value).getTime());
		}
		return super.put(name, value);
	}

	public Object put(String name, int value) {
		return put(name, new Integer(value));
	}

	public Object put(String name, double value) {
		return put(name, new Double(value));
	}

	public Object put(String name, boolean value) {
		return put(name, new Boolean(value));
	}

	public Object getObject(String name) {
		return get(name);
	}

	public Object getObject(String name, Object pdefault) {
		if (!(super.containsKey(name.toLowerCase()))) {
			if (!(super.containsKey(name + "$result".toLowerCase()))) {
				return pdefault;
			}
			name = name + "$result";
		}
		return super.get(name.toLowerCase());
	}

	@Deprecated
	public Object getObjectPara(String name) {
		return getObject(name, null);
	}

	public double getDouble(String name) {
		Object o = getObject(name);
		if ((o == null) || (o.equals(""))) {
			return 0.0D;
		}
		if (o instanceof Double) {
			return ((Double) o).doubleValue();
		}
		return Double.parseDouble(o.toString());
	}

	public double getDouble(String name, double pdefault) {
		Object o = getObject(name, Double.valueOf(String.valueOf(pdefault)));
		if ((o == null) || (o.equals(""))) {
			return pdefault;
		}
		if (o instanceof Double) {
			return ((Double) o).doubleValue();
		}
		return Double.parseDouble(o.toString());
	}

	public int getInt(String name) {
		Object o = getObject(name);
		if ((o == null) || (o.equals(""))) {
			return 0;
		}
		if (o instanceof Integer) {
			return ((Integer) o).intValue();
		}
		return Integer.parseInt(o.toString());
	}

	public String getBigDecimalAsString(String name) {
		Object o = getObject(name);

		if ((o == null) || (o.equals(""))) {
			return "";
		}

		if (o instanceof BigDecimal)
			return ((BigDecimal) o).toString();
		if (o instanceof Double) {
			return new BigDecimal(Double.toString(((Double) o).doubleValue())).toString();
		}
		return new BigDecimal(o.toString()).toString();
	}

	public String getBigDecimalAsString(String name, String pdefault) {
		Object o = getObject(name, pdefault);

		if ((o == null) || (o.equals(""))) {
			return pdefault;
		}

		if (o instanceof BigDecimal)
			return ((BigDecimal) o).toString();
		if (o instanceof Double) {
			return new BigDecimal(Double.toString(((Double) o).doubleValue())).toString();
		}
		return new BigDecimal(o.toString()).toString();
	}

	public BigDecimal getBigDecimal(String name) {
		Object o = getObject(name);

		if ((o == null) || (o.equals(""))) {
			return null;
		}

		if (o instanceof BigDecimal)
			return ((BigDecimal) o);
		if (o instanceof Double) {
			return new BigDecimal(Double.toString(((Double) o).doubleValue()));
		}
		return new BigDecimal(o.toString());
	}

	public BigDecimal getBigDecimal(String name, BigDecimal pdefault) {
		Object o = getObject(name, pdefault);

		if ((o == null) || (o.equals(""))) {
			return pdefault;
		}

		if (o instanceof BigDecimal)
			return ((BigDecimal) o);
		if (o instanceof Double) {
			return new BigDecimal(Double.toString(((Double) o).doubleValue()));
		}
		return new BigDecimal(o.toString());
	}

	public Integer getIntClass(String name)

	{
		Object o = getObject(name);
		if (o == null) {
			return null;
		}
		if (o.equals("")) {
			return Integer.valueOf("0");
		}
		if (o instanceof Integer) {
			return ((Integer) o);
		}
		return Integer.valueOf(o.toString());
	}

	public int getInt(String name, int pdefault)

	{
		Object o = getObject(name, Double.valueOf(String.valueOf(pdefault)));
		if ((o == null) || (o.equals(""))) {
			return pdefault;
		}
		if (o instanceof Integer) {
			return ((Integer) o).intValue();
		}
		return Integer.valueOf(o.toString());
	}

	public boolean getBoolean(String name) {
		Object o = getObject(name);
		if (o == null) {
			return false;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		}
		if ("true".equals(o.toString().toLowerCase()))
			return true;
		if ("false".equals(o.toString().toLowerCase())) {
			return false;
		}
		return false;
	}

	public boolean getBoolean(String name, boolean pdefault)

	{
		Object o = getObject(name, Boolean.valueOf(pdefault));
		if ((o == null) || (o.equals(""))) {
			return pdefault;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		}
		if ("true".equals(o.toString().toLowerCase()))
			return true;
		if ("false".equals(o.toString().toLowerCase())) {
			return false;
		}
		return pdefault;
	}

	public java.util.Date getDate(String name) {
		Object o = getObject(name);
		if ((o == null) || (o.toString().equals(""))) {
			return null;
		}
		if (o instanceof java.util.Date) {
			return ((java.util.Date) o);
		}
		return null;
	}

	public java.util.Date getDate(String name, java.util.Date pdefault)

	{
		Object o = getObject(name, pdefault);
		if ((o == null) || (o.toString().equals(""))) {
			return null;
		}
		if (o instanceof java.util.Date) {
			return ((java.util.Date) o);
		}
		return pdefault;
	}

	public String getString(String name) {
		Object o = getObject(name);
		if (o == null) {
			return null;
		}
		if (o instanceof String) {
			return ((String) o);
		}
		return o + "";
	}

	public String getString(String name, String pdefault)

	{
		Object o = getObject(name, pdefault);
		if (o == null) {
			return null;
		}
		if (o instanceof String) {
			return ((String) o);
		}
		return o + "";
	}

	public DataObject getDataObject(String name) {
		Object o = getObject(name);
		if ((o == null) || (o.toString().equals(""))) {
			return null;
		}
		if (o instanceof DataObject) {
			return ((DataObject) o);
		}
		return null;
	}

	/** @deprecated */
	public static String dataObjectTostring(DataObject parm)

	{
		String name = "";
		String value = "";
		String result = "";
		Object[] array = parm.keySet().toArray();
		for (int i = 0; i < array.length; ++i) {
			name = array[i].toString().toLowerCase();
			if (parm.getObject(name) != null)
				value = parm.getObject(name).toString();
			else {
				value = "`k";
			}
			result = result + "\n" + name + "\t" + value;
		}
		return result;
	}

	public String toJSON() {
		try {
			JSONObject jobj = new JSONObject();
			Set keySet = keySet();
			Iterator iterator = keySet.iterator();
			while (iterator.hasNext()) {
				String varName = (String) iterator.next();
				Object varValue = get(varName);
				if (varValue == null)
					jobj.put(varName, "");
				else if (varValue instanceof Double)
					jobj.put(varName, ((Double) varValue).doubleValue());
				else if (varValue instanceof java.util.Date)
					jobj.put(varName, DateUtil.FormatDate((java.util.Date) varValue, "yyyyMMddHHmmss"));
				else if (varValue instanceof Number)
					jobj.put(varName, (Number) varValue);
				else if (varValue instanceof Boolean)
					jobj.put(varName, (Boolean) varValue);
				else {
					jobj.put(varName, varValue.toString());
				}
			}
			return jobj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static DataObject parseJSON(String json) {
		DataObject dto = new DataObject();
		try {
			JSONObject jObject = new JSONObject(json);
			Iterator iterator = jObject.keys();
			while (iterator.hasNext()) {
				String varName = (String) iterator.next();
				Object varValue = jObject.get(varName);
				if (varValue == null)
					dto.put(varName, "");
				else if (varValue instanceof Double)
					dto.put(varName, ((Double) varValue).doubleValue());
				else if (varValue instanceof java.util.Date)
					dto.put(varName, DateUtil.FormatDate((java.util.Date) varValue, "yyyyMMddHHmmss"));
				else if (varValue instanceof Number)
					dto.put(varName, (Number) varValue);
				else if (varValue instanceof Boolean)
					dto.put(varName, (Boolean) varValue);
				else
					dto.put(varName, varValue.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dto;
	}
}