package com.baili.sharingPlatform.web;

import com.baili.sharingPlatform.common.jackson.Jackson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * API接口公共返回结果
 *
 * @author baili
 * @date 2019/05/06.
 */
@ApiModel("API接口公共返回结果")
public class ResponseEntity<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("API接口调用成功/失败标识")
	private boolean success;
	/**
	 * API接口调用，返回结果状态码
	 * {@link ResponseCode#OK} 标识API接口调用正常，结果数据正常返回
	 * {@link ResponseCode#SERVER_ERROR} 标识API接口调用失败，返回失败提示信息
	 * <p>
	 */
	@ApiModelProperty("状态码, 0表示API接口调用成功, 其它值表示API接口调用失败")
	private String  code;
	/**
	 * API接口调用失败，返回的错误提示信息
	 */
	@ApiModelProperty("错误信息，API接口调用失败才返回")
	private String  message;
	/**
	 * API接口调用成功，返回的结果数据
	 */
	@ApiModelProperty("结果数据，API接口调用成功才返回")
	private T       data;

	private ResponseEntity() {
	}

	public static <T> ResponseEntity<T> ok() {
		return ok(null);
	}

	public static <T> ResponseEntity<T> ok(T data) {
		return build(ResponseCode.OK.getCode(), null, data);
	}

	public static <T> ResponseEntity<T> fail(String message) {
		return build(ResponseCode.SERVER_ERROR.getCode(), message, null);
	}

	public static <T> ResponseEntity<T> fail(ResponseCode responseCode) {
		return build(responseCode.getCode(), responseCode.getMessage(), null);
	}

	public static <T> ResponseEntity<T> build(ResponseCode responseCode, T data) {
		return build(responseCode.getCode(), responseCode.getMessage(), data);
	}

	public static <T> ResponseEntity<T> build(String code, String message, T data) {
		ResponseEntity<T> responseEntity = new ResponseEntity<>();
		responseEntity.setSuccess(ResponseCode.OK.getCode().equals(code));
		responseEntity.setCode(code);
		responseEntity.setMessage(message);
		if (responseEntity.isSuccess()) {
			responseEntity.setData(data);
		}
		return responseEntity;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getCode() {
		return code;
	}

	public ResponseEntity<?> setCode(String code) {
		this.code = code;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public ResponseEntity<?> setMessage(String message) {
		this.message = message;
		return this;
	}

	public T getData() {
		return data;
	}

	public ResponseEntity<?> setData(T data) {
		this.data = data;
		return this;
	}

	public String toJSON() {
		return Jackson.toString(this);
	}

}
