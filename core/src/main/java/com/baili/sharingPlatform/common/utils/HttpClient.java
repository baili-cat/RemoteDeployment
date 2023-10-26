/*
 * Created by baili on 2021/09/15.
 */
package com.baili.sharingPlatform.common.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baili.sharingPlatform.common.jackson.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author baili
 * @date 2021/09/15.
 */
@Slf4j
public class HttpClient {

	/**
	 * 发送http请求
	 */
	public static <T> T sendRequest(HttpRequest request, Converter<T, String> converter) {
		HttpResponse response = null;
		try {
			response = request.execute();
			if (response.getStatus() < 200 || response.getStatus() >= 300) {
				throw new HttpAccessException(request, response);
			}

			if (converter != null) {
				return converter.convert(response.body());
			}
			return null;
		} catch (HttpAccessException e) {
			throw e;
		} catch (Throwable e) {
			throw new HttpAccessException(request, response, e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public interface Converter<T, E> {

		T convert(E value);
	}

	public static class HttpAccessException extends RuntimeException {

		private final HttpRequest  request;
		private final HttpResponse response;
		private final String       message;

		public HttpAccessException(HttpRequest request, HttpResponse response) {
			this(request, response, null);
		}

		public HttpAccessException(HttpRequest request, HttpResponse response, Throwable e) {
			super(e);
			this.request = request;
			this.response = response;
			this.message = buildErrorMessage(request, response);
		}

		private String buildErrorMessage(HttpRequest request, HttpResponse response) {
			String msg = String.format("HTTP %s \"%s\" failed: params=%s", request.getMethod(), request.getUrl(), Jackson.toString(request.form()));
			if (response != null) {
				msg = String.format("%s, response={status=%s, body=%s}", msg, response.getStatus(), response.body());
			}
			if (StringUtils.isNotEmpty(super.getMessage())) {
				msg = String.format("%s cause: %s", msg, super.getMessage());
			}
			return msg;
		}

		public HttpRequest getRequest() {
			return request;
		}

		public HttpResponse getResponse() {
			return response;
		}

		@Override
		public String getMessage() {
			return this.message;
		}
	}
}
