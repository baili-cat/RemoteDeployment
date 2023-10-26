/*
 * Created by baili on 2021/01/27.
 */
package com.baili.sharingPlatform.service;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baili.sharingPlatform.common.jackson.Jackson;
import com.baili.sharingPlatform.common.utils.DateUtils;
import com.baili.sharingPlatform.common.utils.HttpClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Apollo配置管理接口
 *
 * @author baili
 * @date 2021/01/27.
 */
@Slf4j
@Service
public class ServiceApolloClientHelper {

	private static final String APPLICATION_JSON = "application/json";

	private String baseUrl;
	private String username;
	private String password;

	/**
	 * 设置Apollo接口访问地址
	 */
	public void setApolloUrl(String address, String username, String password) {
		this.baseUrl = String.format("http://%s", address);
		this.username = username;
		this.password = password;
	}

	private String buildApiURL(String path) {
		if (StringUtils.isBlank(baseUrl)) {
			throw new IllegalStateException("Apollo address is not initialized");
		}
		return baseUrl + path;
	}

	/**
	 * 获取App列表
	 */
	public List<String> getApps() {
		String url = buildApiURL("/apps");
		return executeRequest(HttpRequest.get(url), value -> {
			List<App> apps = Jackson.parseArray(value, App.class);
			return apps.stream().map(App::getAppId).collect(Collectors.toList());
		});
	}

	/**
	 * 创建App
	 */
	public void createApp(String appId, String name, String ownerName) {
		Map<String, Object> params = new HashMap<>();
		params.put("appId", appId);
		params.put("name", name);
		params.put("orgId", "DEFAULT");
		params.put("orgName", "默认部门");
		params.put("ownerName", ownerName);
		params.put("admins", new ArrayList<String>());

		String url = buildApiURL("/apps");
		executeRequest(HttpRequest.post(url).contentType(APPLICATION_JSON).body(Jackson.toString(params)), null);
	}

	/**
	 * 全量更新配置
	 */
	public void updateAllConfig(String appId, String namespace, Map<String, String> configs) {
		Namespace nas = getNamespace(appId, namespace);
		StringBuilder configText = new StringBuilder();
		configs.forEach((key, value) -> configText.append(String.format("%s=%s\n", key, value)));
		Map<String, Object> params = new HashMap<>();
		params.put("format", "properties");
		params.put("configText", configText.toString());
		// 兼容Apollo v2.0.x版本
		params.put("namespaceId", nas.getBaseInfo().getId());

		String url = buildApiURL(String.format("/apps/%s/envs/PRO/clusters/default/namespaces/%s/items", appId, namespace));
		executeRequest(HttpRequest.put(url).contentType(APPLICATION_JSON).body(Jackson.toString(params)), null);
	}

	/**
	 * 增量更新配置
	 */
	public void updateIncrConfig(String appId, String namespace, Map<String, String> configs) {
		Map<String, String> updateConfig = new HashMap<>();
		ReleaseDTO releaseDTO = getActiveReleaseConfig(appId, namespace);
		if (releaseDTO != null) {
			Map<String, String> releaseMap = Jackson.readValue(releaseDTO.getConfigurations(), new TypeReference<HashMap<String, String>>() {});
			updateConfig.putAll(releaseMap);
		}
		updateConfig.putAll(configs);
		updateAllConfig(appId, namespace, updateConfig);
		releaseConfig(appId, namespace, "");
	}

	/**
	 * Release配置
	 */
	public void releaseConfig(String appId, String namespace, String releaseComment) {
		Map<String, Object> params = new HashMap<>();
		params.put("releaseTitle", DateUtils.format(new Date(), "yyyyMMddHHmmss") + "-release");
		params.put("releaseComment", releaseComment);
		params.put("isEmergencyPublish", false);

		String url = buildApiURL(String.format("/apps/%s/envs/PRO/clusters/default/namespaces/%s/releases", appId, namespace));
		executeRequest(HttpRequest.post(url).contentType(APPLICATION_JSON).body(Jackson.toString(params)), null);
	}

	/**
	 * 获取最新Release配置
	 */
	public ReleaseDTO getActiveReleaseConfig(String appId, String namespace) {
		String url = buildApiURL(String.format("/apps/%s/envs/PRO/clusters/default/namespaces/%s/releases/active?page=0&size=1", appId, namespace));
		return executeRequest(HttpRequest.get(url), value -> {
			List<ReleaseDTO> releases = Jackson.parseArray(value, ReleaseDTO.class);
			return CollectionUtils.isEmpty(releases) ? null : releases.get(0);
		});
	}

	/**
	 * 创建Namespace
	 */
	public void createNamespace(String appId, String namespace, boolean isPublic, String comment) {
		Map<String, Object> params = new HashMap<>();
		params.put("appId", appId);
		params.put("name", namespace);
		params.put("comment", comment);
		params.put("isPublic", isPublic);
		params.put("format", "properties");

		String url = buildApiURL(String.format("/apps/%s/appnamespaces?appendNamespacePrefix=false", appId));
		executeRequest(HttpRequest.post(url).contentType(APPLICATION_JSON).body(Jackson.toString(params)), null);
	}

	/**
	 * 关联公共Namespace
	 */
	public void relationNamespace(String appId, String namespace) {
		String jsonParams =
				"[{\"env\":\"PRO\",\"namespace\":{\"appId\":\"" + appId + "\",\"clusterName\":\"default\",\"namespaceName\":\"" + namespace +
						"\"}}]";
		String url = buildApiURL(String.format("/apps/%s/namespaces", appId));
		executeRequest(HttpRequest.post(url).contentType(APPLICATION_JSON).body(jsonParams), null);
	}

	/**
	 * 获取Namespace列表
	 */
	public List<String> getNamespaces(String appId) {
		String url = buildApiURL(String.format("/apps/%s/envs/PRO/clusters/default/namespaces", appId));
		return executeRequest(HttpRequest.get(url), value -> {
			List<Namespace> apps = Jackson.parseArray(value, Namespace.class);
			return apps.stream().map(namespace -> namespace.getBaseInfo().getNamespaceName()).collect(Collectors.toList());
		});
	}

	/**
	 * 获取Namespace
	 */
	public Namespace getNamespace(String appId, String namespaceName) {
		String url = buildApiURL(String.format("/apps/%s/envs/PRO/clusters/default/namespaces/%s", appId, namespaceName));
		return executeRequest(HttpRequest.get(url), value -> Jackson.parseObject(value, Namespace.class));
	}

	/**
	 * 设置Namespace修改权限
	 */
	public void grantModifyNamespaceRole(String username, String appId, String namespace) {
		String url = buildApiURL(String.format("/apps/%s/namespaces/%s/roles/ModifyNamespace", appId, namespace));
		executeRequest(HttpRequest.post(url).contentType("text/plain").body(username), null);
	}

	/**
	 * 设置Namespace发布权限
	 */
	public void grantReleaseNamespaceRole(String username, String appId, String namespace) {
		String url = buildApiURL(String.format("/apps/%s/namespaces/%s/roles/ReleaseNamespace", appId, namespace));
		executeRequest(HttpRequest.post(url).contentType("text/plain").body(username), null);
	}

	/**
	 * 查找缺失应用环境
	 */
	public List<String> findAppMissEnvs(String appId) {
		String url = buildApiURL(String.format("/apps/%s/miss_envs", appId));
		return executeRequest(HttpRequest.get(url), value -> {
			MultiResponseEntity entity = Jackson.parseObject(value, MultiResponseEntity.class);
			return entity.getEntities().stream().map(MultiResponseEntity.RichResponseEntity::getBody).collect(Collectors.toList());
		});
	}

	/**
	 * 创建缺失应用
	 */
	public void createMissApp(String appId, String name, String ownerName) {
		Map<String, Object> params = new HashMap<>();
		params.put("appId", appId);
		params.put("name", name);
		params.put("orgId", "DEFAULT");
		params.put("orgName", "默认部门");
		params.put("ownerName", ownerName);
		params.put("ownerEmail", ownerName + "@baili.com");

		String url = buildApiURL("/apps/envs/PRO");
		executeRequest(HttpRequest.post(url).contentType(APPLICATION_JSON).body(Jackson.toString(params)), null);
	}

	/**
	 * 查找缺失Namespaces
	 */
	public List<String> findMissNamespaces(String appId) {
		String url = buildApiURL(String.format("/apps/%s/envs/PRO/clusters/default/missing-namespaces", appId));
		return executeRequest(HttpRequest.get(url), value -> {
			MultiResponseEntity entity = Jackson.parseObject(value, MultiResponseEntity.class);
			return entity.getEntities().stream().map(MultiResponseEntity.RichResponseEntity::getBody).collect(Collectors.toList());
		});
	}

	/**
	 * 创建缺失Namespaces
	 */
	public void createMissNamespaces(String appId) {
		String url = buildApiURL(String.format("/apps/%s/envs/PRO/clusters/default/missing-namespaces", appId));
		executeRequest(HttpRequest.post(url), null);
	}

	/**
	 * 执行http请求
	 */
	private <T> T executeRequest(HttpRequest request, HttpClient.Converter<T, String> converter) {
		request.setConnectionTimeout(30000).setReadTimeout(60000);
		request.basicAuth(username, password);
		return HttpClient.sendRequest(request, converter);
	}

	@Data
	static class MultiResponseEntity {

		private int                      code;
		private List<RichResponseEntity> entities = new LinkedList<>();

		@Data
		static class RichResponseEntity {

			private int    code;
			private String message;
			private String body;
		}
	}

	@Data
	static class Namespace {

		private BaseInfo baseInfo;
		private String   format;
		private boolean  isPublic;
		private String   comment;

		@Data
		static class BaseInfo {

			private long   id;
			private String appId;
			private String clusterName;
			private String namespaceName;
		}
	}

	@Data
	static class App {

		private String name;
		private String appId;
		private String orgId;
		private String orgName;
		private String ownerName;
	}

	@Data
	static class ReleaseDTO {

		private long    id;
		private String  releaseKey;
		private String  name;
		private String  appId;
		private String  clusterName;
		private String  namespaceName;
		private String  configurations;
		private String  comment;
		private boolean isAbandoned;

	}

}

