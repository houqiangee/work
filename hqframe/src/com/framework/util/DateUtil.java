package com.framework.util;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.springframework.test.context.jdbc.Sql;

public class DateUtil {
	public static long dbTime = 0L;
	public static long serverStartTime = 0L;
	public static final Object refreshDBTime = new Object();

	public static Date addDay(Date date, int dayNumber)  {
		if ((date == null) || (dayNumber == 0))
			return date;
		Calendar vcal = Calendar.getInstance();
		vcal.setTime(date);
		vcal.add(5, dayNumber);
		date = vcal.getTime();
		return date;
	}

	public static String addDayToString(String dateString, String format, int dayNumber)  {
		Date vdate = stringToDate(dateString, format);
		vdate = addDay(vdate, dayNumber);
		String vdates = dateToString(vdate, format);
		return vdates;
	}

	public static Date addMonth(Date date, int monthNumber)  {
		if ((date == null) || (monthNumber == 0))
			return date;
		Calendar vcal = Calendar.getInstance();
		vcal.setTime(date);
		vcal.add(2, monthNumber);
		date = vcal.getTime();
		return date;
	}

	public static String addMonthToString(String dateString, String format, int monthNumber)  {
		Date vdate = stringToDate(dateString, format);
		vdate = addMonth(vdate, monthNumber);
		String vdates = dateToString(vdate, format);
		return vdates;
	}

	public static String dateToString(Date date)  {
		return dateToString(date, "yyyyMMdd");
	}

	public static String dateTimeToString(Date date)  {
		return dateToString(date, "yyyyMMddHHmmss");
	}

	public static String FormatDate(Date date)  {
		return dateToString(date, "yyyyMMddHHmmss");
	}

	public static String FormatDate(Date date, String format)  {
		return dateToString(date, format);
	}

	public static String dateToString(Date date, String format)  {
		if (date == null) {
			return null;
		}
		Hashtable h = new Hashtable();
		String javaFormat = new String();
		if (format.indexOf("yyyy") != -1)
			h.put(new Integer(format.indexOf("yyyy")), "yyyy");
		else if (format.indexOf("yy") != -1)
			h.put(new Integer(format.indexOf("yy")), "yy");
		if (format.indexOf("MM") != -1)
			h.put(new Integer(format.indexOf("MM")), "MM");
		else if (format.indexOf("mm") != -1)
			h.put(new Integer(format.indexOf("mm")), "MM");
		if (format.indexOf("dd") != -1)
			h.put(new Integer(format.indexOf("dd")), "dd");
		if (format.indexOf("hh24") != -1)
			h.put(new Integer(format.indexOf("hh24")), "HH");
		else if (format.indexOf("hh") != -1)
			h.put(new Integer(format.indexOf("hh")), "HH");
		else if (format.indexOf("HH") != -1) {
			h.put(new Integer(format.indexOf("HH")), "HH");
		}
		if (format.indexOf("mi") != -1)
			h.put(new Integer(format.indexOf("mi")), "mm");
		else if ((format.indexOf("mm") != -1) && (h.containsValue("HH")))
			h.put(new Integer(format.lastIndexOf("mm")), "mm");
		if (format.indexOf("ss") != -1)
			h.put(new Integer(format.indexOf("ss")), "ss");
		if (format.indexOf("SSS") != -1) {
			h.put(new Integer(format.indexOf("SSS")), "SSS");
		}
		for (int intStart = 0; format.indexOf("-", intStart) != -1; ++intStart) {
			intStart = format.indexOf("-", intStart);
			h.put(new Integer(intStart), "-");
		}
		for (int intStart = 0; format.indexOf(".", intStart) != -1; ++intStart) {
			intStart = format.indexOf(".", intStart);
			h.put(new Integer(intStart), ".");
		}
		for (int intStart = 0; format.indexOf("/", intStart) != -1; ++intStart) {
			intStart = format.indexOf("/", intStart);
			h.put(new Integer(intStart), "/");
		}

		for (int intStart = 0; format.indexOf(" ", intStart) != -1; ++intStart) {
			intStart = format.indexOf(" ", intStart);
			h.put(new Integer(intStart), " ");
		}

		for (int intStart = 0; format.indexOf(":", intStart) != -1; ++intStart) {
			intStart = format.indexOf(":", intStart);
			h.put(new Integer(intStart), ":");
		}

		if (format.indexOf("年") != -1)
			h.put(new Integer(format.indexOf("年")), "年");
		if (format.indexOf("月") != -1)
			h.put(new Integer(format.indexOf("月")), "月");
		if (format.indexOf("日") != -1)
			h.put(new Integer(format.indexOf("日")), "日");
		if (format.indexOf("时") != -1)
			h.put(new Integer(format.indexOf("时")), "时");
		if (format.indexOf("分") != -1)
			h.put(new Integer(format.indexOf("分")), "分");
		if (format.indexOf("秒") != -1)
			h.put(new Integer(format.indexOf("秒")), "秒");
		int i = 0;
		while (h.size() != 0) {
			Enumeration e = h.keys();
			int n = 0;
			while (e.hasMoreElements()) {
				i = ((Integer) e.nextElement()).intValue();
				if (i >= n)
					;
				n = i;
			}
			String temp = (String) h.get(new Integer(n));
			h.remove(new Integer(n));
			javaFormat = javaFormat+temp;
		}
		SimpleDateFormat df = new SimpleDateFormat(javaFormat, new DateFormatSymbols());

		return df.format(date);
	}

	public static String descreaseYearMonth(String dateString)  {
		if (dateString == null)
			return null;
		int year = new Integer(dateString.substring(0, 4)).intValue();
		int month = new Integer(dateString.substring(4, 6)).intValue();
		if (--month >= 10)
			return dateString.substring(0, 4) + new Integer(month).toString();
		if ((month > 0) && (month < 10)) {
			return dateString.substring(0, 4) + "0" + new Integer(month).toString();
		}
		return new Integer(year - 1).toString() + new Integer(month + 12).toString();
	}

	public static String descreaseYearMonth(String dateString, int delMonth)  {
		if (dateString == null)
			return null;
		int year = new Integer(dateString.substring(0, 4)).intValue();
		int month = new Integer(dateString.substring(4, 6)).intValue();

		if (delMonth < 0) {
			return increaseYearMonth(dateString, -1 * delMonth);
		}
		month -= delMonth;
		if (month >= 10)
			return dateString.substring(0, 4) + new Integer(month).toString();
		if ((month > 0) && (month < 10)) {
			return dateString.substring(0, 4) + "0" + new Integer(month).toString();
		}
		int yearDec = -1 * month / 12 + 1;
		int month2 = 12 - (-1 * month % 12);
		if (month2 >= 10) {
			return new Integer(year - yearDec).toString() + new Integer(month2).toString();
		}
		return new Integer(year - yearDec).toString() + "0" + new Integer(month2).toString();
	}

	public static int FormatDateToYear(Date date)  {
		return Integer.parseInt(FormatDate(date, "yyyy"));
	}

	public static String FormatDateToYearMonth(Date date)  {
		if (date == null)
			return null;
		return FormatDate(date, "yyyyMM");
	}

	public static String FormatDateToYearMonthDay(Date date)  {
		if (date == null)
			return null;
		return FormatDate(date, "yyyyMMdd");
	}

	public static String getChineseDate(Date date)  {
		if (date == null)
			return null;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", new DateFormatSymbols());

		String dtrDate = df.format(date);
		return dtrDate.substring(0, 4) + "年" + Integer.parseInt(dtrDate.substring(4, 6)) + "月" + Integer.parseInt(dtrDate.substring(6, 8)) + "日";
	}

	public static String getChineseYearAndMonth(String dateString)  {
		if (dateString == null)
			return null;
		String year = dateString.substring(0, 4);
		String month = dateString.substring(4, 6);
		return year + "年" + month + "月";
	}

	public static Date getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}

	public static String getCurrentDateToString()  {
		return getCurrentDateToString("yyyyMMddHHmiss");
	}

	public static String getCurrentDateToString(String strFormat)  {
		return dateToString(getCurrentDate(), strFormat);
	}

	public static int getCurrentYear() {
		Calendar cal = Calendar.getInstance();
		return cal.get(1);
	}

	public static String getCurrentYearMonthToString() {
		Calendar cal = Calendar.getInstance();
		String currentYear = new Integer(cal.get(1)).toString();
		String currentMonth = null;
		if (cal.get(2) < 9)
			currentMonth = "0" + new Integer(cal.get(2) + 1).toString();
		else
			currentMonth = new Integer(cal.get(2) + 1).toString();
		return currentYear + currentMonth;
	}

	public static String getDate(Date date, String format)  {
		if (date == null)
			return null;
		Hashtable h = new Hashtable();
		String javaFormat = new String();
		if (format.indexOf("yyyy") != -1)
			h.put(new Integer(format.indexOf("yyyy")), "yyyy");
		else if (format.indexOf("yy") != -1)
			h.put(new Integer(format.indexOf("yy")), "yy");
		if (format.indexOf("MM") != -1)
			h.put(new Integer(format.indexOf("MM")), "MM");
		else if (format.indexOf("mm") != -1)
			h.put(new Integer(format.indexOf("mm")), "MM");
		if (format.indexOf("dd") != -1)
			h.put(new Integer(format.indexOf("dd")), "dd");
		if (format.indexOf("hh24") != -1)
			h.put(new Integer(format.indexOf("hh24")), "HH");
		else if (format.indexOf("hh") != -1)
			h.put(new Integer(format.indexOf("hh")), "HH");
		else if (format.indexOf("HH") != -1) {
			h.put(new Integer(format.indexOf("HH")), "HH");
		}
		if (format.indexOf("mi") != -1)
			h.put(new Integer(format.indexOf("mi")), "mm");
		else if (format.indexOf("mm") != -1)
			h.put(new Integer(format.indexOf("mm")), "MM");
		if (format.indexOf("ss") != -1)
			h.put(new Integer(format.indexOf("ss")), "ss");
		if (format.indexOf("SSS") != -1)
			h.put(new Integer(format.indexOf("SSS")), "SSS");
		for (int intStart = 0; format.indexOf("-", intStart) != -1; ++intStart) {
			intStart = format.indexOf("-", intStart);
			h.put(new Integer(intStart), "-");
		}
		for (int intStart = 0; format.indexOf(".", intStart) != -1; ++intStart) {
			intStart = format.indexOf(".", intStart);
			h.put(new Integer(intStart), ".");
		}
		for (int intStart = 0; format.indexOf("/", intStart) != -1; ++intStart) {
			intStart = format.indexOf("/", intStart);
			h.put(new Integer(intStart), "/");
		}

		for (int intStart = 0; format.indexOf(" ", intStart) != -1; ++intStart) {
			intStart = format.indexOf(" ", intStart);
			h.put(new Integer(intStart), " ");
		}

		for (int intStart = 0; format.indexOf(":", intStart) != -1; ++intStart) {
			intStart = format.indexOf(":", intStart);
			h.put(new Integer(intStart), ":");
		}

		if (format.indexOf("年") != -1)
			h.put(new Integer(format.indexOf("年")), "年");
		if (format.indexOf("月") != -1)
			h.put(new Integer(format.indexOf("月")), "月");
		if (format.indexOf("日") != -1)
			h.put(new Integer(format.indexOf("日")), "日");
		if (format.indexOf("时") != -1)
			h.put(new Integer(format.indexOf("时")), "时");
		if (format.indexOf("分") != -1)
			h.put(new Integer(format.indexOf("分")), "分");
		if (format.indexOf("秒") != -1)
			h.put(new Integer(format.indexOf("秒")), "秒");
		int i = 0;
		while (h.size() != 0) {
			Enumeration e = h.keys();
			int n = 0;
			while (e.hasMoreElements()) {
				i = ((Integer) e.nextElement()).intValue();
				if (i >= n)
					;
				n = i;
			}
			String temp = (String) h.get(new Integer(n));
			h.remove(new Integer(n));
			javaFormat = temp + javaFormat;
		}
		SimpleDateFormat df = new SimpleDateFormat(javaFormat, new DateFormatSymbols());

		return df.format(date);
	}

	@Deprecated
	public static Date getDateBetween(Date date, int i)  {
		if ((date == null) || (i == 0))
			return date;
		Calendar calo = Calendar.getInstance();
		calo.setTime(date);
		calo.add(5, i);
		return calo.getTime();
	}

	@Deprecated
	public static String getDateBetweenToString(Date date, int i, String strFromat)  {
		Date dateOld = getDateBetween(date, i);
		return getDate(dateOld, strFromat);
	}

	public static long getDayDifferenceBetweenTwoDate(Date beginDate, Date endDate)  {
		long ld1 = beginDate.getTime();
		long ld2 = endDate.getTime();
		long days = (ld2 - ld1) / 86400000L;
		return days;
	}

	public static Date getDBDate()  {
		return new Date();
	}

	public static Date getDBTime()  {
		return new Date();
	}

	public static String getFirstDayOfNextMonth()  {
		String strToday = getCurrentDateToString();
		return increaseYearMonth(strToday.substring(0, 6)) + "01";
	}

	public static String getLastDayOfMonth(String dateString)  {
		if (dateString == null)
			return null;
		int vnf = Integer.parseInt(dateString.substring(0, 4));
		int vyf = Integer.parseInt(dateString.substring(4, 6));
		if (vyf == 2) {
			if (((vnf % 4 == 0) && (vnf % 100 != 0)) || (vnf % 400 == 0)) {
				return "29";
			}
			return "28";
		}

		switch (vyf) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return "31";
		case 4:
		case 6:
		case 9:
		case 11:
			return "30";
		case 2:
		}
		return null;
	}

	public static int getMonthFirstDay(Date pdate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(pdate);
		return calendar.getActualMinimum(5);
	}

	public static int getMonthLastDay(Date pdate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(pdate);
		return calendar.getActualMaximum(5);
	}

	public static String getOracleFormatDateStr(Date d)  {
		return getDate(d, "yyyy-MM-dd hh24:mi:ss");
	}

	public static String getStrHaveAcross(String dateString)  {
		if (dateString == null)
			return null;
		return dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6, 8);
	}

	@Deprecated
	public static Date increaseMonth(Date date, int i) {
		if ((date == null) || (i == 0))
			return date;
		Calendar calo = Calendar.getInstance();
		calo.setTime(date);
		calo.add(2, i);
		return calo.getTime();
	}

	public static Date increaseYear(Date date, int i) {
		if ((date == null) || (i == 0))
			return date;
		Calendar calo = Calendar.getInstance();
		calo.setTime(date);
		calo.add(1, i);
		return calo.getTime();
	}

	@Deprecated
	public static String increaseYearMonth(String dateString)  {
		if (dateString == null)
			return null;
		int year = new Integer(dateString.substring(0, 4)).intValue();
		int month = new Integer(dateString.substring(4, 6)).intValue();
		if ((++month <= 12) && (month >= 10))
			return dateString.substring(0, 4) + new Integer(month).toString();
		if (month < 10) {
			return dateString.substring(0, 4) + "0" + new Integer(month).toString();
		}
		return new Integer(year + 1).toString() + "0" + new Integer(month - 12).toString();
	}

	@Deprecated
	public static String increaseYearMonth(String dateString, int addMonth)  {
		if (dateString == null)
			return null;
		int year = new Integer(dateString.substring(0, 4)).intValue();
		int month = new Integer(dateString.substring(4, 6)).intValue();

		if (addMonth < 0) {
			return descreaseYearMonth(dateString, -1 * addMonth);
		}
		month += addMonth;
		year += month / 12;
		month %= 12;
		if (month == 0) {
			month = 12;
			--year;
		}
		if ((month <= 12) && (month >= 10)) {
			return year + new Integer(month).toString();
		}
		return year + "0" + new Integer(month).toString();
	}

	public static boolean isDate(String dateString) {
		String s = null;
		if (dateString == null)
			return false;
		if ((dateString.length() != 10) && (dateString.length() != 8)) {
			return false;
		}
		if (dateString.length() == 10) {
			s = dateString.substring(0, 4) + dateString.substring(5, 7) + dateString.substring(8, 10);
		} else
			s = dateString;
		try {
			stringToDate(s, "yyyyMMdd");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean isDate(String dateString, String format) {
		if (dateString == null)
			return false;
		if (dateString.length() != format.length())
			return false;
		try {
			stringToDate(dateString, format);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static Date stringToDate(String dateString)  {
		Date vdate = null;
		String vformat = null;
		if (dateString == null)
			return null;
		if (dateString.length() == 4) {
			vformat = "yyyy";
		} else if (dateString.length() == 6) {
			vformat = "yyyyMM";
		} else if (dateString.length() == 7) {
			dateString = dateString.substring(0, 4) + dateString.substring(5, 7);

			vformat = "yyyyMM";
		} else if (dateString.length() == 8) {
			vformat = "yyyyMMdd";
		} else if (dateString.length() == 10) {
			dateString = dateString.substring(0, 4) + dateString.substring(5, 7) + dateString.substring(8, 10);
			vformat = "yyyyMMdd";
		} else if (dateString.length() == 14) {
			vformat = "yyyyMMddHHmmss";
		} else if (dateString.length() == 19) {
			vformat = "yyyy-MM-dd HH:mm:ss";
		}
		vdate = stringToDate(dateString, vformat);
		return vdate;
	}

	public static Date stringToDate(String dateString, String format)  {
		if (dateString == null) {
			return null;
		}
		Hashtable h = new Hashtable();
		if (format.indexOf("yyyy") != -1)
			h.put(new Integer(format.indexOf("yyyy")), "yyyy");
		else if (format.indexOf("yy") != -1)
			h.put(new Integer(format.indexOf("yy")), "yy");
		if (format.indexOf("MM") != -1)
			h.put(new Integer(format.indexOf("MM")), "MM");
		else if (format.indexOf("mm") != -1)
			h.put(new Integer(format.indexOf("mm")), "MM");
		if (format.indexOf("dd") != -1)
			h.put(new Integer(format.indexOf("dd")), "dd");
		if (format.indexOf("hh24") != -1)
			h.put(new Integer(format.indexOf("hh24")), "HH");
		else if (format.indexOf("hh") != -1)
			h.put(new Integer(format.indexOf("hh")), "HH");
		else if (format.indexOf("HH") != -1) {
			h.put(new Integer(format.indexOf("HH")), "HH");
		}
		if (format.indexOf("mi") != -1)
			h.put(new Integer(format.indexOf("mi")), "mm");
		else if ((format.indexOf("mm") != -1) && (h.containsValue("HH")))
			h.put(new Integer(format.lastIndexOf("mm")), "mm");
		if (format.indexOf("ss") != -1)
			h.put(new Integer(format.indexOf("ss")), "ss");
		if (format.indexOf("SSS") != -1) {
			h.put(new Integer(format.indexOf("SSS")), "SSS");
		}
		for (int intStart = 0; format.indexOf("-", intStart) != -1; ++intStart) {
			intStart = format.indexOf("-", intStart);
			h.put(new Integer(intStart), "-");
		}
		for (int intStart = 0; format.indexOf(".", intStart) != -1; ++intStart) {
			intStart = format.indexOf(".", intStart);
			h.put(new Integer(intStart), ".");
		}
		for (int intStart = 0; format.indexOf("/", intStart) != -1; ++intStart) {
			intStart = format.indexOf("/", intStart);
			h.put(new Integer(intStart), "/");
		}

		for (int intStart = 0; format.indexOf(" ", intStart) != -1; ++intStart) {
			intStart = format.indexOf(" ", intStart);
			h.put(new Integer(intStart), " ");
		}

		for (int intStart = 0; format.indexOf(":", intStart) != -1; ++intStart) {
			intStart = format.indexOf(":", intStart);
			h.put(new Integer(intStart), ":");
		}

		if (format.indexOf("年") != -1)
			h.put(new Integer(format.indexOf("年")), "年");
		if (format.indexOf("月") != -1)
			h.put(new Integer(format.indexOf("月")), "月");
		if (format.indexOf("日") != -1)
			h.put(new Integer(format.indexOf("日")), "日");
		if (format.indexOf("时") != -1)
			h.put(new Integer(format.indexOf("时")), "时");
		if (format.indexOf("分") != -1)
			h.put(new Integer(format.indexOf("分")), "分");
		if (format.indexOf("秒") != -1) {
			h.put(new Integer(format.indexOf("秒")), "秒");
		}
		String javaFormat = new String();
		int i = 0;
		while (h.size() != 0) {
			Enumeration e = h.keys();
			int n = 0;
			while (e.hasMoreElements()) {
				i = ((Integer) e.nextElement()).intValue();
				if (i >= n)
					;
				n = i;
			}
			String temp = (String) h.get(new Integer(n));
			h.remove(new Integer(n));
			javaFormat = javaFormat+temp;
		}
		SimpleDateFormat df = new SimpleDateFormat(javaFormat);
		df.setLenient(false);
		Date myDate = new Date();
		try {
			myDate = df.parse(dateString);
		} catch (ParseException e) {
			try {
				df.setLenient(true);

				Calendar c = Calendar.getInstance();
				c.setTime(df.parse(dateString));

				if (((c.get(1) == 1991) && (c.get(2) == 3) && (c.get(5) == 14) && (c.get(11) == 1)) || ((c.get(1) == 1990) && (c.get(2) == 3) && (c.get(5) == 15) && (c.get(11) == 1)) || ((c.get(1) == 1989) && (c.get(2) == 3) && (c.get(5) == 16) && (c.get(11) == 1)) || ((c.get(1) == 1988) && (c.get(2) == 3) && (c.get(5) == 10) && (c.get(11) == 1)) || ((c.get(1) == 1987) && (c.get(2) == 3) && (c.get(5) == 12) && (c.get(11) == 1)) || ((c.get(1) == 1986) && (c.get(2) == 4) && (c.get(5) == 4) && (c.get(11) == 1)) || ((c.get(1) == 1941) && (c.get(2) == 2) && (c.get(5) == 16) && (c.get(11) == 1)) || ((c.get(1) == 1940) && (c.get(2) == 5) && (c.get(5) == 3) && (c.get(11) == 1)))
					myDate = c.getTime();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}

		return myDate;
	}

	public static boolean yearMonthGreatEqual(String beginDate, String endDate)  {
		String temp1 = beginDate.substring(0, 4);
		String temp2 = endDate.substring(0, 4);
		String temp3 = beginDate.substring(4, 6);
		String temp4 = endDate.substring(4, 6);
		if (Integer.parseInt(temp1) > Integer.parseInt(temp2))
			return true;
		if (Integer.parseInt(temp1) == Integer.parseInt(temp2)) {
			return (Integer.parseInt(temp3) >= Integer.parseInt(temp4));
		}
		return false;
	}

	public static boolean yearMonthGreater(String beginDate, String endDate)  {
		String temp1 = beginDate.substring(0, 4);
		String temp2 = endDate.substring(0, 4);
		String temp3 = beginDate.substring(4, 6);
		String temp4 = endDate.substring(4, 6);
		if (Integer.parseInt(temp1) > Integer.parseInt(temp2))
			return true;
		if (Integer.parseInt(temp1) == Integer.parseInt(temp2)) {
			return (Integer.parseInt(temp3) > Integer.parseInt(temp4));
		}
		return false;
	}

	public static String monthToYearMonth(String month)  {
		if (month == null)
			return null;
		String yearMonth = "";
		int smonth = 0;
		int year = 0;
		int rmonth = 0;
		if ("0".equals(month))
			return "0月";
		smonth = Integer.parseInt(month);
		year = smonth / 12;
		rmonth = smonth % 12;
		if (year > 0)
			yearMonth = year + "年";
		if (rmonth > 0)
			yearMonth = yearMonth + rmonth + "个月";
		return yearMonth;
	}

	@Deprecated
	public static int getFirstDayOfMonth(Date date)  {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMinimum(5);
	}

	@Deprecated
	public static int getLastDayOfMonth(Date date)  {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(5);
	}

	public static int getWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(7);
	}

	public static String getChineseWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int i = cal.get(7);
		if (i == 1)
			return "星期天";
		if (i == 2)
			return "星期一";
		if (i == 3)
			return "星期二";
		if (i == 4)
			return "星期三";
		if (i == 5)
			return "星期四";
		if (i == 6)
			return "星期五";
		if (i == 7) {
			return "星期六";
		}
		return "";
	}

}