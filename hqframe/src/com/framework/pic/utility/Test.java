package com.framework.pic.utility;

import java.io.IOException;

import com.framework.pic.ui.MainFrame;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			
		
      
		String source="M:\\hqself\\1.png";
		
		double start=System.currentTimeMillis();
		
		
		int [] pics=new int[15];
		for(int i=1;i<=1;i++){
			String target="M:\\hqself\\2.png";
			new MainFrame(source, target).show(true);    	    
		}
      
		double end=System.currentTimeMillis(); 
		System.out.println(end-start);
     // System.out.println(Math.atan2(-2, 1)-Math.atan2(1, 2)+"\n"+Math.atan2(1, 2));
      
    /*
     ///打印高斯模板
       double baseSigma=1.6;
      int gaussS=6,s=3;
      
      double[] sig=new double[6];
		sig[0]=baseSigma;
		for(int i=1;i<gaussS;i++){
			double preSigma=baseSigma*Math.pow(2, (double)(i-1)/s);
			double nextSigma=preSigma*Math.pow(2, (double)1/s);
			sig[i]=Math.sqrt(nextSigma*nextSigma-preSigma*preSigma);
			
		}
      for(double d:sig){
    	  double[][] g=ImageTransform.getGaussTemplate(d);
    	  System.out.println("\n\n\nsigma:"+d);
    	   for(double[] g1:g){
    		   System.out.print("{");
    	    	  for(double g2:g1){
    			  System.out.print(g2+",");
    		  }
    		  System.out.print("},");
        	  System.out.println("");
    	  }
    	  
      }*/
      
      
      
      
	}	
}
