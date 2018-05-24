package com.framework.layer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.mvc.multiaction.ParameterMethodNameResolver;

import com.framework.exception.BusinessException;
import com.framework.exception.UserlessException;
import com.framework.user.User;
import com.framework.util.ActionUtil;
import com.framework.util.DataObject;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;

public class Controller extends AbstractController {
	private MethodNameResolver methodNameResolver = new ParameterMethodNameResolver();
	protected static final Log pageNotFoundLogger = LogFactory.getLog("org.springframework.web.servlet.PageNotFound");

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String methodName = this.methodNameResolver.getHandlerMethodName(request);
			Method method = this.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class,DataObject.class);
			DataObject para=new DataObject();
		    Enumeration enu=request.getParameterNames();  
		    while(enu.hasMoreElements()){  
			    String paraName=(String)enu.nextElement();  
			    para.put(paraName, request.getParameter(paraName));
		    }
		    ModelAndView r = null;
		    Transaction tm=TransactionManager.getTransaction();
		    DataObject rdo=new DataObject();
		    try{
		    	r=(ModelAndView) method.invoke(this, request, response,para);
		    }catch(InvocationTargetException e){
		    	Throwable cause = e.getCause();
	            if(cause instanceof BusinessException){
	            	rdo.put("_hqglo_error_msg", e.getMessage());
			    	rdo.put("_hqglo_error_type", "bus");
			    	writeMsg(response, rdo.toJSON());
			    	return null;
	            }else if(cause instanceof UserlessException){
	            	rdo.put("_hqglo_error_msg", e.getMessage());
			    	rdo.put("_hqglo_error_type", "userless");
			    	writeMsg(response, rdo.toJSON());
			    	return null;
	            }
		    }catch(Exception e){
		    	e.printStackTrace();
		    	tm.rollbackWithoutStart();
		    	rdo.put("_hqglo_error_msg", "Õ¯¬Á“Ï≥££¨«Î…‘∫Û≥¢ ‘°£");
		    	rdo.put("_hqglo_error_type", "red");
		    	writeMsg(response, rdo.toJSON());
		    	return null;
		    }
		    tm.commitWithoutStart();
			return r;
		} catch (NoSuchRequestHandlingMethodException ex) {
			return handleNoSuchRequestHandlingMethod(ex, request, response);
		}
	}

	protected ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException ex, HttpServletRequest request, HttpServletResponse response) throws Exception {
		pageNotFoundLogger.warn(ex.getMessage());
		response.sendError(404);
		return null;
	}

	public User getUser(HttpServletRequest request) {
		User cu = (User) request.getSession().getAttribute("current_user");
		return cu;
	}

	protected ModelAndView writeMsg(HttpServletResponse response, String msg) throws Exception {
		ActionUtil.writeMessageToResponse(response, msg);
		return null;
	}

	protected ModelAndView goPage(String JspPath) throws Exception {
		return new ModelAndView(JspPath);
	}

	protected ModelAndView goPage(String JspPath, DataObject para) throws Exception {
		return new ModelAndView(JspPath, para);
	}

	protected <T extends BPO> T newBPO(Class<T> clazz){
		BPO clazzObj = null;
		try {
			clazzObj = (BPO) clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return (T) clazzObj;
	}
}