/*
 * Created by baili on 2020/12/05.
 */
package com.baili.sharingPlatform.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baili
 * @date 2020/12/05.
 */
@NoArgsConstructor
@Data
public class HostPort {

	private String  host;
	private Integer port;

	public HostPort(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	public String toAddress() {
		return host + ":" + port;
	}

}
