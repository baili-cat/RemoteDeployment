/*
 * Created by baili on 2021/09/15.
 */
package com.baili.sharingPlatform.service.ServiceXxlJob;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author baili
 * @date 2021/09/15.
 */
@ConfigurationProperties("xxl-job")
public class XxlJobProperties {

	private final AdminProperties    admin    = new AdminProperties();
	private final ExecutorProperties executor = new ExecutorProperties();

	public AdminProperties getAdmin() {
		return admin;
	}

	public ExecutorProperties getExecutor() {
		return executor;
	}

	public static class AdminProperties {

		private String address;
		private String user;
		private String password;
		private String accessToken;

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
	}

	public static class ExecutorProperties {

		private Long    groupId;
		private String  appName;
		private String  ip;
		private Integer port;
		private String  logPath;
		private Integer logRetentionDays = 3;

		public Long getGroupId() {
			return groupId;
		}

		public void setGroupId(Long groupId) {
			this.groupId = groupId;
		}

		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public String getLogPath() {
			return logPath;
		}

		public void setLogPath(String logPath) {
			this.logPath = logPath;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public Integer getPort() {
			return port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public Integer getLogRetentionDays() {
			return logRetentionDays;
		}

		public void setLogRetentionDays(Integer logRetentionDays) {
			this.logRetentionDays = logRetentionDays;
		}
	}
}
