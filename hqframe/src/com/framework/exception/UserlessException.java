package com.framework.exception;

public class UserlessException extends Exception {
	public UserlessException(String msg) {
		super(msg);
	}

	public UserlessException(String msg, String solution) {
		super(msg + solution);
	}

	public UserlessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}