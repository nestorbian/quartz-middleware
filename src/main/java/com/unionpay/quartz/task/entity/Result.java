package com.unionpay.quartz.task.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class Result<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3417236081038992732L;
	
	public static final int SUCCESS = 0;
	public static final int UNKNOWN_EXCEPTION = 49;
	
	private int status = SUCCESS;
	private String message;
	private T data;
	
	public Result(T data) {
		super();
		this.data = data;
	}

	public Result(int status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	
	public Result(Throwable e) {
		super();
		this.status = UNKNOWN_EXCEPTION;
		this.message = e.toString();
	}
	
}
