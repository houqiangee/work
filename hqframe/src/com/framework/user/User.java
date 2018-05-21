package com.framework.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.framework.exception.UserlessException;

public class User implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private String userid;
	private String username;
	private String password;
	private ConcurrentHashMap<String, Object> customPropertyMap;

	public User() {
		this.customPropertyMap = new ConcurrentHashMap();
	}

	public Object getValue(String propertyName) {
		if (this.customPropertyMap.containsKey(propertyName)) {
			return this.customPropertyMap.get(propertyName);
		}
		return null;
	}

	public void setProperty(String propertyName, Object property) {
		if (property != null)
			this.customPropertyMap.put(propertyName, property);
	}

	public void removeProperty(String propertyName) {
		if (this.customPropertyMap.containsKey(propertyName))
			this.customPropertyMap.remove(propertyName);
	}

	public boolean containsProperty(String propertyName) {
		return this.customPropertyMap.containsKey(propertyName);
	}

	public HashMap<String, Object> getPropertyMap() {
		HashMap tmp = new HashMap();
		for (Entry key : this.customPropertyMap.entrySet()) {
			tmp.put(key.getKey(), key.getValue());
		}
		return tmp;
	}

	public String getUserid() throws UserlessException {
		if(this.userid==null || "".equals(this.userid)){
			throw new UserlessException("Userless");
		}
		return this.userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}