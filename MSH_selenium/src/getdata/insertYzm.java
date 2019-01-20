package getdata;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import util.MSHsUtil;

import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.Sql;
import com.dareway.framework.util.database.Transaction;
import com.dareway.framework.util.database.TransactionManager;

public class insertYzm {
	
	public static void main(String str[]){
		GlobalNames.CONFIGINWAR="true";
		
		String spid2[]={"576894617947","577258409416","576901721124","577650297359"};
		for(int i=0;i<spid2.length;i++){
			GetYzmThread gt=new GetYzmThread();
			gt.setSpid(spid2[i]).start();
		}
	}
	
	
	public static class GetYzmThread extends Thread  {
		private String spid="";
		
		public GetYzmThread setSpid(String spid){
			this.spid=spid;
			return this;
		}
		
		public void run() {
			try{
				WebDriver driver = new ChromeDriver();
				//超时时间设置
				driver.manage().timeouts().implicitlyWait(30,TimeUnit.SECONDS);//识别元素时的超时时间
				driver.manage().timeouts().pageLoadTimeout(30,TimeUnit.SECONDS);//页面加载时的超时时间
				driver.manage().timeouts().setScriptTimeout(30,TimeUnit.SECONDS);//异步脚本的超时时间
				//设置窗口大小
				driver.manage().window().maximize();
				//打开URL
		        driver.get("https://login.taobao.com/member/login.jhtml?f=top&amp;redirectURL=https%3A%2F%2Fwww.taobao.com%2F");
		        MSHsUtil.waitElementDisappear(driver, By.id("J_Quick2Static"), 1000);
		        
		        driver.get("http://miao.item.taobao.com/"+spid+".htm?spm=5070.7116829.1996665325.15.XfRS4F");
		        Thread.sleep(2000);
		        Transaction tm=TransactionManager.getTransaction();tm.begin();
				Sql sql=new Sql();
				driver.manage().timeouts().implicitlyWait(3,TimeUnit.SECONDS);//接下来就是无限访问了，但可能505，所以设置超时3秒，505后3秒后继续刷。
				while(true){
					boolean end=false;
					DataObject vdo=new DataObject();
					sql.setSql("insert into stock.taobao_yzm(imgurl) select ? as imgurl from dual where not exists(select 1 from stock.taobao_yzm x where x.imgurl=?) ");
					for(int i=0;i<200;i++){
						driver.get("http://m.ajax.taobao.com/qst.htm?_ksTS=1536840041941_761&cb=jsonp762&id="+spid);
						String re;
						try{
							re=driver.findElement(By.tagName("pre")).getText();
						}catch(Exception e){
							continue;
						}
			        	if(re==null || "".equals(re)){
			        		end=true;
			        		break;
			        	}
			        	re=re.replace("jsonp762(", "").replace(")", "");
			        	JSONObject jo=JSONObject.fromObject(re);
			        	String imgurl=jo.getString("qst");
			        	if(vdo.containsKey(imgurl)){
			        		continue;
			        	}else{
			        		vdo.put(imgurl, "1");
			        	}
			        	sql.setString(1, imgurl);
			        	sql.setString(2, imgurl);
			        	sql.addBatch();
			        }
					sql.executeBatch();
					tm.commit();
					if(end){
						break;
					}
				}
				driver.quit();
			}catch(Exception e){
			}
		};
		
	}
}
