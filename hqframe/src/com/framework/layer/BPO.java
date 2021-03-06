package com.framework.layer;

import com.framework.exception.BusinessException;
import com.framework.exception.UserlessException;
import com.framework.user.User;
import com.framework.util.CallStackTracer;
import com.framework.util.DataObject;
import com.framework.util.Sql;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BPO {

	public Sql sql;
	public Sql readonlysql;
	private User _user;

	public BPO() {
		this._user = null;
		this.sql = new Sql();
	}

	protected User getUser() {
		return this._user;
	}

	protected void setUser(User user) {
		this._user = user;
	}

	public final DataObject doMethod(String pMethodName, DataObject para, User user) throws Exception {
		setUser(user);
		String dbName = null;
		if (para.containsKey("dbName")) {
			dbName = para.getString("dbName");
		}
		Transaction tm = TransactionManager.getTransaction(dbName);
		int tmFlag = 0;
		try {
			String pMethodFullName = super.getClass().getName() + "." + pMethodName;

			CallStackTracer.startNode(super.getClass().getName(), pMethodFullName);

			tmFlag = tm.begin();
			Method method = super.getClass().getMethod(pMethodName, new Class[] { DataObject.class });
			DataObject vdo = (DataObject) method.invoke(this, new Object[] { para });
			if (tmFlag == 1) {
				tm.commitWithoutStart();
			}
			return vdo;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getTargetException();
            if(cause instanceof BusinessException 
               || cause instanceof UserlessException){
            }else{
            	e.printStackTrace();
            }
            throw e;
		} catch (Exception e) {
			e.printStackTrace();
			if (tmFlag == 1)
				;
			throw e;
		} finally {
			CallStackTracer.endNode();
		}
	}

	protected BPO newBPO(Class<BPO> clazz) {
		BPO clazzObj = null;
		try {
			clazzObj = (BPO) clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return clazzObj;
	}

	protected void BusinessException(String msg) throws BusinessException {
		throw new BusinessException(msg);
	}
}