/*
 * Created by baili on 2021/09/15.
 */
package com.baili.sharingPlatform.service.ServiceXxlJob;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.baili.sharingPlatform.common.jackson.Jackson;
import com.baili.sharingPlatform.common.utils.BeanUtils;
import com.baili.sharingPlatform.service.ServiceXxlJob.XxlJobProperties.AdminProperties;
import com.baili.sharingPlatform.service.ServiceXxlJob.model.JobInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author baili
 * @date 2021/09/15.
 */
@Slf4j
public class XxlJobAdminManager {

    private final String DEFAULT_SERVER_VERSION = "2.3.0.1";

    private XxlJobProperties properties;
    private String baseUrl;
    private Long jobGroupId;
    private String serverVersion;
    private boolean lowVersion;

    public XxlJobAdminManager(XxlJobProperties properties) {
        this.properties = properties;
        this.baseUrl = buildBaseUrl(properties.getAdmin().getAddress());
        this.serverVersion = getServerVersion();
        this.lowVersion = DEFAULT_SERVER_VERSION.equals(this.serverVersion);

        if (lowVersion) {
            this.jobGroupId = properties.getExecutor().getGroupId();
            if (this.jobGroupId == null) {
                // XXL-JOB-ADMIN服务端运行版本低于2.3.0.2
                throw new IllegalArgumentException(
                        "当前应用未设置XXL-JOB任务执行器(GroupId)，可通过修改Spring配置文件(application.yml)添加配置项['xxl-job.executor" + ".groupId']设置XXL-JOB任务执行器"
                                + "(GroupId)");
            }
        } else {
            this.jobGroupId = initJobGroup(properties.getExecutor().getAppName());
        }
    }

    private String buildBaseUrl(String address) {
        address = StringUtils.removeEnd(address, "/");
        if (!address.contains("://")) {
            address = "http://" + address;
        }
        return address;
    }

    /**
     * 读取xxl-job-admin服务端版本
     */
    private String getServerVersion() {
        HttpRequest request = HttpRequest.get(buildApiURL("/version"));
        request.setConnectionTimeout(30000).setReadTimeout(60000);
        try (HttpResponse response = request.execute()) {
            // 服务端从2.3.0.2版本开始新增了/version接口，如果低于此版本会返回302状态码跳转到登录页面
            if (response.getStatus() == 302) {
                return DEFAULT_SERVER_VERSION;
            }

            if (response.getStatus() != 200) {
                throw new HttpAccessException(request, response);
            }

            JsonNode jsonNode = Jackson.readValue(response.body());
            if (!jsonNode.has("code") || jsonNode.get("code").intValue() != 200) {
                throw new HttpAccessException(request, response);
            }

            return jsonNode.get("content").asText();
        }
    }

    private String buildApiURL(String path) {
        return baseUrl + path;
    }

    /**
     * 初始化执行器
     */
    private Long initJobGroup(String appName) {
        JobGroup jobGroup;
        try {
            jobGroup = sendRequest(HttpRequest.post(buildApiURL(String.format("/jobgroup/loadByAppName?appname=%s", appName))),
                    value -> Jackson.valueToObject(value, JobGroup.class));
        } catch (Throwable e) {
            throw new IllegalStateException("初始化XXL-JOB执行器失败, 查询执行器出错", e);
        }
        if (jobGroup != null) {
            return jobGroup.getId();
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("appname", appName);
            params.put("title", appName);
            params.put("addressType", 0);

            Long jobGroupId = sendRequest(HttpRequest.post(buildApiURL("/jobgroup/save")).form(params), value -> value.get("id").longValue());
            log.info("创建XXL-JOB执行器成功, JobGroupId:{}, Params:{}", jobGroupId, Jackson.toString(params));
            return jobGroupId;
        } catch (Throwable e) {
            throw new IllegalStateException("初始化XXL-JOB执行器失败, 创建执行器出错", e);
        }
    }

    /**
     * 添加任务
     */
    public Long addJob(JobInfo jobInfo) {
        try {
            jobInfo.setId(null);
            jobInfo.setJobGroup(jobGroupId);
            Map<String, Object> params = BeanUtils.beanToMap(jobInfo, false, true);

            Long id = sendRequest(HttpRequest.post(buildApiURL("/jobinfo/add")).form(params), JsonNode::asLong);
            log.info("添加XXL-JOB定时任务成功, JobID:{}, Params:{}", id, Jackson.toString(params));
            return id;
        } catch (Throwable e) {
            log.error("添加XXL-JOB定时任务失败, JobInfo:{}", Jackson.toString(jobInfo), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新任务
     */
    public Long updateJob(JobInfo jobInfo) {
        try {
            jobInfo.setJobGroup(jobGroupId);
            Map<String, Object> params = BeanUtils.beanToMap(jobInfo, false, true);

            sendRequest(HttpRequest.post(buildApiURL("/jobinfo/update")).form(params), null);
            log.info("修改XXL-JOB定时任务成功, JobID:{}, Params:{}", jobInfo.getId(), Jackson.toString(params));
            return jobInfo.getId();
        } catch (Throwable e) {
            log.error("修改XXL-JOB定时任务失败, JobInfo:{}", Jackson.toString(jobInfo), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询任务(仅支持xxl-job-admin:2.3.0.2之后版本)
     */
    public JobInfo getJob(long id) {
        if (lowVersion) {
            throw new UnsupportedOperationException("XXL-JOB-ADMIN服务端运行版本低于<2.3.0.2>，不支持调用此接口");
        }
        try {
            return sendRequest(HttpRequest.post(buildApiURL(String.format("/jobinfo/getById?id=%s", id))),
                    value -> Jackson.valueToObject(value, JobInfo.class));
        } catch (Throwable e) {
            log.error("查询XXL-JOB定时任务失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询任务(仅支持xxl-job-admin:2.3.0.2之后版本)
     */
    public List<JobInfo> findJob(Integer triggerStatus, String jobKey, String executorHandler) {
        if (lowVersion) {
            throw new UnsupportedOperationException("XXL-JOB-ADMIN服务端运行版本低于<2.3.0.2>，不支持调用此接口");
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("jobGroup", jobGroupId);
            params.put("triggerStatus", triggerStatus);
            params.put("jobKey", jobKey);
            params.put("executorHandler", executorHandler);

            return sendRequest(HttpRequest.post(buildApiURL("/jobinfo/findList")).form(params), value -> Jackson.valueToArray(value, JobInfo.class));
        } catch (Throwable e) {
            log.error("查询XXL-JOB定时任务失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除任务
     */
    public boolean deleteJob(long id) {
        try {
            sendRequest(HttpRequest.post(buildApiURL(String.format("/jobinfo/remove?id=%s", id))), null);
            log.info("删除XXL-JOB定时任务成功, JobID:{}", id);
            return true;
        } catch (Throwable e) {
            log.error("删除XXL-JOB定时任务失败, JobID:{}", id, e);
            return false;
        }
    }

    /**
     * 启动任务
     */
    public boolean startJob(long id) {
        try {
            sendRequest(HttpRequest.post(buildApiURL(String.format("/jobinfo/start?id=%s", id))), null);
            log.info("启动XXL-JOB定时任务成功, JobID:{}", id);
            return true;
        } catch (Throwable e) {
            log.error("启动XXL-JOB定时任务失败, JobID:{}", id, e);
            return false;
        }
    }

    /**
     * 停止任务
     */
    public boolean stopJob(long id) {
        try {
            sendRequest(HttpRequest.post(buildApiURL(String.format("/jobinfo/stop?id=%s", id))), null);
            log.info("停止XXL-JOB定时任务成功, JobID:{}", id);
            return true;
        } catch (Throwable e) {
            log.error("停止XXL-JOB定时任务失败, JobID:{}", id, e);
            return false;
        }
    }

    /**
     * 触发执行任务
     */
    public boolean triggerJob(long id, String executorParam, String addressList) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            params.put("executorParam", executorParam);
            params.put("addressList", addressList);

            sendRequest(HttpRequest.post(buildApiURL("/jobinfo/trigger")).form(params), null);
            log.info("触发执行XXL-JOB定时任务成功, JobID:{}, executorParam:{}, addressList:{}", id, executorParam, addressList);
            return true;
        } catch (Throwable e) {
            log.error("触发执行XXL-JOB定时任务失败, JobID:{}, executorParam:{}, addressList:{}", id, executorParam, addressList, e);
            return false;
        }
    }

    /**
     * 发送http请求
     */
    private <T> T sendRequest(HttpRequest request, Converter<T, JsonNode> converter) {
        HttpResponse response = null;
        try {
            request.setConnectionTimeout(30000).setReadTimeout(60000);
            if (this.lowVersion) {
                request.header("Cookie", getAuthCookies());
            } else {
                // xxl-job-admin:2.3.0.2之后版本才支持basicAuth认证方式
                request.basicAuth(properties.getAdmin().getUser(), properties.getAdmin().getPassword());
            }
            response = request.execute();
            if (response.getStatus() != 200) {
                throw new HttpAccessException(request, response);
            }

            JsonNode jsonNode = Jackson.readValue(response.body());
            if (!jsonNode.has("code") || jsonNode.get("code").intValue() != 200) {
                throw new HttpAccessException(request, response);
            }

            if (jsonNode.has("content") && converter != null) {
                return converter.convert(jsonNode.get("content"));
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

    private String getAuthCookies() {
        AdminProperties admin = properties.getAdmin();
        Map<String, Object> params = new HashMap<>();
        params.put("userName", admin.getUser());
        params.put("password", admin.getPassword());

        HttpRequest request = HttpRequest.post(buildApiURL("/login")).form(params);
        request.setConnectionTimeout(30000).setReadTimeout(60000);
        try (HttpResponse response = request.execute()) {
            if (response.getStatus() != 200) {
                throw new HttpAccessException(request, response);
            }
            return response.getCookies().stream().map(HttpCookie::toString).collect(Collectors.joining());
        }
    }

    private interface Converter<T, E> {

        T convert(E value);
    }

    @Data
    static class JobGroup {

        private Long id;
        private String appname;
        private String title;
        private Integer addressType;// 执行器地址类型：0=自动注册、1=手动录入
    }

    static class HttpAccessException extends RuntimeException {

        private final HttpRequest request;
        private final HttpResponse response;
        private final String message;

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
