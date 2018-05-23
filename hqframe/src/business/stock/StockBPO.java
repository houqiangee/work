package business.stock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import oracle.jrockit.jfr.events.DataStructureDescriptor;

import com.framework.layer.BPO;
import com.framework.util.DataStore;
import com.framework.util.DateUtil;
import com.framework.util.HttpRequestUtil;
import com.framework.util.Sql;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;


public class StockBPO extends BPO{
	
	//初始化股票信息
	public static void main(String[] str) throws Exception{
		getStockHis();
	}
	
	//初始化股票信息
	public static void initStockInfor(String[] str) throws Exception{
		System.out.println("ks");
		InputStream input = new FileInputStream(new File("H:/股票列表h.txt"));
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream(); 
		byte[] buff = new byte[100]; //buff用于存放循环读取的临时数据 
		int rc = 0; 
		while ((rc = input.read(buff, 0, 100)) > 0) { 
			swapStream.write(buff, 0, rc); 
		} 
		byte[] in_b = swapStream.toByteArray();
		String stri=new String(in_b);
		String regEx ="<a.*?>(.*?)</a>?";
		
		Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(stri);
        Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
        while(m.find()){
        	String d=m.group();
        	d=d.replaceAll("<.*?>", "");
        	Pattern p1 = Pattern.compile("\\(.*\\)");
            Matcher m1 = p1.matcher(d);
            m1.find();
            String gpdm=m1.group().replace("(", "").replace(")", "");
            String gpmc=d.replaceAll("\\(.*?\\)", "");
        	sql.setSql("insert into stock.stock_list(jysc,gpdm,gpmc) values (?,?,?) ");
        	sql.setString(1, "h");
        	sql.setString(2, gpdm);
        	sql.setString(3, gpmc);
        	sql.executeUpdate();
        }
        tm.commitWithoutStart();
        System.out.println("js");
	}
	
	//获取股票历史
	public static void getStockHis() throws Exception{
		System.out.println("ks");
		String nowstr=DateUtil.dateToString(DateUtil.getDBDate(), "yyyyMMdd");
		
		Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
        String jysc[][]={{"0","h"},{"1","s"}};
        for(int s=0;s<2;s++){
        	sql.setSql("select a.gpdm," +
        			"		   (select max(rq) from stock.stock_day_infor x where x.jysc=a.jysc and x.gpdm=a.gpdm) zhrq" +
        			"	  from stock.stock_list a " +
        			"    where jysc='"+jysc[s][1]+"' order by gpdm ");
        	DataStore gpds=sql.executeQuery();
        	for(int k=0;k<gpds.rowCount();k++){
        		String gpdm=gpds.getString(k, "gpdm");
        		Date rq=gpds.getDate(k, "zhrq");
        		if(rq==null){
        			rq=DateUtil.stringToDate("19901219", "yyyyMMdd");
        		}
        		String rqstr=DateUtil.dateToString(rq,"yyyyMMdd");
        		String url="http://quotes.money.163.com/service/chddata.html?code="+jysc[s][0]+gpdm+"&start="+rqstr+"&end="+nowstr+"&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
        		String restr= HttpRequestUtil.httpRequest_string(url, null, "get", "");
        		restr=new String(restr.getBytes("ISO8859-1"), "GBK");
        		String data[]=restr.split("\n");
        		for(int i=1;i<data.length;i++){
        			if(data[i]==null || "".equals(data[i])){
        				continue;
        			}
        			String one[]=data[i].replace("None", "0").split(",");//0日期,1股票代码,2名称,3收盘价,4最高价,5最低价,6开盘价,7前收盘,8涨跌额,9涨跌幅,10换手率,11成交量,12成交金额,13总市值,14流通市值
        			sql.setSql("insert into stock.stock_day_infor( " +
        					"		   jysc, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz,ltsz) " +
        					"   values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?)");
        			sql.setString(1, jysc[s][1]);
        			sql.setString(2, gpdm);
        			sql.setDate(3, DateUtil.stringToDate(one[0], "yyyy-MM-dd"));
        			sql.setDouble(4, Double.parseDouble(one[3]));
        			sql.setDouble(5, Double.parseDouble(one[4]));
        			sql.setDouble(6, Double.parseDouble(one[5]));
        			sql.setDouble(7, Double.parseDouble(one[6]));
        			sql.setDouble(8, Double.parseDouble(one[7]));
        			sql.setDouble(9, Double.parseDouble(one[8]));
        			sql.setDouble(10, Double.parseDouble(one[9]));
        			sql.setDouble(11, Double.parseDouble(one[11]));
        			sql.setDouble(12, Double.parseDouble(one[12]));
        			sql.setDouble(13, Double.parseDouble(one[10]));
        			sql.setDouble(14, Double.parseDouble(one[13]));
        			sql.setDouble(15, Double.parseDouble(one[14]));
        			sql.executeUpdate();
        		}
        		tm.commit();
        		System.out.println(jysc[s][1]+" "+gpdm+" 完成");
        	}
        }
        System.out.println("js");
	}
}