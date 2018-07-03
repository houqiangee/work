package business.stock;
import java.util.Iterator;
import java.util.Map;

import com.framework.util.DataObject;
import com.framework.util.DataStore;
import com.framework.util.DateUtil;
import com.framework.util.Sql;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;


//计算相似股票线程
public class GetXsgpThread implements Runnable{
	String gpdm1="";
	String jysc1="";
	int dbts=0;
	public GetXsgpThread(String gpdm1,String jysc1,int dbts){
		this.gpdm1=gpdm1;
		this.jysc1=jysc1;
		this.dbts=dbts;
	}
	public void run() {
		dobus();
	}
	
	public void dobus(){
		System.out.println(this.jysc1+":"+this.gpdm1);
		try {
			if(dbts<30){
				return;
			}
			DataStore xsgpds=new DataStore();
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
			double tzx=dbts;//特征界限，当特征差大于特征线时说明差距过大
			
			for(int u=0;u<StockBPO.DATADO.rowCount();u++){
				String jysc2=StockBPO.DATADO.getString(u, "jysc");
				String gpdm2=StockBPO.DATADO.getString(u, "gpdm");
				if(gpdm2.equals(gpdm1) && jysc2.equals(jysc1)){
					continue;
				}
				
				String data2[]= (String[]) StockBPO.DATADO.getObject(u, "zdf");
				String rq[]= (String[]) StockBPO.DATADO.getObject(u, "rq");
				if(data2.length<dbts){
					continue;
				}
				double csz1=100.0/data1.getDouble(0, "spj");
				for(int i=0;i<data2.length-dbts;i++){
					double tzc=0.0;//特征差
					double csz2=100.0/Double.parseDouble(data2[i]);
					for(int j=0;j<dbts;j++){
						Double zdf1=data1.getDouble(j, "spj");
						Double zdf2=Double.parseDouble(data2[i+j]);
						if(zdf2<0.1){
							tzc=1000;
						}
						Double cz=csz1*zdf1-csz2*zdf2;
						if(cz.isNaN()){
							tzc=1000;
						}
						cz=cz>0?cz:(0-cz);
						tzc+=cz;
						if(tzc>=tzx){
							break;
						}
					}
					if(tzc<tzx){
						xsgpds.addRow();
						int r=xsgpds.rowCount()-1;
						xsgpds.put(r, "jysc", jysc2);
						xsgpds.put(r, "gpdm", gpdm2);
						xsgpds.put(r, "qsrq", rq[i]);
						xsgpds.put(r, "zzrq", rq[i+dbts]);
						xsgpds.put(r, "xsd", tzc);
						i+=(dbts-1);
					}
				}
			}
			xsgpds.sort("xsd");
			while(xsgpds.rowCount()>10){//最多只留10行
				xsgpds.remove(xsgpds.rowCount()-1);
			}
			Transaction tm=TransactionManager.getTransaction();
	        tm.begin();
	        sql.setSql(" update stock.stock_list a " +
        			"       set tzc_1=?,xsfxrq=(select max(rq) as rq from stock.stock_day_infor x where x.gpdm=a.gpdm and x.jysc=a.jysc) " +
             		"	  where a.gpdm=? and a.jysc=? ");
	        sql.setString(1, xsgpds.toJSON());
        	sql.setString(2, gpdm1);
        	sql.setString(3, jysc1);
        	sql.executeUpdate();
        	tm.commitWithoutStart();
			System.out.println("=="+jysc1+"："+gpdm1+"=="+xsgpds.toJSON());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}