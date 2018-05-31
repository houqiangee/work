package com.framework.exception;

public class BusinessException extends Exception {
	private String msg;
	public BusinessException(String msg) {
		super(msg);
		this.msg=msg;
	}

	public BusinessException(String msg, String solution) {
		super(msg + solution);
	}

	public BusinessException(String msg, Throwable cause) {
		super(msg, cause);
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}

	@Override
	public String toString() {
		return this.msg;
	}

}