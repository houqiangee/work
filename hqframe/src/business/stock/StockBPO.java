package business.stock;

import java.awt.Image;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;


import com.framework.layer.BPO;
import com.framework.pic.algorithm.sift.ImageTransform;
import com.framework.pic.algorithm.sift.MyPoint;
import com.framework.pic.utility.Image_Utility;
import com.framework.sift.SIFT;
import com.framework.util.DataObject;
import com.framework.util.DataStore;
import com.framework.util.DateUtil;
import com.framework.util.HttpRequestUtil;
import com.framework.util.Sql;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;


public class StockBPO extends BPO{
	
	//�Աȹ�ƱƬ����ĳֻ��Ʊ
	public static void main(String[] str) throws Exception{
		double sor[][];
		double tar[][];
		
		
		List<MyPoint> v1 = ImageTransform.getCharacterVectors(sor);
		List<MyPoint> v2 = ImageTransform.getCharacterVectors(tar);
		int num = ImageTransform.getSimilarPointsNum(v1, v2);
		System.out.println("���������ֱ�Ϊ��" + v1.size() + "&" + v2.size() + "  ���Ƶ���Ϊ��" + num);
	}
	
	//��������������
	public static void setTrendFeature() throws Exception{
		Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
        //����
        sql.setSql("update stock.stock_list a " +
        		"      set tzc_7=(select wm_concat(round(zdf,1)||'') zdf " +
        		"				    from (select zdf " +
        		" 						    from stock.stock_week_infor x " +
        		"						   where x.gpdm=a.gpdm and x.jysc=a.jysc order by rq) as tem)");
        sql.executeUpdate();
        tm.commitWithoutStart();
	}
	
	//��ʼ����Ʊ��Ϣ
	public static void initStockInfor() throws Exception{
		System.out.println("ks");
		InputStream input = new FileInputStream(new File("D:/��Ʊ�б�s.txt"));
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream(); 
		byte[] buff = new byte[100]; //buff���ڴ��ѭ����ȡ����ʱ���� 
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
	
	//ͨ������������
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
        		Date zqrq=gpds.getDate(k, "zqrq");//��ǰ�������
        		Date zhrq=gpds.getDate(k, "zhrq");//����������
        		Date zhweek=gpds.getDate(k, "zhweek");//�������ߣ������߿�����δ������ɣ�
        		if(zqrq==null || (zhweek==null && DateUtil.addDay(zqrq, 7).after(new Date()))){
        			//û���ݵĲ��㣬���в���7��Ĳ���
        			continue;
        		}
        		System.out.println(jysc[s][1]+" "+gpdm);
        		Date zhou0,zhou6;//ÿ�ܵĵ�һ�죨���գ������һ�죨������
        		Double qsp;//ǰ���̼�
        		if(zhweek==null){//�����û������������������һ���ܵ����һ��(������ÿ�ܵ�һ��)
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
        				kpj=1;//������̼���0��ǿ�Ƴ�1���������Ƿ��ͳ�0��
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
        			//�����һ����׼��������ѭ������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}else{//����Ѿ���һ���ˣ�������������̼ۣ�Ȼ������һ�ܵ�ɾ���������ѭ�����ɾ�����������㣩
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
        		
        		//��������ˣ�����Ϳ�ʼѭ��������
        		while(!zhou0.after(zhrq)){//ֻҪ�����ܵĵ�һ�첻�������ߵ����һ�����ݣ��ͽ�ѭ��
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
        				kpj=1;//������̼���0��ǿ�Ƴ�1���������Ƿ��ͳ�0��
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
        			//׼����һ��ѭ��������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}
        	}
        }
        System.out.println("end");
	}
	
	//ͨ������������
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
        		Date zqrq=gpds.getDate(k, "zqrq");//��ǰ�������
        		Date zhrq=gpds.getDate(k, "zhrq");//����������
        		Date zhweek=gpds.getDate(k, "zhweek");//�������ߣ������߿�����δ������ɣ�
        		if(zqrq==null || (zhweek==null && DateUtil.addDay(zqrq, 7).after(new Date()))){
        			//û���ݵĲ��㣬���в���7��Ĳ���
        			continue;
        		}
        		System.out.println(jysc[s][1]+" "+gpdm);
        		Date zhou0,zhou6;//ÿ�ܵĵ�һ�죨���գ������һ�죨������
        		Double qsp;//ǰ���̼�
        		if(zhweek==null){//�����û������������������һ���ܵ����һ��(������ÿ�ܵ�һ��)
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
        				kpj=1;//������̼���0��ǿ�Ƴ�1���������Ƿ��ͳ�0��
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
        			//�����һ����׼��������ѭ������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}else{//����Ѿ���һ���ˣ�������������̼ۣ�Ȼ������һ�ܵ�ɾ���������ѭ�����ɾ�����������㣩
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
        		
        		//��������ˣ�����Ϳ�ʼѭ��������
        		while(!zhou0.after(zhrq)){//ֻҪ�����ܵĵ�һ�첻�������ߵ����һ�����ݣ��ͽ�ѭ��
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
        				kpj=1;//������̼���0��ǿ�Ƴ�1���������Ƿ��ͳ�0��
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
        			//׼����һ��ѭ��������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}
        	}
        }
        System.out.println("end");
	}
	
	//��ȡ��Ʊ��ʷ
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
        			String one[]=data[i].replace("None", "0").split(",");//0����,1��Ʊ����,2����,3���̼�,4��߼�,5��ͼ�,6���̼�,7ǰ����,8�ǵ���,9�ǵ���,10������,11�ɽ���,12�ɽ����,13����ֵ,14��ͨ��ֵ
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
        		System.out.println(jysc[s][1]+" "+gpdm+" ���"+k+"/"+rowc);
        	}
        }
        System.out.println("js");
	}
	
	/**
	 * ��ĳ����Ʊ��k��
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
				"	 where gpdm=? and jysc=? and kpj<>0 and rq>? " +
				"	 order by rq ) as m ");
		sql.setString(1, gpdm);
		sql.setString(2, jysc);
		sql.setDate(3, DateUtil.addDay(DateUtil.getDBDate(), -2*365));
		DataStore vds=sql.executeQuery();
		if(vds.rowCount()>0){
			re=vds.getString(0, "re");
		}
		if(re==null || "".equals(re)){
			this.BusinessException("ɶ��û�鵽��");
		}
		vdo.put("re", re);
		return vdo;
	}
}