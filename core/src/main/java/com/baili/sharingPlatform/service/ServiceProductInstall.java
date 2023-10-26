package com.baili.sharingPlatform.service;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.dtflys.forest.http.ForestCookie;
import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baili.sharingPlatform.common.TestCaseException;
import com.baili.sharingPlatform.common.jackson.Jackson;
import com.baili.sharingPlatform.common.utils.CommonHttpClient;
import com.baili.sharingPlatform.model.installationTools.http.LoginMessage;
import com.baili.sharingPlatform.model.installationTools.http.ServerHostMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author baili
 * @date 2022年08月20日1:46 PM
 */
@Slf4j
@Service
public class ServiceProductInstall {
    //请求的超时时间，单位毫秒
    private int TIMEOUT = 300000;

    private ForestCookie forestCookie;

    private String productName;
    //部署平台端口
    private final String port = "8080";

    private String installationToolsUrl;
    //部署平台登录uri
    private static final String LOGIN_URI = "/api/auth/login";
    //获取产品集名称uri
    private static final String GET_PRODUCT_NAME_URI = "/api/productset/files";
    //部署平台导入产品集uri
    private static final String PRODUCTIMPORT_URI = "/api/productset/install/import";

    //产品集导入完成
    private static final String PRODUCTSET_IMPORT_FINISH = "/api/install/productset-import/finish";

    //添加服务器
    private static final String SERVER_HOST_ADD = "/api/serverHost/add";

    //获取服务器列表
    private static final String SERVER_HOST_LIST = "/api/serverHost/list";

    //服务器配置完成
    private static final String SERVER_CONFIGURE_FINISH = "/api/install/server-configure/finish";


    //设置单机部署方案
    private static final String CONFIGURE_STANDALONE = "/api/appInfo/configure/standalone";

    //应用部署配置完成
    private static final String DEPLOYMENT_CONFIGURE_FINISH = "/api/install/deployment-configure/finish";

    //产品集中包含的部署应用列表
    private static final String APP_INFO_LIST = "/api/appInfo/list";

    //服务一键部署
    private static final String QUICK_DEPLOY = "/api/appInfo/quick_deploy";

    //单个部署服务
    private static final String APP_DEPLOY = "/api/appInfo/deploy";

    //平台组件安装完成
    private static final String BASIC_SERVICE_INSTALL_FINISH = "/api/install/basic-service-install/finish";

    //获取平台需要执行的sql信息
    private static final String DATA_FILES = "/api/productset/dataFiles";

    //执行sql文件
    private static final String DATAFILE_QUICK_EXECUTE = "/api/dataFile/quick_execute";

    //忽略执行失败的sql
    private static final String DATAFILE_IGNORE = "/api/dataFile/ignore";

    //sql执行完成
    private static final String DATA_INIT_FINISH = "/api/install/data-init/finish";


    //获取license机器码
    private static final String LICENSE_MACHINE_INFO = "/api/license/getMachineInfo";

    //获取license信息
    private static final String LICENSE_INFO = "/api/license/getLicenseInfo";

    //上传license信息
    private static final String LICENSE_FILE_UPLOAD = "/api/license/uploadLicense";

    //导入license完成
    private static final String LICENSE_FILE_UPLOAD_FINISH = "/api/install/license-import/finish";

    //平台应用安装完成
    private static final String PLATFORM_SERVICE_INSTALL = "/api/install/platform-service-install/finish";

    //部署平台安装阶段信息获取uri
    private static final String SYSTEMINFO_URI = "/api/systemInfo";


    public void initInstallationToolsUrl(String installationToolsHostIp) {
        this.installationToolsUrl = "http://" + installationToolsHostIp + ":" + port;
    }

    public boolean login(LoginMessage loginMessage) {

        // 获取Cookie
        try {
            CommonHttpClient loginUrlClient = new CommonHttpClient();
            forestCookie = loginUrlClient.requestCookie(installationToolsUrl + LOGIN_URI, loginMessage.getLoginMessage());
            if (ObjectUtil.isNotEmpty(forestCookie)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    //获取安装阶段
    public String productInstallStage() {

        try {
            String productInstallStage =null;
            CommonHttpClient productInstallStageClient = new CommonHttpClient();
            AtomicReference<ForestResponse> responses = productInstallStageClient.requestGet(installationToolsUrl + SYSTEMINFO_URI,
                    forestCookie);
            ObjectMapper mapper = new ObjectMapper();

            //这里只取列表中的第一个产品集，多个产品集，多个产品集暂时不支持；
            JsonNode productInstallStageResult =
                    mapper.readTree(responses.get().getResult().toString()).get("data");
            productInstallStage =  productInstallStageResult.get("state").toString().replaceAll("\"", "").trim();
            return productInstallStage;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    //判断产品是否存在
    public String productNameActive(String productName) {
        CommonHttpClient productNameUrlClient = new CommonHttpClient();
        try {
            AtomicReference<ForestResponse> responses = productNameUrlClient.requestGet(installationToolsUrl + GET_PRODUCT_NAME_URI,
                    forestCookie);
            String productNameExist = null;
            ObjectMapper mapper = new ObjectMapper();

            //这里只取列表中的第一个产品集，多个产品集，多个产品集暂时不支持；
            JsonNode productNameResult = mapper.readTree(responses.get().getResult().toString()).get("data");
            if (StringUtils.isEmpty(productName)) {
                return mapper.readTree(responses.get().getResult().toString()).get("data").get(0).toString().replaceAll("\"", "").trim();
            }

            for (JsonNode name : productNameResult) {
                if (StringUtils.equals(name.toString().replaceAll("\"", "").trim(), productName)) {
                    productNameExist = name.toString().replaceAll("\"", "").trim();
                }
            }
            return productNameExist;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    //导入产品集并进入到服务部署方案配置阶段
    public boolean productImport(String productName) {
        CommonHttpClient productImportUrlClient = new CommonHttpClient();
        try {
            Map<String, Object> importPorductMap = new HashMap<>();
            importPorductMap.put("fileName", productName);
            AtomicReference<ForestResponse> responses = productImportUrlClient.requestPost(installationToolsUrl + PRODUCTIMPORT_URI,
                    importPorductMap, forestCookie, TIMEOUT);
            log.info("productImport请求结果：" + responses.toString());
            if (responses.get().isError()) {
                //// 获取请求响应状态码
                //int status = responses.get().getStatusCode();
                //return "status:" + status;
                log.error("产品集导入失败");
                return false;
            }
            //此处导入产品集成功，自动跳转到下一步
            CommonHttpClient productImportFinishUrlClient = new CommonHttpClient();
            responses = productImportFinishUrlClient.requestPost(installationToolsUrl + PRODUCTSET_IMPORT_FINISH, (Map<String, Object>) null, forestCookie, TIMEOUT);
            if (responses.get().isError()) {
                log.error("进入到服务配置阶段失败");
                return false;
            }
            log.info("产品集导入完成，进入到服务配置阶段，请求结果: " + responses.toString());
            return true;
        } catch (TestCaseException e) {
            return false;
        }
    }

    //添加服务器
    public boolean productServerConfigure(ServerHostMessage serverHostMessage) {
        CommonHttpClient serverHostAddClient = new CommonHttpClient();
        try {

            //ServerHostMessage serverHostMessage = new ServerHostMessage();
            serverHostMessage.setGroupId("1");
            serverHostMessage.setServerHostAutuType("password");
            //TODO 需要添加判断部署阶段，以及请求失败之后的返回记过处理
            AtomicReference<ForestResponse> responses = serverHostAddClient.requestPost(installationToolsUrl + SERVER_HOST_ADD,
                    serverHostMessage.getServerHostMessage(), forestCookie, TIMEOUT);
            log.info("productServerConfigure请求结果：" + responses.toString());
            if (responses.get().isError()) {
                //// 获取请求响应状态码
                //int status = responses.get().getStatusCode();
                //return "status:" + status;
                log.error("添加服务器失败");
                return false;
            }
            //需等待初始化完成，暂时等待10s
            Thread.sleep(10000);
            //此处导入产品集成功，自动跳转到下一步
            CommonHttpClient serverConfigureFinishUrlClient = new CommonHttpClient();
            responses = serverConfigureFinishUrlClient.requestPost(installationToolsUrl + SERVER_CONFIGURE_FINISH, (Map<String, Object>) null, forestCookie, TIMEOUT);
            if (responses.get().isError()) {
                log.error("进入到部署方案配置阶段失败");
                return false;
            }
            log.info("服务器添加完成，进入到服务配置阶段，请求结果: " + responses.toString());
            return true;
        } catch (TestCaseException e) {
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //单机部署方案配置
    public boolean productConfigureStandalone() {
        CommonHttpClient serverHostListUrlClient = new CommonHttpClient();
        try {
            Map<String, Object> serverType = new HashMap<>();
            //查询服务器列表时请求体
            serverType.put("serverType", "platform-service");
            AtomicReference<ForestResponse> serverListResponses =
                    serverHostListUrlClient.requestPost(installationToolsUrl + SERVER_HOST_LIST,
                            serverType, forestCookie, TIMEOUT);
            if (serverListResponses.get().isError()) {
                log.error("获取服务器列表失败");
                return false;
            }
            ObjectMapper serverListMapper = new ObjectMapper();
            //TODO 默认取第一个id，后续多机部署时要考虑
            String serverId = serverListMapper.readTree(serverListResponses.get().getResult().toString()).get(
                    "data").get(0).get("id").toString();
            log.info("serverId获取结果：" + serverId);
            if (StringUtils.isNotEmpty(serverId)) {
                //获取serverId
                Map<String, Object> standalone = new HashMap<>();
                //查询服务器列表时请求体
                standalone.put("serverId", serverId);
                standalone.forEach((key, value) -> {
                    System.out.println("第五种:" + key + " ：" + value);
                });
                CommonHttpClient configureStandaloneUrlClient = new CommonHttpClient();
                AtomicReference<ForestResponse> standaloneResponses =
                        configureStandaloneUrlClient.requestPost(installationToolsUrl + CONFIGURE_STANDALONE, standalone, forestCookie, TIMEOUT);
                if (standaloneResponses.get().isError()) {
                    log.error("单机部署方案配置失败");
                    return false;
                }
                log.info("单机部署方案配置完成，进入到服务配置阶段，请求结果: " + standaloneResponses.get().getResult().toString());
                CommonHttpClient serverConfigureFinishClient = new CommonHttpClient();
                AtomicReference<ForestResponse> serverConfigFinishResponses =
                        serverConfigureFinishClient.requestPost(installationToolsUrl + DEPLOYMENT_CONFIGURE_FINISH, (Map<String, Object>) null, forestCookie, TIMEOUT);
                if (serverConfigFinishResponses.get().isError()) {
                    log.error("单机部署方案配置完成后，进入到下一步失败");
                    return false;
                }
                return true;
            }

        } catch (TestCaseException e) {
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    //平台组件安装
    public boolean productBasicServiceInstall() {

        //基础组件部署结果 和license部署结果
        //TODO 需要兼容没有license情况，把基础服务部署和licesne部署分开，这里部署license可能不是必须的
        boolean componentInstallResult = false;
        try {


            //需要部署的平台组件id
            List<Object> componentIds = new ArrayList<>();
            //需要部署的后端服务license的id
            List<Object> licenseServerId = new ArrayList<>();
            componentIds = getServerIds("component");
            licenseServerId = getSpecifyNameServerIds("service", "license-server");
            List<Object> basicServerId = Stream.of(licenseServerId, componentIds).flatMap(Collection::stream).collect(Collectors.toList());
            CommonHttpClient basicServiceInstallUrlClient = new CommonHttpClient();
            AtomicReference<ForestResponse> appInfoListResponses =
                    basicServiceInstallUrlClient.requestPost(installationToolsUrl + QUICK_DEPLOY,
                            basicServerId, forestCookie, TIMEOUT);

            if (appInfoListResponses.get().isError()) {
                log.error("一键安装基础组件失败");
            }
            for (int i = 1; i <= 20; i++) {
                //循环等待服务部署完成后进入下一步，最长10分钟
                Thread.sleep(30000);

                //部署的服务中未完成部署的服务
                List<Object> basicServerIdResult = null;
                basicServerIdResult = getStatusMismatchServiceInfo(basicServerId, "running");
                log.info("服务" + basicServerIdResult + "未部署完成，已部署30*" + i + "s");
                //log.info("basicServerIdResult" + basicServerIdResult.toString());
                //log.info("basicServerId",basicServerId.toString());
                if (basicServerIdResult.isEmpty()) {
                    componentInstallResult = true;
                    break;
                }
            }

            if (componentInstallResult) {
                CommonHttpClient basicServiceInstallFinishUrlClient = new CommonHttpClient();
                AtomicReference<ForestResponse> basicServerFinishResponses =
                        basicServiceInstallFinishUrlClient.requestPost(installationToolsUrl + BASIC_SERVICE_INSTALL_FINISH,
                                (Map<String, Object>) null, forestCookie, TIMEOUT);
                log.info("基础组件安装完成，进入到数据初始化阶段");
                if (basicServerFinishResponses.get().isError()) {
                    componentInstallResult = false;
                    log.error("一键安装基础组件失败");
                }
            }
            return componentInstallResult;

        } catch (TestCaseException e) {
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //数据初始化
    public boolean productDataInitialization() {
        //数据初始化，执行sql结果
        boolean dataInitializationResult = false;
        try {
            //需要执行的sql id
            List<Object> sqlIds = new ArrayList<>();
            sqlIds = getSqlIds();
            log.info(sqlIds.toString());
            CommonHttpClient dataInitializationUrlClient = new CommonHttpClient();
            AtomicReference<ForestResponse> dataInitializationListResponses =
                    dataInitializationUrlClient.requestPost(installationToolsUrl + DATAFILE_QUICK_EXECUTE,
                            sqlIds, forestCookie, TIMEOUT);

            if (dataInitializationListResponses.get().isError()) {
                log.error("一键执行sql失败");
            }
            for (int i = 1; i <= 60; i++) {
                //循环等待服务部署完成后进入下一步，最长10分钟
                Thread.sleep(5000);

                //部署的服务中未完成部署的服务
                List<Object> failureSqlsIdResult = null;
                failureSqlsIdResult = getNotSpecifyStatusSqlIds("success");
                log.info("数据文件" + failureSqlsIdResult + "未执行成功，已等待30*" + i + "s");
                //log.info("basicServerIdResult" + basicServerIdResult.toString());
                //log.info("basicServerId",basicServerId.toString());
                if (failureSqlsIdResult.isEmpty()) {
                    dataInitializationResult = true;
                    break;
                }
            }
            if (dataInitializationResult) {
                CommonHttpClient dataInitFinishUrlClient = new CommonHttpClient();
                AtomicReference<ForestResponse> basicServerFinishResponses =
                        dataInitFinishUrlClient.requestPost(installationToolsUrl + DATA_INIT_FINISH,
                                (Map<String, Object>) null, forestCookie, TIMEOUT);
                log.info("数据初始化完成，进入到license上传阶段");
                if (basicServerFinishResponses.get().isError()) {
                    dataInitializationResult = false;
                    log.error("一键执行初始化sql失败");
                }
            }
            return dataInitializationResult;

        } catch (TestCaseException e) {
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    //TODO 忽略sql执行失败后续在添加
    ////数据初始化
    //public boolean productDataFileIgnore() {
    //    //数据初始化，执行sql结果
    //    boolean dataInitializationResult = false;
    //    try {
    //        //需要执行的sql id
    //            //部署的服务中未完成部署的服务
    //            List<Object> failureSqlsIdResult = null;
    //            failureSqlsIdResult = getNotSpecifyStatusSqlIds("success");
    //            //log.info("basicServerIdResult" + basicServerIdResult.toString());
    //            //log.info("basicServerId",basicServerId.toString());
    //            if (!failureSqlsIdResult.isEmpty()) {
    //                CommonHttpClient dataInitializationIgnoreUrlClient = new CommonHttpClient();
    //                AtomicReference<ForestResponse> appInfoListResponses =
    //                        dataInitializationIgnoreUrlClient.requestPost(installationToolsUrl + DATAFILE_IGNORE,
    //                                failureSqlsIdResult, forestCookie, TIMEOUT);
    //            }
    //
    //        return dataInitializationResult;
    //
    //    } catch (TestCaseException e) {
    //        return false;
    //    } catch (JsonProcessingException e) {
    //        throw new RuntimeException(e);
    //    } catch (InterruptedException e) {
    //        throw new RuntimeException(e);
    //    }
    //}

    //上传license文件
    public boolean productLiscenseFileUpload() {
        //获取license机器码
        CommonHttpClient licenseMachineUrlClient = new CommonHttpClient();
        AtomicReference<ForestResponse> licenseMachineResponses =
                licenseMachineUrlClient.requestGet(installationToolsUrl + LICENSE_MACHINE_INFO, forestCookie);
        if (licenseMachineResponses.get().isError()) {

            log.error("获取机器码失败");
            return false;
        }
        JsonNode appInfoList = Jackson.readValue(licenseMachineResponses.get().getResult().toString()).get(
                "data");
        String licenseMachine = appInfoList.get("code").toString().replaceAll("\"", "").trim();
        System.out.println("licenseMachine" + licenseMachine);
        if (StringUtils.isNotEmpty(licenseMachine)) {
            //获取license文件
            //临时存储的license路径
            String filePath = "/tmp/baili-vip-baili.lic";
            File devOpsToolsLicenseDownFile = productLicenseGenerate(licenseMachine, filePath);


            CommonHttpClient upDataUrlClient = new CommonHttpClient();
            AtomicReference<ForestResponse> upDataUrlClientResult =
                    upDataUrlClient.getUploadFile(installationToolsUrl + LICENSE_FILE_UPLOAD, "file", devOpsToolsLicenseDownFile
                            , forestCookie);
            if (upDataUrlClientResult.get().isError()) {
                log.error("上传license文件失败");
                return false;
            }

            //上传成功后进入到下一步
            CommonHttpClient licenseFinishUrlClient = new CommonHttpClient();
            AtomicReference<ForestResponse> licenseFinishUrlClientResult =
                    licenseFinishUrlClient.requestPost(installationToolsUrl + LICENSE_FILE_UPLOAD_FINISH,
                            (Map<String, Object>) null, forestCookie, TIMEOUT);
            if (upDataUrlClientResult.get().isError()) {
                log.error("导入license成功后，进入下一步失败");
                return false;
            }

        }
        return true;

    }

    //平台应用安装
    public boolean productPlatformServiceInstall() {

        //平台服务部署结果
        boolean platformServiceInstallResult = false;
        try {
            //需要部署的平台服务id
            //后端应用
            List<Object> platformServiceIds = new ArrayList<>();
            platformServiceIds =
                    Stream.of(getServerIds("service"), getServerIds("flinkjob"), getServerIds("webui")).flatMap(Collection::stream).collect(Collectors.toList());
            CommonHttpClient platformServiceInstallUrlClient = new CommonHttpClient();
            AtomicReference<ForestResponse> platformServiceInstallResponses =
                    platformServiceInstallUrlClient.requestPost(installationToolsUrl + QUICK_DEPLOY,
                            platformServiceIds, forestCookie, TIMEOUT);

            if (platformServiceInstallResponses.get().isError()) {
                log.error("一键安装平台应用失败");
            }
            for (int i = 1; i <= 20; i++) {
                //循环等待服务部署完成后进入下一步，最长10分钟
                Thread.sleep(30000);

                //部署的服务中未完成部署的服务
                List<Object> platformServerIdResult = null;
                platformServerIdResult = getStatusMismatchServiceInfo(platformServiceIds, "running");
                log.info("平台应用" + platformServerIdResult + "未部署完成，已部署30*" + i + "s");
                //log.info("basicServerIdResult" + basicServerIdResult.toString());
                //log.info("basicServerId",basicServerId.toString());
                if (platformServerIdResult.isEmpty()) {
                    platformServiceInstallResult = true;
                    break;
                }
                //由于stressTestingTools启动慢，并且需要依赖其他服务所以特殊处理；这里获取stressTestingTools的id
                //获取stressTestingTools的id
                List<Object> stressTestingToolsServerId = new ArrayList<>();
                stressTestingToolsServerId = getSpecifyNameServerIds("service","stressTestingTools");
                //如果只剩下一个服务并且是stressTestingTools，则重新部署该服务
                if(ObjectUtils.isNotEmpty(platformServerIdResult) && platformServerIdResult.size() == 1 && platformServerIdResult.get(0).equals(
                        stressTestingToolsServerId.get(0))) {
                    CommonHttpClient stressTestingToolsServiceInstallUrlClient = new CommonHttpClient();
                    AtomicReference<ForestResponse> stressTestingToolsServiceInstallResponses =
                            stressTestingToolsServiceInstallUrlClient.requestPost(installationToolsUrl + APP_DEPLOY,
                                    platformServerIdResult, forestCookie, TIMEOUT);
                    if (platformServiceInstallResponses.get().isError()) {
                        log.error("重新部署stressTestingTools失败");
                    }
                    log.info("只有stressTestingTools长时间未启动，重新部署stressTestingTools");
                    for (int k = 1; k <= 20; k++) {
                        Thread.sleep(30000);
                        if(ObjectUtils.isEmpty(getStatusMismatchServiceInfo(stressTestingToolsServerId, "running")) ){
                            break;
                        }
                    }
                }
            }

            if (platformServiceInstallResult) {
                CommonHttpClient platformServiceInstallFinishUrlClient = new CommonHttpClient();
                AtomicReference<ForestResponse> platformServerFinishResponses =
                        platformServiceInstallFinishUrlClient.requestPost(installationToolsUrl + PLATFORM_SERVICE_INSTALL,
                                (Map<String, Object>) null, forestCookie, TIMEOUT);
                log.info("平台应用安装完成，进入到日常运维阶段");
                if (platformServerFinishResponses.get().isError()) {
                    platformServiceInstallResult = false;
                    log.error("一键安装平台应用失败");
                }
            }
            return platformServiceInstallResult;

        } catch (TestCaseException e) {
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //到devOpsTools生成license文件,获取license 文件
    public File productLicenseGenerate(String licenseMachine, String filePath) {

        String devOpsToolsUrl = "http://devOpsTools.baili-inc.com";
        String devOpsToolsLoginUri = "/api/auth/login";
        String devOpsToolsLicenseUri = "/api/customer/env/license/test/add";
        String devOpsToolsLicenseDetails = "/api/customer/env/details";
        //devOpsTools只提供了文件流，要自己上传
        String devOpsToolsLicenseDownloadUrl = devOpsToolsUrl + "/api/customer/env/license/download?id=";
        CommonHttpClient devOpsToolsLoginUrlClient = new CommonHttpClient();
        Map<String, Object> devOpsToolsLoginMessage = new HashMap<>();
        devOpsToolsLoginMessage.put("username", "test");
        devOpsToolsLoginMessage.put("password", "test");
        ForestCookie devOpsToolsForestCookie = devOpsToolsLoginUrlClient.requestCookie(devOpsToolsUrl + devOpsToolsLoginUri,
                devOpsToolsLoginMessage);
        if (ObjectUtil.isEmpty(devOpsToolsForestCookie)) {
            log.error("devOpsTools登录失败");
        }

        CommonHttpClient devOpsToolsLicenseUrlClient = new CommonHttpClient();
        Map<String, Object> devOpsToolsLicenseRequest = new HashMap<>();
        String customerEnvId = "785";
        devOpsToolsLicenseRequest.put("code", licenseMachine);
        devOpsToolsLicenseRequest.put("connectLimit", 1000);
        //提前在devOpsTools创建的客户以及环境，这个id不会变
        devOpsToolsLicenseRequest.put("customerEnvId", customerEnvId);
        devOpsToolsLicenseRequest.put("descr", "部署平台自动部署时自动生成license用");
        devOpsToolsLicenseRequest.put("type", "permanent");
        //7300 license代表永久有效
        devOpsToolsLicenseRequest.put("validTime", "7300");
        //TODO 适配devOpsTools新增的license版本，部署平台部署默认v1
        devOpsToolsLicenseRequest.put("version","v1");
        //生成license
        AtomicReference<ForestResponse> devOpsToolsLicenseUrlResponse =
                devOpsToolsLicenseUrlClient.requestPost(devOpsToolsUrl + devOpsToolsLicenseUri,
                        devOpsToolsLicenseRequest, devOpsToolsForestCookie, TIMEOUT);
        if (devOpsToolsLicenseUrlResponse.get().isError()) {
            log.error("生成license失败");
        }
        //获取license下载id
        CommonHttpClient devOpsToolsLicenseUploadUrlClient = new CommonHttpClient();
        Map<String, Object> devOpsToolsLicenseIdRequest = new HashMap<>();
        devOpsToolsLicenseIdRequest.put("id", customerEnvId);
        AtomicReference<ForestResponse> devOpsToolsLicenseUploadIdResponse =
                devOpsToolsLicenseUploadUrlClient.requestPost(devOpsToolsUrl + devOpsToolsLicenseDetails,
                        devOpsToolsLicenseIdRequest, devOpsToolsForestCookie, TIMEOUT);
        if (devOpsToolsLicenseUploadIdResponse.get().isError()) {
            log.error("获取license下载id失败");
        }
        JsonNode appInfoList = Jackson.readValue(devOpsToolsLicenseUploadIdResponse.get().getResult().toString()).get(
                "data");
        String licenseUploadId = appInfoList.get("latestLicense").get("id").toString().replaceAll("\"", "").trim();
        log.info(licenseUploadId);

        if (licenseUploadId.isEmpty()) {
            log.error("未获取到license下载id");
        }

        log.info(devOpsToolsLicenseDownloadUrl);

        File licenseFile = new File(filePath);
        //devOpsTools只提供了文件流，要自己上传，由于ForestResponse下载文件不会用，所以先用HttpResponse；后续可以考虑优化
        Map<String, String> param = new HashMap<>();
        param.put("username", "test");
        param.put("password", "test");
        HttpResponse loginResponse =
                HttpRequest.post(devOpsToolsUrl + devOpsToolsLoginUri).body(Jackson.toString(param)).execute();
        String url = devOpsToolsLicenseDownloadUrl + licenseUploadId;
        HttpResponse response = HttpRequest.get(url).cookie(loginResponse.getCookies()).execute();
        HttpUtil.downloadFile(url, FileUtil.file(filePath));

        return licenseFile;

    }

    //为了获取服务id
    public List<Object> getServerIds(String serversType) throws JsonProcessingException {
        CommonHttpClient appInfoListUrlClient = new CommonHttpClient();
        Map<String, Object> appInfoRequest = new HashMap<>();
        AtomicReference<ForestResponse> appInfoListResponses =
                appInfoListUrlClient.requestPost(installationToolsUrl + APP_INFO_LIST,
                        appInfoRequest, forestCookie, TIMEOUT);
        //符合条件的服务id列表
        List<Object> serverids = new ArrayList<>();
        if (appInfoListResponses.get().isError()) {
            log.error("获取服务安装列表失败");
        }

        JsonNode appInfoList = Jackson.readValue(appInfoListResponses.get().getResult().toString()).get(
                "data");
        for (int i = 0; i < appInfoList.size(); i++) {
            if (StringUtils.equals(appInfoList.get(i).get("tags").get(0).toString().replaceAll("\"", "").trim(), serversType)) {
                //为了兼容获取出来的处理类型，后续可以优化
                serverids.add(appInfoList.get(i).get("id").toString());
            }
        }
        return serverids;
    }

    //为了获取sql id
    public List<Object> getSqlIds() throws JsonProcessingException {
        CommonHttpClient dataFilesListUrlClient = new CommonHttpClient();
        AtomicReference<ForestResponse> dataFileListResponses =
                dataFilesListUrlClient.requestGet(installationToolsUrl + DATA_FILES,
                        forestCookie);
        if (dataFileListResponses.get().isError()) {
            log.error("获取服务安装列表失败");
        }

        JsonNode dataFileList = Jackson.readValue(dataFileListResponses.get().getResult().toString()).get(
                "data");
        List<Object> sqlIds = new ArrayList<>();
        for (int i = 0; i < dataFileList.size(); i++) {
            for (int j = 0; j < dataFileList.get(i).get("items").size(); j++) {
                log.info(dataFileList.get(i).get("items").get(j).get("id").toString().toString());
                sqlIds.add(dataFileList.get(i).get("items").get(j).get("id").toString());

            }
        }
        return sqlIds;
    }

    //为了获取未执行成功的所有sql id
    public List<Object> getNotSpecifyStatusSqlIds(String sqlExecuteStatus) throws JsonProcessingException {
        CommonHttpClient dataFilesListUrlClient = new CommonHttpClient();
        AtomicReference<ForestResponse> dataFileListResponses =
                dataFilesListUrlClient.requestGet(installationToolsUrl + DATA_FILES,
                        forestCookie);
        if (dataFileListResponses.get().isError()) {
            log.error("获取服务安装列表失败");
        }

        JsonNode dataFileList = Jackson.readValue(dataFileListResponses.get().getResult().toString()).get(
                "data");
        List<Object> failureSqlIds = new ArrayList<>();
        for (int i = 0; i < dataFileList.size(); i++) {
            for (int j = 0; j < dataFileList.get(i).get("items").size(); j++) {
                if (!StringUtils.equals(dataFileList.get(i).get("items").get(j).get("status").toString().replaceAll("\""
                        , "").trim(), sqlExecuteStatus)) {
                    failureSqlIds.add(dataFileList.get(i).get("items").get(j).get("id").toString());
                }
            }
        }

        return failureSqlIds;
    }

    //为了获取服务id
    public List<Object> getSpecifyNameServerIds(String serversType, String specifyServerName) throws JsonProcessingException {
        CommonHttpClient appInfoListUrlClient = new CommonHttpClient();
        Map<String, Object> appInfoRequest = new HashMap<>();
        AtomicReference<ForestResponse> appInfoListResponses =
                appInfoListUrlClient.requestPost(installationToolsUrl + APP_INFO_LIST,
                        appInfoRequest, forestCookie, TIMEOUT);
        //符合条件的服务id列表
        List<Object> serverids = new ArrayList<>();
        if (appInfoListResponses.get().isError()) {
            log.error("获取服务安装列表失败");
        }

        JsonNode appInfoList = Jackson.readValue(appInfoListResponses.get().getResult().toString()).get(
                "data");
        for (int i = 0; i < appInfoList.size(); i++) {
            if (StringUtils.equals(appInfoList.get(i).get("tags").get(0).toString().replaceAll("\"", "").trim(), serversType)
                    && StringUtils.equals(appInfoList.get(i).get("name").toString().replaceAll("\"", "").trim(), specifyServerName)) {
                //为了兼容获取出来的处理类型，后续可以优化
                serverids.add(appInfoList.get(i).get("id").toString());
            }
        }
        return serverids;
    }

    //为了获取服务部署状态
    public List<Object> getStatusMismatchServiceInfo(List<Object> serversIds,
                                                     String serversStatus) throws JsonProcessingException {
        CommonHttpClient appStatusInfoUrlClient = new CommonHttpClient();
        Map<String, Object> appStatusInfoRequest = new HashMap<>();
        AtomicReference<ForestResponse> appInfoListResponses =
                appStatusInfoUrlClient.requestPost(installationToolsUrl + APP_INFO_LIST,
                        appStatusInfoRequest, forestCookie, TIMEOUT);
        //符合条件的服务id列表
        //List<Object> serverids = new ArrayList<>();
        if (appInfoListResponses.get().isError()) {
            log.error("获取服务安装列表失败");
        }

        JsonNode appInfoList = Jackson.readValue(appInfoListResponses.get().getResult().toString()).get(
                "data");
        for (int i = 0; i < appInfoList.size(); i++) {
            for (int j = 0; j < serversIds.size(); j++) {
                //log.info(serversIds.toString());
                //log.info(appInfoList.get(i).get("id").toString()+"id---------");
                //log.info(serversIds.get(j).toString());
                if (StringUtils.equals(appInfoList.get(i).get("statusInfo").get("status").toString().replaceAll("\""
                                , "").trim(),
                        serversStatus)
                        && appInfoList.get(i).get("id").toString().equals(serversIds.get(j).toString())) {
                    //为了兼容获取出来的处理类型，后续可以优化
                    //serversIds.add(appInfoList.get(i).get("id").toString());
                    serversIds.remove(j);
                    //log.info(serversIds.toString()+"serversIds!!!!!!!!!!");
                }
                //如果有部署失败，则尝试重新部署,尝试部署次数跟调用处逻辑有关系
                if (StringUtils.equals(appInfoList.get(i).get("statusInfo").get("status").toString().replaceAll("\""
                                , "").trim(),
                        "deploy-failed")) {
                    CommonHttpClient appDeplouUrlClient = new CommonHttpClient();
                    List<Object> platformServiceIds = new ArrayList<>();
                    platformServiceIds.add(serversIds.get(j));
                    AtomicReference<ForestResponse> appDeployResponses =
                            appDeplouUrlClient.requestPost(installationToolsUrl + APP_DEPLOY,
                                    platformServiceIds, forestCookie, TIMEOUT);
                    log.warn("服务部署失败，重新尝试部署");



                }
            }
        }
        log.info(serversIds.toString());
        return serversIds;
    }

    //部署指定服务
    public boolean deploySpecifiedService(String serversType,String serviceName) throws JsonProcessingException {

        //需要部署的后端服务的id
        List<Object> serverId = new ArrayList<>();
        serverId = getSpecifyNameServerIds(serversType,serviceName);
        if (CollectionUtils.isEmpty(serverId)){
            log.error("需要部署的服务%s不存在",serviceName);
            return false;
        }
        CommonHttpClient basicServiceInstallUrlClient = new CommonHttpClient();
        AtomicReference<ForestResponse> appInfoListResponses =
                basicServiceInstallUrlClient.requestPost(installationToolsUrl + APP_DEPLOY,
                        serverId, forestCookie, TIMEOUT);

        if (appInfoListResponses.get().isError()) {
            log.error("部署服务%s失败",serviceName);
            return false;
        }
        return true;
    }

}
