package com.framework.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class ActionUtil
{
  public static void writeMessageToResponse(HttpServletResponse response, String data)
  {
    response.setContentType("text/html;charset=GBK");
    if (data == null) {
      data = "";
    }
    PrintWriter out = null;
    try {
      response.setContentLength(data.getBytes("GBK").length);
      out = response.getWriter();
      out.print(data);
      out.close();
    } catch (IOException ex) {
    	ex.printStackTrace();
    }
  }
}