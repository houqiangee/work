package business.stock;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.framework.layer.BPO;
import com.framework.util.DataObject;
import com.framework.util.DataStore;
import com.framework.util.DateUtil;
import com.framework.util.HttpRequestUtil;
import com.framework.util.Sql;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;


public class StockBPO extends BPO{
	public static DataStore DATADO=new DataStore();
	
	//�Աȹ�ƱƬ����ĳֻ��Ʊ                            
	public static void getXsgpImg(String gpdm1,int dbts) throws Exception{
		if(dbts<30){
			return;
		}
		Sql sql=new Sql();
		sql.setSql("select * from stock.stock_day_infor where gpdm=? order by rq desc limit ? ");
		sql.setString(1, gpdm1);
		sql.setInt(2, dbts);
		DataStore data1=sql.executeQuery();
		if(data1.rowCount()<30){
			return;
		}
		data1.sort("rq");
		dbts=data1.rowCount();
		
		String path="D:\\work\\hqself\\img";
		
		deleteDir(path);
		
		BufferedImage sor=getStockKImg(gpdm1,data1.getDate(0, "rq"),data1.getDate(dbts-1, "rq"));
		File file1 = new File (path+"\\main.jpg");
		ImageIO.write(sor,"jpg",file1);
		
		sql.setSql("select * from stock.stock_list where gpdm=? ");
		sql.setString(1, gpdm1);
		DataStore lsds=sql.executeQuery();
		String tzc_1=lsds.getString(0, "tzc_1");
		JSONArray ja=JSONArray.fromObject(tzc_1);
		
		for(int i=0;i<ja.size();i++){
			JSONObject job = ja.getJSONObject(i);
			String gpdm2=job.getString("gpdm");
			String qsrq=job.getString("qsrq");
			String zzrq=job.getString("zzrq");
			String xsd=job.getString("xsd");
			BufferedImage tar=getStockKImg(gpdm2,DateUtil.stringToDate(qsrq),DateUtil.stringToDate(zzrq));
			File file2 = new File (path+"\\"+gpdm2+"_"+xsd+".jpg");
			ImageIO.write(tar,"jpg",file2);
		}
	}
	
	//��ȡ���й�Ʊ�����ƹ�Ʊ
	public static void getXsgp(int dbts) throws Exception{
		Sql sql=new Sql();
		sql.setSql("select * from stock.stock_list a " +
				"    where exists(select 1 " +
				"							    from stock.stock_day_infor x " +
				"							   where x.gpdm=a.gpdm) limit 300 ");
		DataStore lsds=sql.executeQuery();
		DATADO=new DataStore();
		for(int i=0;i<lsds.rowCount();i++){
        	String gpdm=lsds.getString(i, "gpdm");
            sql.setSql("select wm_concat(round(spj,2)||'') zdf,wm_concat(to_char(rq,'yyyy-mm-dd')) rq " +  //�ǵ�������С�����5λ
             		"				    from (select spj,rq " +
             		" 						    from stock.stock_day_infor x " +
             		"						   where x.gpdm=? order by rq) as t");
        	sql.setString(1, gpdm);
            DataStore ds=sql.executeQuery();
            System.out.println(i+" "+gpdm);
            String zdf[]=ds.getString(0, "zdf").replace("NaN", "0").split(",");
            String rq[]=ds.getString(0, "rq").split(",");
            DATADO.addRow();
            int r=DATADO.rowCount()-1;
            DATADO.put(r,"gpdm",gpdm);
            DATADO.put(r,"zdf",zdf);
            DATADO.put(r,"rq",rq);
        }
		
		for(int g=0;g<lsds.rowCount();g++){
			String gpdm2=lsds.getString(g, "gpdm");
			if(findThreadsCount()<20){
				GetXsgpThread thread = new GetXsgpThread(gpdm2,dbts);
				Thread t=new Thread(thread);
				t.start();
			}else{
				while(findThreadsCount()>=20){//���ֻ����100���߳�
					Thread.sleep(1000);
				}
				GetXsgpThread thread = new GetXsgpThread(gpdm2,dbts);
				Thread t=new Thread(thread);
				t.start();
			}
		}
	}
	
	public static void main(String str[]) throws Exception{
		getXsgpImg("600415",45);
		//getXsgp(45);
	}
	
	
	public static boolean deleteDir(String path){
		File file = new File(path);
		if(!file.exists()){//�ж��Ƿ��ɾ��Ŀ¼�Ƿ����
			System.err.println("The dir are not exists!");
			return false;
		}
		
		String[] content = file.list();//ȡ�õ�ǰĿ¼�������ļ����ļ���
		for(String name : content){
			File temp = new File(path, name);
			if(temp.isDirectory()){//�ж��Ƿ���Ŀ¼
				deleteDir(temp.getAbsolutePath());//�ݹ���ã�ɾ��Ŀ¼�������
				temp.delete();//ɾ����Ŀ¼
			}else{
				if(!temp.delete()){//ֱ��ɾ���ļ�
					System.err.println("Failed to delete " + name);
				}
			}
		}
		return true;
	}

	
    /**
     *  ��ȡJava VM�е�ǰ���е��߳�����
     * @return
     */
    public static int findThreadsCount() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;
        // �����߳���������ȡ���߳���
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        // ������߳����ӱ�
        int estimatedSize = topGroup.activeCount() * 2;
        Thread[] slacks = new Thread[estimatedSize];
         //��ȡ���߳���������߳�
        int actualSize = topGroup.enumerate(slacks);
        Thread[] threads = new Thread[actualSize];
        System.arraycopy(slacks, 0, threads, 0, actualSize);
        return threads.length;
    }
	
	//��ȡ��ƱͼƬ
	public static BufferedImage getStockKImg(String gpdm,Date qssj,Date zzsj) throws Exception{
		int max1,max2,max3,min1,min2,min3;
		Sql sql=new Sql();
		sql.setSql("select rq,kpj*100 kpj,spj*100 spj,zgj*100 zgj,zdj*100 zdj " +
				"	  from stock.stock_day_infor where gpdm=? and rq between ? and ? order by rq");
		sql.setString(1, gpdm);
		sql.setDate(2, qssj);
		sql.setDate(3, zzsj);
		DataStore data1=sql.executeQuery();
		if(data1.rowCount()==0){
			return null;
		}
		sql.setSql("select max(kpj*100) max1,max(spj*100) max2,max(zgj*100) max3,min(kpj*100) min1,min(spj*100) min2,min(zdj*100) min3 " +
				" 	  from stock.stock_day_infor where gpdm=? and rq between ? and ? ");
		sql.setString(1, gpdm);
		sql.setDate(2, qssj);
		sql.setDate(3, zzsj);
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
			int hig=kpj;//ÿ��K�ߵĵ͵�
			int low=spj; //ÿ��K�ߵĸߵ�
			if(kpj<spj){
				hig=spj;
				low=kpj;
				rgb=0xFF0000;
			}
			
			for(int j=0;j<5;j++){
				if(j!=2){//ÿ��K�߿�5���أ��м��Ǹ�����������
					for(int y=low;y<=hig;y++){
						sor.setRGB((i*5)+j, height1-y, rgb);//���Ͻ���0��0,����Ҫheight1-y
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
	
	//��������������
	public static void setTrendFeature() throws Exception{
		Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
        sql.setSql("select * from stock.stock_list");
        DataStore lsds=sql.executeQuery();
        for(int i=0;i<lsds.rowCount();i++){
        	String gpdm=lsds.getString(i, "gpdm");
        	sql.setSql(" update stock.stock_list a " +
        			"       set tzc_1=(select wm_concat(round(zdf,2)||'') zdf " +  //�ǵ�������С�����5λ
             		"				    from (select zdf " +
             		" 						    from stock.stock_day_infor x " +
             		"						   where x.gpdm=? order by rq) as tem) " +
             		"	  where a.gpdm=? ");
        	sql.setString(1, gpdm);
        	sql.setString(2, gpdm);
            sql.executeUpdate();
            sql.setSql(" update stock.stock_list a " +
        			"       set tzc_7=(select wm_concat(round(zdf,2)||'') zdf " +  //�ǵ�������С�����5λ
             		"				    from (select zdf " +
             		" 						    from stock.stock_week_infor x " +
             		"						   where x.gpdm=? order by rq) as tem) " +
             		"	  where a.gpdm=? ");
        	sql.setString(1, gpdm);
        	sql.setString(2, gpdm);
            sql.executeUpdate();
            System.out.println(i+" "+gpdm);
        }
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
        	sql.setSql("select a.gpdm," +
        			"		   (select min(rq) from stock.stock_day_infor x where x.gpdm=a.gpdm) zqrq," +
        			"		   (select max(rq) from stock.stock_day_infor x where x.gpdm=a.gpdm) zhrq," +
        			"		   (select max(rq) from stock.stock_week_infor x where x.gpdm=a.gpdm) zhweek" +
        			"	  from stock.stock_list a " +
        			"    order by gpdm ");
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
        		System.out.println(gpdm);
        		Date zhou0,zhou6;//ÿ�ܵĵ�һ�죨���գ������һ�죨������
        		Double qsp;//ǰ���̼�
        		if(zhweek==null){//�����û������������������һ���ܵ����һ��(������ÿ�ܵ�һ��)
        			zhou0=DateUtil.addDay(zqrq,0-DateUtil.getWeek(zqrq));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=?)) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=? " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? )) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjje " +
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
        					"		   ltsz, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_week_infor where gpdm=? and rq=?) ");
        			sql.setDouble(1, 0);
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
        			sql.setString(15, gpdm);
        			sql.setDate(16, zhweek);
        			sql.executeUpdate();
        			//�����һ����׼��������ѭ������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}else{//����Ѿ���һ���ˣ�������������̼ۣ�Ȼ������һ�ܵ�ɾ���������ѭ�����ɾ�����������㣩
        			zhou0=DateUtil.addDay(zhweek,0-DateUtil.getWeek(zhweek));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select spj from stock.stock_day_infor " +
        					"	 where gpdm=? " +
        					"      and rq=(select max(rq) from stock.stock_day_infor where rq<? and gpdm=?  )");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhou0);
        			sql.setString(3, gpdm);
        			DataStore lsds1=sql.executeQuery();
        			if(lsds1.rowCount()==0){
        				qsp=1.0;
        			}else{
        				qsp=lsds1.getDouble(0, "spj");
        			}
        			sql.setSql("delete from stock.stock_week_infor where gpdm=? and rq=? ");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhweek);
        			sql.executeUpdate();
        		}
        		
        		//��������ˣ�����Ϳ�ʼѭ��������
        		while(!zhou0.after(zhrq)){//ֻҪ�����ܵĵ�һ�첻�������ߵ����һ�����ݣ��ͽ�ѭ��
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=?  " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? )) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=?  " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? )) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjje " +
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
        					"		   ltsz, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_week_infor where gpdm=? and rq=?) ");
        			sql.setDouble(1, 0);
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
        			sql.setString(15, gpdm);
        			sql.setDate(16, zhweek);
        			sql.executeUpdate();
        			tm.commit();
        			//׼����һ��ѭ��������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}
        	}
        System.out.println("end");
	}
	
	//ͨ������������
	public static void getStockMonthHis() throws Exception{
		Transaction tm=TransactionManager.getTransaction();
        tm.begin();
        Sql sql=new Sql();
        	sql.setSql("select a.gpdm," +
        			"		   (select min(rq) from stock.stock_day_infor x where x.gpdm=a.gpdm) zqrq," +
        			"		   (select max(rq) from stock.stock_day_infor x where x.gpdm=a.gpdm) zhrq," +
        			"		   (select max(rq) from stock.stock_month_infor x where x.gpdm=a.gpdm) zhweek" +
        			"	  from stock.stock_list a " +
        			"    order by gpdm ");
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
        		System.out.println(gpdm);
        		Date zhou0,zhou6;//ÿ�ܵĵ�һ�죨���գ������һ�죨������
        		Double qsp;//ǰ���̼�
        		if(zhweek==null){//�����û������������������һ���ܵ����һ��(������ÿ�ܵ�һ��)
        			zhou0=DateUtil.addDay(zqrq,0-DateUtil.getWeek(zqrq));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=?  " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? )) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=?  " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? )) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjje " +
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
        					"		   ltsz, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_month_infor where gpdm=? and rq=?) ");
        			sql.setDouble(1, 0);
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
        			sql.setString(15, gpdm);
        			sql.setDate(16, zhweek);
        			sql.executeUpdate();
        			//�����һ����׼��������ѭ������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
        		}else{//����Ѿ���һ���ˣ�������������̼ۣ�Ȼ������һ�ܵ�ɾ���������ѭ�����ɾ�����������㣩
        			zhou0=DateUtil.addDay(zhweek,0-DateUtil.getWeek(zhweek));
        			zhou6=DateUtil.addDay(zhou0, 6);
        			sql.setSql("select spj from stock.stock_day_infor " +
        					"	 where gpdm=?  " +
        					"      and rq=(select max(rq) from stock.stock_day_infor where rq<? and gpdm=?   )");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhou0);
        			sql.setString(3, gpdm);
        			DataStore lsds1=sql.executeQuery();
        			if(lsds1.rowCount()==0){
        				qsp=1.0;
        			}else{
        				qsp=lsds1.getDouble(0, "spj");
        			}
        			sql.setSql("delete from stock.stock_month_infor where gpdm=?  and rq=? ");
        			sql.setString(1, gpdm);
        			sql.setDate(2, zhweek);
        			sql.executeUpdate();
        		}
        		
        		//��������ˣ�����Ϳ�ʼѭ��������
        		while(!zhou0.after(zhrq)){//ֻҪ�����ܵĵ�һ�첻�������ߵ����һ�����ݣ��ͽ�ѭ��
        			sql.setSql("select " +
        					"  (select max(rq) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zhweek," +
        					"  (select max(zgj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zgj," +
        					"  (select min(zdj) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) zdj," +
        					"  (select kpj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=?  " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? )) kpj," +
        					"  (select spj " +
        					"	  from stock.stock_day_infor where rq>=? and rq<=? and gpdm=?  " +
        					"	   and rq=(select min(rq) " +
        					"    			 from stock.stock_day_infor " +
        					"   		    where rq>=? and rq<=? and gpdm=? )) spj," +
        					"  (select sum(cjl) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjl," +
        					"  (select sum(cjje) " +
        					"     from stock.stock_day_infor " +
        					"    where rq>=? and rq<=? and gpdm=? ) cjje " +
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
        					"		   ltsz, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_month_infor where gpdm=? and rq=?) ");
        			sql.setDouble(1, 0);
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
        			sql.setString(15, gpdm);
        			sql.setDate(16, zhweek);
        			sql.executeUpdate();
        			tm.commit();
        			//׼����һ��ѭ��������
        			qsp=spj;
        			zhou0=DateUtil.addDay(zhou0, 7);
        			zhou6=DateUtil.addDay(zhou0, 6);
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
        			"		   (select max(rq) from stock.stock_day_infor x where x.gpdm=a.gpdm) zhrq" +
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
        					"		   ltsz, gpdm, rq, spj, zgj," +
        					"		   zdj, kpj, qsp, zde, zdf," +
        					"		   cjl, cjje,hsl,zsz) " +
        					"   select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? from dual " +
        					"    where not exists(select 1 from stock.stock_day_infor where gpdm=? and rq=?) ");
        			sql.setDouble(1, Double.parseDouble(one[14]));
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
        			sql.setString(15, gpdm);
        			sql.setDate(16, DateUtil.stringToDate(one[0], "yyyy-MM-dd"));
        			sql.executeUpdate();
        		}
        		tm.commit();
        		System.out.println(gpdm+" ���"+k+"/"+rowc);
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
		sql.setSql("select wm_concat(to_char(rq,'yyyy/mm/dd')||'#'||kpj||'#'||spj||'#'||zde||'#'||zdf||'%#'||zdj||'#'||zgj) as re " +
				"     from " +
				"  (select * from stock.stock_day_infor " +
				"	 where gpdm=? and kpj<>0 and rq between ? and ? " +
				"	 order by rq ) as m ");
		sql.setString(1, gpdm);
		sql.setDate(3, DateUtil.addDay(DateUtil.getDBDate(), -2*365));
		sql.setDate(4, DateUtil.getDBDate());
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