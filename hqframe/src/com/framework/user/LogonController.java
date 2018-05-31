package com.framework.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.framework.layer.Controller;
import com.framework.util.DataObject;


public class LogonController extends Controller{
	/**
	 * µÇÂ¼Ò³Ãæ
	 * @author hq
	 */
	public ModelAndView gologonPage(HttpServletRequest request,
			HttpServletResponse response, DataObject para) throws Exception {
		return this.goPage("/hqframe/farme/dologon.jsp");
	}
	
	/**
	 * µÇÂ¼
	 * @author hq
	 */
	public ModelAndView dologon(HttpServletRequest request,
			HttpServletResponse response, DataObject para) throws Exception {
		
		
		
		return this.writeMsg(response, "µÇÂ¼³É¹¦");
	}
	
}