package com.framework.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.framework.layer.Controller;
import com.framework.util.DataObject;


public class LogonController extends Controller{
	/**
	 * ��¼ҳ��
	 * @author hq
	 */
	public ModelAndView gologonPage(HttpServletRequest request,
			HttpServletResponse response, DataObject para) throws Exception {
		return this.goPage("/hqframe/farme/dologon.jsp");
	}
	
	/**
	 * ��¼
	 * @author hq
	 */
	public ModelAndView dologon(HttpServletRequest request,
			HttpServletResponse response, DataObject para) throws Exception {
		
		
		
		return this.writeMsg(response, "��¼�ɹ�");
	}
	
}