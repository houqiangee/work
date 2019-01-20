package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * @author hq
 * @version 1.0
 */
public class MSHsUtil {
	public static int TimeoutSECS=30;//默认超时秒数
	
	//获取某个文件的字节数组
	public static byte[] getContent(String filePath) throws IOException {
		File file = new File(filePath);
		long fileSize = file.length();
		if (fileSize > Integer.MAX_VALUE) {
			System.out.println("file too big...");
			return null;
		}
		FileInputStream fi = new FileInputStream(file);
		byte[] buffer = new byte[(int) fileSize];
		int offset = 0;
		int numRead = 0;
		while (offset < buffer.length
				&& (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
			offset += numRead;
		}
		if (offset != buffer.length) {
			fi.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}
		fi.close();
		return buffer;
	}
	
	//获取某个文件的字节数组
	public static void deleteFile(String filePath) {
		try{
			File file = new File(filePath);
			file.delete();
		}catch(Exception e){
		}
	}
	
	/** 
	 * 判断页面上是否有某元素
	 * timeout:最多等待时间（秒）
	 */
	public static boolean haveElement(WebDriver driver,By by,int timeout) {
		driver.manage().timeouts().implicitlyWait(timeout,TimeUnit.SECONDS);//识别元素时的超时时间
        try{
        	driver.findElement(by);
        	driver.manage().timeouts().implicitlyWait(MSHsUtil.TimeoutSECS,TimeUnit.SECONDS);
        	return true;
        }catch(NoSuchElementException e){
        	driver.manage().timeouts().implicitlyWait(MSHsUtil.TimeoutSECS,TimeUnit.SECONDS);
        	return false;
        }
	}
	
	/** 
	 * 判断页面上是否有某元素
	 */
	public static boolean haveElement(WebDriver driver,By by) {
		return haveElement(driver,by,5);//默认等5秒
	}
	
	/** 
	 * 判断页面上是否有某元素
	 */
	public static boolean haveElement(WebDriver driver,WebElement we,By by,int timeout) {
		driver.manage().timeouts().implicitlyWait(timeout,TimeUnit.SECONDS);//识别元素时的超时时间
        try{
        	we.findElement(by);
        	driver.manage().timeouts().implicitlyWait(MSHsUtil.TimeoutSECS,TimeUnit.SECONDS);
        	return true;
        }catch(NoSuchElementException e){
        	driver.manage().timeouts().implicitlyWait(MSHsUtil.TimeoutSECS,TimeUnit.SECONDS);
        	return false;
        }
	}
	
	/** 
	 * 判断页面上是否有某元素
	 */
	public static boolean haveElement(WebDriver driver,WebElement we,By by) {
		return haveElement(driver,we,by,3);//默认等3秒
	}
	
	/** 
	 * 判断页面上是否有某元素
	 * @throws Exception 
	 */
	public static void waitElementDisappear(WebDriver driver,By by,int maxwaitsecs) throws Exception {
		int maxwaitmil=maxwaitsecs*1000;
		int millis=0;
		while(haveElement(driver,by,1)){
			Thread.sleep(200);
			millis+=200;
			if(millis>maxwaitmil){
				throw new Exception("所等待元素已经超过"+maxwaitsecs+"秒还未消失！");
			}
		}
	}
	
	/** 
	 * 判断页面上是否有某元素
	 * @throws Exception 
	 */
	public static void waitElementDisappear(WebDriver driver,By by) throws Exception {
		waitElementDisappear(driver,by,10);
	}
}
