package business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class inputYzm {
    
    public static List<File> getFiles(String path){
        File root = new File(path);
        List<File> files = new ArrayList<File>();
        if(!root.isDirectory()){
            files.add(root);
        }else{
            File[] subFiles = root.listFiles();
            for(File f : subFiles){
                files.addAll(getFiles(f.getAbsolutePath()));
            }    
        }
        return files;
    }
         
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        List<File> files = getFiles("M:\\hqself\\_题库大全_增加四汉字\\all");
        for(File f : files){
        	String base64=getFileBase64(f);
            System.out.println(hex_md5(base64.getBytes()));
        }
    }
    
	public static String getFileBase64(File file) throws IOException {
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
			throw new IOException("Could not completely read file "+ file.getName());
		}
		fi.close();
		
		Base64 base64 = new Base64();
        byte bin[] = base64.encode(buffer);
        return new String(bin);
	}
	
	public static String hex_md5(byte plainBytes[]) throws NoSuchAlgorithmException
	    {
	        StringBuffer buf;
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        md.update(plainBytes);
	        byte b[] = md.digest();
	        buf = new StringBuffer("");
	        for(int offset = 0; offset < b.length; offset++)
	        {
	            int i = b[offset];
	            if(i < 0)
	                i += 256;
	            if(i < 16)
	                buf.append("0");
	            buf.append(Integer.toHexString(i));
	        }

	        return buf.toString();
	    }
}
