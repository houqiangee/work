package business.stock;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.framework.layer.BPO;
import com.framework.pic.algorithm.sift.ImageTransform;
import com.framework.pic.algorithm.sift.MyPoint;
import com.framework.util.DataObject;
import com.framework.util.DataStore;
import com.framework.util.DateUtil;
import com.framework.util.HttpRequestUtil;
import com.framework.util.Sql;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;


public class StockBPO extends BPO{
	public static DataStore DATADO=new DataStore();
	
	//对比股票片段与某只股票                            
	public static void getXsgpImg(String gpdm1,String jysc1,int dbts) throws Exception{
		if(dbts<30){
			return;
		}
		Sql sql=new Sql();
		sql.setSql("select * from stock.stock_day_infor where jysc=? and gpdm=? order by rq desc limit ? ");
		sql.setString(1, jysc1);
		sql.setString(2, gpdm1);
		sql.setInt(3, dbts);
		DataStore data1=sql.executeQuery();
		if(data1.rowCount()<30){
			return;
		}
		data1.sort("rq");
		dbts=data1.rowCount();
		
		BufferedImage sor=getStockKImg(jysc1,gpdm1,data1.getDate(0, "rq"),data1.getDate(dbts-1, "rq"));
		File file1 = new File ("M:\\hqself\\img\\main.jpg");
		ImageIO.write(sor,"jpg",file1);
		
		sql.setSql("select * from stock.stock_list where jysc=? and gpdm=? ");
		sql.setString(1, jysc1);
		sql.setString(2, gpdm1);
		DataStore lsds=sql.executeQuery();
		String tzc_1=lsds.getString(0, "tzc_1");
		JSONArray ja=JSONArray.fromObject(tzc_1);
		
		for(int i=0;i<ja.size();i++){
			JSONObject job = ja.getJSONObject(i);
			String jysc2=job.getString("jysc");
			String gpdm2=job.getString("gpdm");
			String qsrq=job.getString("qsrq");
			String zzrq=job.getString("zzrq");
			String xsd=job.getString("xsd");
			BufferedImage tar=getStockKImg(jysc2,gpdm2,DateUtil.stringToDate(qsrq),DateUtil.addDay(DateUtil.stringToDate(zzrq), 45));
			File file2 = new File ("M:\\hqself\\img\\"+jysc2+"_"+gpdm2+"_"+xsd+".jpg");
			ImageIO.write(tar,"jpg",file2);
		}
	}
	
	//获取所有股票的相似股票
	public static void getXsgp(int dbts) throws Exception{
		Sql sql=new Sql();
		sql.setSql("select * from stock.stock_list a " +
				"    where exists(select 1 " +
				"							    from stock.stock_day_infor x " +
				"							   where x.jysc=a.jysc and x.gpdm=a.gpdm) and jysc='h' limit 100 ");
		DataStore lsds=sql.executeQuery();
		DATADO=new DataStore();
		for(int i=0;i<lsds.rowCount();i++){
        	String gpdm=lsds.getString(i, "gpdm");
        	String jysc=lsds.getString(i, "jysc");
            sql.setSql("select wm_concat(round(spj,2)||'') zdf,wm_concat(to_char(rq,'yyyy-mm-dd')) rq " +  //涨跌幅保留小数点后5位
             		"				    from (select spj,rq " +
             		" 						    from stock.stock_day_infor x " +
             		"						   where x.gpdm=? and x.jysc=? order by rq) as t");
        	sql.setString(1, gpdm);
        	sql.setString(2, jysc);
            DataStore ds=sql.executeQuery();
            System.out.println(i+" "+jysc+" "+gpdm);
            String zdf[]=ds.getString(0, "zdf").replace("NaN", "0").split(",");
            String rq[]=ds.getString(0, "rq").split(",");
            DATADO.addRow();
            int r=DATADO.rowCount()-1;
            DATADO.put(r,"jysc",jysc);
            DATADO.put(r,"gpdm",gpdm);
            DATADO.put(r,"zdf",zdf);
            DATADO.put(r,"rq",rq);
        }
		
		for(int g=0;g<lsds.rowCount();g++){
			String jysc2=lsds.getString(g, "jysc");
			String gpdm2=lsds.getString(g, "gpdm");
			if(findThreadsCount()<20){
				GetXsgpThread thread = new GetXsgpThread(gpdm2,jysc2,dbts);
				Thread t=new Thread(thread);
				t.start();
			}else{
				while(findThreadsCount()>=20){//最多只运行100个线程
					Thread.sleep(1000);
				}
				GetXsgpThread thread = new GetXsgpThread(gpdm2,jysc2,dbts);
				Thread t=new Thread(thread);
				t.start();
			}
		}
	}
	
	public static void main(String str[]) throws Exception{
		getXsgpImg("601727","h",45);
		//getXsgp(45);
	}
	
    /**
     *  获取Java VM中当前运行的线程总数
     * @return
     */
    public static int findThreadsCount() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;
        // 遍历线程组树，获取根线程组
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        // 激活的线程数加倍
        int estimatedSize = topGroup.activeCount() * 2;
        Thread[] slacks = new Thread[estimatedSize];
         //获取根线程组的所有线程
        int actualSize = topGroup.enumerate(slacks);
        Thread[] threads = new Thread[actualSize];
        System.arraycopy(slacks, 0, threads, 0, actualSize);
        return threads.length;
    }
	
	//获取股票图片
	public static BufferedImage getStockKImg(String jysc,String gpdm,Date qssj,Date zzsj) throws Exception{
		int max1,max2,max3,min1,min2,min3;
		Sql sql=new Sql();
		sql.setSql("select rq,kpj*100 kpj,spj*100 spj,zgj*100 zgj,zdj*100 zdj " +
				"	  from stock.stock_day_infor where jysc=? and gpdm=? and rq between ? and ? order by rq");
		sql.setString(1, jysc);
		sql.setString(2, gpdm);
		sql.setDate(3, qssj);
		sql.setDate(4, zzsj);
		DataStore data1=sql.executeQuery();
		if(data1.rowCount()==0){
			return null;
		}
		sql.setSql("select max(kpj*100) max1,max(spj*100) max2,max(zgj*100) max3,min(kpj*100) min1,min(spj*100) min2,min(zdj*100) min3 " +
				" 	  from stock.stock_day_infor where jysc=? and gpdm=? and rq between ? and ? ");
		sql.setString(1, jysc);
		sql.setString(2, gpdm);
		sql.setDate(3, qssj);
		sql.setDate(4, zzsj);
		DataStore count1=sql.executeQuery();
		max1=(int)count1.getDouble(0, "max1");
		max2=(int)count1.getDouble(0, "max2");
		max3=(int)count1.getDouble(0, "max3");
		min1=(int)count1.getDouble(0, "min1");
		min2=(int)count1.getDouble(0, "min2");
		min3=(int)count1.getDouble(0, "min3");
		if(max1<max2){
			max1=max2;
		}
		if(max1<max3){
			max1=max3;
		}
		if(min1>min2){
			min1=min2;
		}
		if(min1>min3){
			min1=min3;
		}
		int height1=max1-min1+1;
		int width1=data1.rowCount()*5;
		
		BufferedImage sor=new BufferedImage(width1, height1+2, BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<data1.rowCount();i++){
			int kpj=(int)data1.getDouble(i, "kpj")-min1;
			int spj=(int)data1.getDouble(i, "spj")-min1;
			int zgj=(int)data1.getDouble(i, "zgj")-min1;
			int zdj=(int)data1.getDouble(i, "zdj")-min1;
			Date rq=data1.getDate(i, "rq");
			int rgb=0x00FF00;
			int hig=kpj;//每个K线的低点
			int low=spj; //每个K线的高点
			if(kpj<spj){
				hig=spj;
				low=kpj;
				rgb=0xFF0000;
			}
			
			for(int j=0;j<5;j++){
				if(j!=2){//每个K线宽5像素，中间那根是最高最低线
					for(int y=low;y<=hig;y++){
						sor.setRGB((i*5)+j, height1-y, rgb);//左上角是0，0,所以要height1-y
					}
				}else{
					for(int y=zdj;y<=zgj;y++){
						sor.setRGB((i*5)+j, height1-y, rgb);
					}
				}
			}
		}	
		return sor;
	}
	
	//计算趋势特征串
	public static void setTrendFeature() throws Exception{
		Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
        sql.setSql("select * from stock.stock_list");
        DataStore lsds=sql.executeQuery();
        for(int i=0;i<lsds.rowCount();i++){
        	String gpdm=lsds.getString(i, "gpdm");
        	String jysc=lsds.getString(i, "jysc");
        	sql.setSql(" update stock.stock_list a " +
        			"       set tzc_1=(select wm_concat(round(zdf,2)||'') zdf " +  //涨跌幅保留小数点后5位
             		"				    from (select zdf " +
             		" 						    from stock.stock_day_infor x " +
             		"						   where x.gpdm=? and x.jysc=? order by rq) as tem) " +
             		"	  where a.gpdm=? and a.jysc=? ");
        	sql.setString(1, gpdm);
        	sql.setString(2, jysc);
        	sql.setString(3, gpdm);
        	sql.setString(4, jysc);
            sql.executeUpdate();
            sql.setSql(" update stock.stock_list a " +
        			"       set tzc_7=(select wm_concat(round(zdf,2)||'') zdf " +  //涨跌幅保留小数点后5位
             		"				    from (select zdf " +
             		" 						    from stock.stock_week_infor x " +
             		"						   where x.gpdm=? and x.jysc=? order by rq) as tem) " +
             		"	  where a.gpdm=? and a.jysc=? ");
        	sql.setString(1, gpdm);
        	sql.setString(2, jysc);
        	sql.setString(3, gpdm);
        	sql.setString(4, jysc);
            sql.executeUpdate();
            System.out.println(i+" "+jysc+" "+gpdm);
        }
        tm.commitWithoutStart();
	}
	
	//初始化股票信息
	public static void initStockInfor() throws Exception{
		System.out.println("ks");
		InputStream input = new FileInputStream(new File("D:/股票列表s.txt"));
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
        	sql.setString(1, "s");
        	sql.setString(2, gpdm);
        	sql.setString(3, gpmc);
        	sql.executeUpdate();
        }
        sql.setSql("select * from stock.stock_list");
        DataStore ds=sql.executeQuery();
        System.out.println(1);
        tm.commitWithoutStart();
	}
	
	//通过日线算周线
	public static void getStockWeekHis() throws Exception{
		Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
		String jysc[][]={{"1","s"},{"0","h"}};
        for(int s=0;s<2;s++){
        	sql.setSql("select a.gpdm," +
        			"		   (select min(rq) from stock.stock_day_infor x where x.jysc=a.jysc and x.gpdm=a.gpdm) zqrq," +
        			"		   (select max(rq) from stock.stock_day_infor x where x.jysc=a.jysc and x.gpdm=a.gpdm) zhrq," +
        			"		   (select max(rq) from stock.stock_week_infor x where x.jysc=a.jysc and x.gpdm=a.gpdm) zhweek" +
        			"	  from stock.stock_list a " +
        			"    where jysc='"+jysc[s][1]+"' order by gpdm ");
        	DataStore gpds=sql.executeQuery();
        	int rowc=gpds.rowCount();
        	for(int k=0;k<rowc;k++){
        		String gpdm=gpds.getString(k, "gpdm");
        		Date zqrq=gpds.getDate(k, "zqrq");//最前面的日期
        		Date zhrq=gpds.getDate(k, "zhrq");//最后面的日期
        		Date zhweek=gpds.getDate(k, "zhweek");//最后的周线（这条线可能尚未汇总完成）
        		if(zqrq==null || (zhweek==null && DateUtil.addDay(zqrq, 7).after(new Date()))){
        			//没数据的不算，上市不足7天的不算
        			continue;
        		}
        		System.out.println(jysc[s][1]+" "+gpdm);
        		Date zhou0,zhou6;//每周的第一天（周日）和最后一天（周六）
        		Double qsp;//前收盘价
        		if(zhweek==null){//如果从没计算过，则先算出来第一个周的最后一天(周日算每周第一天)
        			zhou0=DateUtil.addDay(zqrq,0-DateUtil.getWeek(zqrq));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjje " +
        					" from dual ");
        			int index=1;
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			DataStore lsds=sql.executeQuery();
        			
        			zhweek=lsds.getDate(0, "zhweek");
        			double spj=lsds.getDouble(0, "spj");
        			double zgj=lsds.getDouble(0, "zgj");
        			double zdj=lsds.getDouble(0, "zdj");
        			double kpj=lsds.getDouble(0, "kpj");
        			double cjl=lsds.getDouble(0, "cjl");
        			double cjje=lsds.getDouble(0, "cjje");
        			if(kpj==0){
        				kpj=1;//如果开盘价是0，强制成1，否则算涨幅就除0了
        			}
        			qsp=kpj;
        			
        			sql.setSql("insert into stock.stock_week_infor( " +
        					"		   jysc, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz,ltsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_week_infor where jysc=? and gpdm=? and rq=?) ");
        			sql.setString(1, jysc[s][1]);
        			sql.setString(2, gpdm);
        			sql.setDate(3, zhweek);
        			sql.setDouble(4,spj);
        			sql.setDouble(5, zgj);
        			sql.setDouble(6, zdj);
        			sql.setDouble(7, kpj);
        			sql.setDouble(8, qsp);
        			sql.setDouble(9, spj-qsp);
        			sql.setDouble(10, (spj-qsp)*100.0/qsp);
        			sql.setDouble(11, cjl);
        			sql.setDouble(12,cjje);
        			sql.setDouble(13, 0);
        			sql.setDouble(14, 0);
        			sql.setDouble(15, 0);
        			sql.setString(16, jysc[s][1]);
        			sql.setString(17, gpdm);
        			sql.setDate(18, zhweek);
        			sql.executeUpdate();
        			//插完第一条后，准备后续的循环数据
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}else{//如果已经有一条了，则算出上周收盘价，然后把最后一周的删掉（后面的循环会把删掉的这周重算）
        			zhou0=DateUtil.addDay(zhweek,0-DateUtil.getWeek(zhweek));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select spj from stock.stock_day_infor " +
        					"	 where gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"      and rq=(select max(rq) from stock.stock_day_infor where rq<? and gpdm=? and jysc='"+jysc[s][1]+"'  )");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhou0);
        			sql.setString(3, gpdm);
        			DataStore lsds1=sql.executeQuery();
        			if(lsds1.rowCount()==0){
        				qsp=1.0;
        			}else{
        				qsp=lsds1.getDouble(0, "spj");
        			}
        			sql.setSql("delete from stock.stock_week_infor where gpdm=? and jysc='"+jysc[s][1]+"' and rq=? ");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhweek);
        			sql.executeUpdate();
        		}
        		
        		//上面算好了，下面就开始循环插周线
        		while(!zhou0.after(zhrq)){//只要所算周的第一天不晚于日线的最后一条数据，就进循环
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjje " +
        					" from dual ");
        			int index=1;
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			DataStore lsds=sql.executeQuery();
        			
        			zhweek=lsds.getDate(0, "zhweek");
        			if(zhweek==null){
        				zhou0=DateUtil.addDay(zhou0, 7);
            			zhou6=DateUtil.addDay(zhou0, 6);
            			continue;
        			}
        			
        			double spj=lsds.getDouble(0, "spj");
        			double zgj=lsds.getDouble(0, "zgj");
        			double zdj=lsds.getDouble(0, "zdj");
        			double kpj=lsds.getDouble(0, "kpj");
        			double cjl=lsds.getDouble(0, "cjl");
        			double cjje=lsds.getDouble(0, "cjje");
        			if(kpj==0){
        				kpj=1;//如果开盘价是0，强制成1，否则算涨幅就除0了
        			}
        			
        			sql.setSql("insert into stock.stock_week_infor( " +
        					"		   jysc, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz,ltsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_week_infor where jysc=? and gpdm=? and rq=?) ");
        			sql.setString(1, jysc[s][1]);
        			sql.setString(2, gpdm);
        			sql.setDate(3, zhweek);
        			sql.setDouble(4,spj);
        			sql.setDouble(5, zgj);
        			sql.setDouble(6, zdj);
        			sql.setDouble(7, kpj);
        			sql.setDouble(8, qsp);
        			sql.setDouble(9, spj-qsp);
        			sql.setDouble(10, (spj-qsp)*100.0/qsp);
        			sql.setDouble(11, cjl);
        			sql.setDouble(12,cjje);
        			sql.setDouble(13, 0);
        			sql.setDouble(14, 0);
        			sql.setDouble(15, 0);
        			sql.setString(16, jysc[s][1]);
        			sql.setString(17, gpdm);
        			sql.setDate(18, zhweek);
        			sql.executeUpdate();
        			tm.commit();
        			//准备下一个循环的数据
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}
        	}
        }
        System.out.println("end");
	}
	
	//通过日线算周线
	public static void getStockMonthHis() throws Exception{
		Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
		String jysc[][]={{"1","s"},{"0","h"}};
        for(int s=0;s<2;s++){
        	sql.setSql("select a.gpdm," +
        			"		   (select min(rq) from stock.stock_day_infor x where x.jysc=a.jysc and x.gpdm=a.gpdm) zqrq," +
        			"		   (select max(rq) from stock.stock_day_infor x where x.jysc=a.jysc and x.gpdm=a.gpdm) zhrq," +
        			"		   (select max(rq) from stock.stock_month_infor x where x.jysc=a.jysc and x.gpdm=a.gpdm) zhweek" +
        			"	  from stock.stock_list a " +
        			"    where jysc='"+jysc[s][1]+"' order by gpdm ");
        	DataStore gpds=sql.executeQuery();
        	int rowc=gpds.rowCount();
        	for(int k=0;k<rowc;k++){
        		String gpdm=gpds.getString(k, "gpdm");
        		Date zqrq=gpds.getDate(k, "zqrq");//最前面的日期
        		Date zhrq=gpds.getDate(k, "zhrq");//最后面的日期
        		Date zhweek=gpds.getDate(k, "zhweek");//最后的周线（这条线可能尚未汇总完成）
        		if(zqrq==null || (zhweek==null && DateUtil.addDay(zqrq, 7).after(new Date()))){
        			//没数据的不算，上市不足7天的不算
        			continue;
        		}
        		System.out.println(jysc[s][1]+" "+gpdm);
        		Date zhou0,zhou6;//每周的第一天（周日）和最后一天（周六）
        		Double qsp;//前收盘价
        		if(zhweek==null){//如果从没计算过，则先算出来第一个周的最后一天(周日算每周第一天)
        			zhou0=DateUtil.addDay(zqrq,0-DateUtil.getWeek(zqrq));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjje " +
        					" from dual ");
        			int index=1;
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			DataStore lsds=sql.executeQuery();
        			
        			zhweek=lsds.getDate(0, "zhweek");
        			double spj=lsds.getDouble(0, "spj");
        			double zgj=lsds.getDouble(0, "zgj");
        			double zdj=lsds.getDouble(0, "zdj");
        			double kpj=lsds.getDouble(0, "kpj");
        			double cjl=lsds.getDouble(0, "cjl");
        			double cjje=lsds.getDouble(0, "cjje");
        			if(kpj==0){
        				kpj=1;//如果开盘价是0，强制成1，否则算涨幅就除0了
        			}
        			qsp=kpj;
        			
        			sql.setSql("insert into stock.stock_month_infor( " +
        					"		   jysc, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz,ltsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_month_infor where jysc=? and gpdm=? and rq=?) ");
        			sql.setString(1, jysc[s][1]);
        			sql.setString(2, gpdm);
        			sql.setDate(3, zhweek);
        			sql.setDouble(4,spj);
        			sql.setDouble(5, zgj);
        			sql.setDouble(6, zdj);
        			sql.setDouble(7, kpj);
        			sql.setDouble(8, qsp);
        			sql.setDouble(9, spj-qsp);
        			sql.setDouble(10, (spj-qsp)*100.0/qsp);
        			sql.setDouble(11, cjl);
        			sql.setDouble(12,cjje);
        			sql.setDouble(13, 0);
        			sql.setDouble(14, 0);
        			sql.setDouble(15, 0);
        			sql.setString(16, jysc[s][1]);
        			sql.setString(17, gpdm);
        			sql.setDate(18, zhweek);
        			sql.executeUpdate();
        			//插完第一条后，准备后续的循环数据
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}else{//如果已经有一条了，则算出上周收盘价，然后把最后一周的删掉（后面的循环会把删掉的这周重算）
        			zhou0=DateUtil.addDay(zhweek,0-DateUtil.getWeek(zhweek));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select spj from stock.stock_day_infor " +
        					"	 where gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"      and rq=(select max(rq) from stock.stock_day_infor where rq<? and gpdm=? and jysc='"+jysc[s][1]+"'  )");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhou0);
        			sql.setString(3, gpdm);
        			DataStore lsds1=sql.executeQuery();
        			if(lsds1.rowCount()==0){
        				qsp=1.0;
        			}else{
        				qsp=lsds1.getDouble(0, "spj");
        			}
        			sql.setSql("delete from stock.stock_month_infor where gpdm=? and jysc='"+jysc[s][1]+"' and rq=? ");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhweek);
        			sql.executeUpdate();
        		}
        		
        		//上面算好了，下面就开始循环插周线
        		while(!zhou0.after(zhrq)){//只要所算周的第一天不晚于日线的最后一条数据，就进循环
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"' " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"')) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? and jysc='"+jysc[s][1]+"') cjje " +
        					" from dual ");
        			int index=1;
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			sql.setDate(index++, zhou0);
        			sql.setDate(index++, zhou6);
        			sql.setString(index++, gpdm);
        			DataStore lsds=sql.executeQuery();
        			
        			zhweek=lsds.getDate(0, "zhweek");
        			if(zhweek==null){
        				zhou0=DateUtil.addDay(zhou0, 7);
            			zhou6=DateUtil.addDay(zhou0, 6);
            			continue;
        			}
        			
        			double spj=lsds.getDouble(0, "spj");
        			double zgj=lsds.getDouble(0, "zgj");
        			double zdj=lsds.getDouble(0, "zdj");
        			double kpj=lsds.getDouble(0, "kpj");
        			double cjl=lsds.getDouble(0, "cjl");
        			double cjje=lsds.getDouble(0, "cjje");
        			if(kpj==0){
        				kpj=1;//如果开盘价是0，强制成1，否则算涨幅就除0了
        			}
        			
        			sql.setSql("insert into stock.stock_month_infor( " +
        					"		   jysc, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz,ltsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_month_infor where jysc=? and gpdm=? and rq=?) ");
        			sql.setString(1, jysc[s][1]);
        			sql.setString(2, gpdm);
        			sql.setDate(3, zhweek);
        			sql.setDouble(4,spj);
        			sql.setDouble(5, zgj);
        			sql.setDouble(6, zdj);
        			sql.setDouble(7, kpj);
        			sql.setDouble(8, qsp);
        			sql.setDouble(9, spj-qsp);
        			sql.setDouble(10, (spj-qsp)*100.0/qsp);
        			sql.setDouble(11, cjl);
        			sql.setDouble(12,cjje);
        			sql.setDouble(13, 0);
        			sql.setDouble(14, 0);
        			sql.setDouble(15, 0);
        			sql.setString(16, jysc[s][1]);
        			sql.setString(17, gpdm);
        			sql.setDate(18, zhweek);
        			sql.executeUpdate();
        			tm.commit();
        			//准备下一个循环的数据
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}
        	}
        }
        System.out.println("end");
	}
	
	//获取股票历史
	public static void getStockDayHis() throws Exception{
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
        	int rowc=gpds.rowCount();
        	for(int k=0;k<rowc;k++){
        		String gpdm=gpds.getString(k, "gpdm");
        		Date rq=gpds.getDate(k, "zhrq");
        		if(rq==null){
        			rq=DateUtil.stringToDate("19901219", "yyyyMMdd");
        		}else{
        			rq=DateUtil.addDay(rq, 1);
        		}
        		String rqstr=DateUtil.dateToString(rq,"yyyyMMdd");
        		if(rqstr.compareTo(nowstr)>=0){
        			continue;
        		}
        		String url="http://quotes.money.163.com/service/chddata.html?code="+jysc[s][0]+gpdm+"&start="+rqstr+"&end="+nowstr+"&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
        		System.out.println(url);
        		String restr= HttpRequestUtil.httpRequest_string(url, null, "get", "");
        		restr=new String(restr.getBytes("ISO8859-1"), "GBK");
        		System.out.println(restr);
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
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_day_infor where jysc=? and gpdm=? and rq=?) ");
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
        			sql.setString(16, jysc[s][1]);
        			sql.setString(17, gpdm);
        			sql.setDate(18, DateUtil.stringToDate(one[0], "yyyy-MM-dd"));
        			sql.executeUpdate();
        		}
        		tm.commit();
        		System.out.println(jysc[s][1]+" "+gpdm+" 完成"+k+"/"+rowc);
        	}
        }
        System.out.println("js");
	}
	
	/**
	 * 查某个股票的k线
	 * @author hq
	 */
	public DataObject showOneStockK(DataObject para) throws Exception {
		DataObject vdo=new DataObject();
		String re="";
		String gpdm=para.getString("gpdm");
		String jysc=para.getString("jysc");
		sql.setSql("select wm_concat(to_char(rq,'yyyy/mm/dd')||'#'||kpj||'#'||spj||'#'||zde||'#'||zdf||'%#'||zdj||'#'||zgj) as re " +
				"     from " +
				"  (select * from stock.stock_day_infor " +
				"	 where gpdm=? and jysc=? and kpj<>0 and rq between ? and ? " +
				"	 order by rq ) as m ");
		sql.setString(1, gpdm);
		sql.setString(2, jysc);
		sql.setDate(3, DateUtil.addDay(DateUtil.getDBDate(), -2*365));
		sql.setDate(4, DateUtil.getDBDate());
		DataStore vds=sql.executeQuery();
		if(vds.rowCount()>0){
			re=vds.getString(0, "re");
		}
		if(re==null || "".equals(re)){
			this.BusinessException("啥都没查到啊");
		}
		vdo.put("re", re);
		return vdo;
	}
}