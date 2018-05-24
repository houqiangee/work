package com.framework.exception;

public class BusinessException extends Exception {
	public BusinessException(String msg) {
		super(msg);
	}

	public BusinessException(String msg, String solution) {
		super(msg + solution);
	}

	public BusinessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}