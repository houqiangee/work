package getdata;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import util.HttpRequestUtil;

import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.Sql;
import com.dareway.framework.util.database.Transaction;
import com.dareway.framework.util.database.TransactionManager;

public class insertYzm {
	
	public static void main(String str[]){
		GlobalNames.CONFIGINWAR="true";
		
		GetYzmThread gt=new GetYzmThread();
		gt.setSpid("123").start();
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
		        Thread.sleep(10000);
		        
		        WebDriver driver1 = new ChromeDriver();
		        driver1.get("https://login.taobao.com/member/login.jhtml?f=top&amp;redirectURL=https%3A%2F%2Fwww.taobao.com%2F");
		        
		        Set<Cookie> cookieSet=driver.manage().getCookies();
		        for (Cookie ck: cookieSet) {  
		        	driver1.manage().addCookie(ck);
		        }   
		        
		        driver1.get("http://miao.item.taobao.com/"+spid+".htm?spm=5070.7116829.1996665325.15.XfRS4F");
		        Thread.sleep(2000);
		        Transaction tm=TransactionManager.getTransaction();tm.begin();
				Sql sql=new Sql();
				while(true){
					sql.setSql("insert into stock.taobao_yzm(imgurl) select ? as imgurl from dual where not exists(select 1 from stock.taobao_yzm x where x.imgurl=?) ");
					for(int i=0;i<200;i++){
						driver1.get("http://m.ajax.taobao.com/qst.htm?_ksTS=1536840041941_761&cb=jsonp762&id="+spid);
			        	String re=driver1.findElement(By.tagName("pre")).getText();
			        	if(re==null || "".equals(re)){
			        		continue;
			        	}
			        	re=re.replace("jsonp762(", "").replace(")", "");
			        	JSONObject jo=JSONObject.fromObject(re);
			        	String imgurl=jo.getString("qst");
			        	sql.setString(1, imgurl);
			        	sql.setString(2, imgurl);
			        	sql.addBatch();
			        }
					sql.executeBatch();
					tm.commit();
				}
			}catch(Exception e){
			}
		};
		
	}
}
