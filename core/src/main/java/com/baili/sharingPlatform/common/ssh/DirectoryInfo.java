/*
 * Created by baili on 2021/04/14.
 */
package com.baili.sharingPlatform.common.ssh;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baili
 * @date 2021/04/14.
 */
@NoArgsConstructor
@Data
public class DirectoryInfo {

	// 目录
	private String path;
	// 权限
	private String guid;

	public DirectoryInfo(String path) {
		this.path = path;
	}

	public DirectoryInfo(String path, String guid) {
		this.path = path;
		this.guid = guid;
	}
}
