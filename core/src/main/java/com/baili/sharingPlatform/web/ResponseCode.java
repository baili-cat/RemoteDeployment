package com.baili.sharingPlatform.web;

/**
 * 框架预定义系统级别返回码
 *
 * @author baili
 * @date 2019/05/06.
 */
public enum ResponseCode {

	OK("100000", ""),//
	Unauthorized("130000", "用户未登录"),//
	SERVER_ERROR("500", "服务器繁忙，请稍候重试");

	private String code;
	private String message;

	ResponseCode(String code, String msg) {
		this.code = code;
		this.message = msg;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
