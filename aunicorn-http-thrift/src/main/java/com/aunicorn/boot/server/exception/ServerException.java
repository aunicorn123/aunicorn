package com.aunicorn.boot.server.exception;

public class ServerException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	private int code ;
	
	private String msg ;
	
	public ServerException(int code , String msg){
		super(msg);
		this.code = code ;
		this.msg = msg ;
	}
	
	public ServerException(int code , String msg , Exception e){
		super(msg , e);
		this.code = code ;
		this.msg = msg ;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
