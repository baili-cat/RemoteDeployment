package com.baili.sharingPlatform.service;

import com.baili.sharingPlatform.api.web.ResponseEntity;
import com.baili.sharingPlatform.common.TestCaseException;
import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.ServiceSubstituteParameters;
import com.baili.sharingPlatform.model.installationTools.InstallationToolsConfig;
import com.baili.sharingPlatform.model.installationTools.InstallationToolsTdolphinConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年05月13日11:29 上午
 */
@Slf4j
@Service
public class ServiceInstallationToolsDeploy {
    @Resource
    private final ServiceRemoteExecutor installationToolsDeployRemoteExecutor = new ServiceRemoteExecutor();

    public boolean deployInstallationTools(ServerConfig serverConfig,
                                  WgetConfig wgetConfig, InstallationToolsConfig installationToolsConfig) throws TestCaseException {

        //是否部署成功
        boolean res = false;
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        installationToolsConfig.setInstallDir(wgetConfig.getInstallDir());
        try {
            //下载部署平台包到指定路径,并解压
            wgetInstallationTools(serverChainConfig, wgetConfig);
            //如果需要修改配置
            if (installationToolsConfig.getReplaceKeyValueEnable()) {
                res = false;
                //替换配置
                ServiceSubstituteParameters serviceSubstituteParameters = new ServiceSubstituteParameters();
                serviceSubstituteParameters.setFilelPath(installationToolsConfig.getInstallationToolsConfigPath());
                serviceSubstituteParameters.setReplaceKeyValue(installationToolsConfig.getReplaceKeyValue());
                if (replaceInstallationToolsKeyValue(serverChainConfig, serviceSubstituteParameters)) {
                    res = true;
                }
            }
            //启动部署平台
            res = false;
            if (installInstallationTools(serverConfig, installationToolsConfig)) {
                res = true;
            }
            //拷贝产品集到部署平台
            if (installationToolsConfig.isWgetProductSetEnable()) {
                res = false;
                log.info("开始下载产品集到部署平台");
                if (wgetProduct(serverChainConfig, installationToolsConfig)) {
                    log.info("下载产品集成功");
                    res = true;
                }
            }
            //自定义超时时间防止服务器差部署平台没部署起来
            if (StringUtils.isNotEmpty(installationToolsConfig.getTimeout())) {
                Thread.sleep(Long.parseLong(installationToolsConfig.getTimeout()));
            }
            return res;
        } catch (TestCaseException e) {
            e.printStackTrace();
            return res;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean wgetInstallationTools(ServerChainConfig serverChainConfig, WgetConfig wgetConfig) throws TestCaseException {

        try {
            installationToolsDeployRemoteExecutor.wgetArchiveFileUpload(serverChainConfig, wgetConfig);
            return true;
        } catch (TestCaseException e) {
            ResponseEntity.fail("下载部署平台包失败，错误原因：" + e.getMessage());
            return false;
        }
    }

    public boolean replaceInstallationToolsKeyValue(ServerChainConfig serverChainConfig, ServiceSubstituteParameters serviceSubstituteParameters) {
        try {
            installationToolsDeployRemoteExecutor.replaceFileContent(serverChainConfig, serviceSubstituteParameters);
            return true;
        } catch (TestCaseException e) {
            ResponseEntity.fail("替换部署平台配置失败，错误原因：" + e.getMessage());
            return false;
        }

    }

    public boolean installInstallationTools(ServerConfig serverConfig, InstallationToolsConfig installationToolsConfig) {
        try {
            //TODO 目前执行不进入到部署平台目录按照部署平台会失败，此处为了兼容，所以先进入到部署平台目录：/data/installationTools
            //部署平台启动命令
            String installationToolsStartCmd =
                    String.format("cd %s/installationTools/;sh installationTools.sh install",
                            StringUtils.removeEnd(installationToolsConfig.getInstallDir(), "/"));
            log.info("执行启动部署平台命令：" + installationToolsStartCmd + "中");
            if (installationToolsDeployRemoteExecutor.executeCmd(serverConfig,
                    installationToolsStartCmd)) {
                log.info("执行启动部署平台命令：" + installationToolsStartCmd + "成功");
                if (installedInstallationTools(serverConfig, installationToolsConfig)) {
                    log.info("部署平台已启动成功");
                    return true;
                }
            }
            log.info("部署平台启动失败，已等待5分钟");

            return false;
            //installationToolsDeployRemoteExecutor.executeScript(serverConfig, installationToolsConfig.getScriptPath() + " install");
        } catch (
                TestCaseException e) {
            ResponseEntity.fail("安装部署平台失败，错误原因：" + e.getMessage());
            return false;
        }

    }

    public boolean uninstallInstallationTools(ServerConfig serverConfig, InstallationToolsConfig installationToolsConfig) {
        try {
            installationToolsDeployRemoteExecutor.executeScript(serverConfig, installationToolsConfig.getScriptPath() + " uninstall");
            return true;
        } catch (TestCaseException e) {
            ResponseEntity.fail("卸载部署平台失败，错误原因：" + e.getMessage());
            return false;
        }
    }

    public boolean installedInstallationTools(ServerConfig serverConfig, InstallationToolsConfig installationToolsConfig) {
        boolean installed = false;
        try {

            //查询部署平台端口结果
            String port = "8080";
            String[] portResult = installationToolsDeployRemoteExecutor.portCheck(serverConfig,
                    port).getResult().split("\n");
            log.info(portResult[0]);
            for (int time = 1; time <= 10; time++) {
                log.info("等待部署平台启动，未检测到8080端口");
                //判断端口是否存在
                //TODO 暂时默认8080端口过滤出来只有一个
                if (StringUtils.contains(portResult[0], port)) {
                    log.info("检查到部署平台端口8080已活跃，部署平台已启动");
                    //等待20s确保部署平台已启动
                    Thread.sleep(20000);
                    return true;
                }
                Thread.sleep(30000);
            }
            return false;
            //installationToolsDeployRemoteExecutor.executeScript(serverConfig, installationToolsConfig.getScriptPath() + " install");
        } catch (TestCaseException e) {
            ResponseEntity.fail("安装部署平台失败，错误原因：" + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean wgetProduct(ServerChainConfig serverChainConfig, InstallationToolsConfig installationToolsConfig) throws TestCaseException {

        try {
            WgetConfig productWgetConfig = new WgetConfig();
            productWgetConfig.setInstallDir(installationToolsConfig.getInstallDir() + installationToolsConfig.getProductsetsPath());
            productWgetConfig.setProbePackageUrl(installationToolsConfig.getProbePackageUrl());
            productWgetConfig.setIsDecompress(false);
            installationToolsDeployRemoteExecutor.wgetArchiveFileUpload(serverChainConfig, productWgetConfig);
            return true;
        } catch (TestCaseException e) {
            ResponseEntity.fail("下载产品集到部署平台失败，错误原因：" + e.getMessage());
            return false;
        }
    }

    public boolean restartInstallationTools(ServerConfig serverConfig, InstallationToolsConfig installationToolsConfig) {
        try {
            String installationToolsReStartCmd =
                    String.format("cd %s/installationTools/;sh installationTools.sh restart",
                            StringUtils.removeEnd(installationToolsConfig.getInstallDir(), "/"));
            installationToolsDeployRemoteExecutor.executeCmd(serverConfig,
                    installationToolsReStartCmd);
            return true;
        } catch (TestCaseException e) {
            ResponseEntity.fail("重启部署平台失败，错误原因：" + e.getMessage());
            return false;
        }
    }

    //TODO 临时使用先写死，有时间再优化
    public boolean mountTdolphinAgent(ServerConfig serverConfig, InstallationToolsConfig installationToolsConfig,
                                      InstallationToolsTdolphinConfig installationToolsTdolphinConfig) {
        try {
            //替换java运行环境基础镜像
            ServerChainConfig installationToolsServerChainConfig = new ServerChainConfig();
            installationToolsServerChainConfig.setServerConfig(serverConfig);
            //根据请求参数判断，是否需要重新下载jdk镜像并重新load
            if (installationToolsTdolphinConfig.getDownLoadJDKImage()) {
                //wget http://ftp.baili-inc.com/ftp/installationTools/images/baili-jdk-1.8.0-internal.tar.gz -O /tmp/baili-jdk-1.8.0-internal.tar.gz
                installationToolsDeployRemoteExecutor.wgetArchiveFileUpload(installationToolsServerChainConfig, new WgetConfig("http://ftp.baili-inc.com/ftp/installationTools/images/baili-jdk-1.8.0-internal.tar.gz", "/tmp/", false));
                //加载镜像命令
                installationToolsDeployRemoteExecutor.executeCmd(serverConfig, "docker load -i /tmp/baili-jdk-1.8.0-internal.tar.gz");
                installationToolsDeployRemoteExecutor.executeCmd(serverConfig, "docker tag docker.baili-inc.com/installationTools/baili-jdk:1.8.0-internal docker.baili-inc.com/installationTools/baili-jdk:1.8.0-alpine");
                installationToolsDeployRemoteExecutor.executeCmd(serverConfig, "docker save docker.baili-inc.com/installationTools/baili-jdk:1.8.0-internal | gzip > " + installationToolsConfig.getInstallDir()
                        + "/installationTools/share/images/baili-jdk-1.8.0-alpine.tar.gz");
            }
            if (installationToolsTdolphinConfig.getDownLoadAgentPackage()) {
                //TODO 下载探针前删除原探针,这里会把历史版本的包全部删掉
                //TODO 整体比较low随便写的，有缘人来优化，有缘人大概率是自己
                installationToolsDeployRemoteExecutor.deleteDirectory(installationToolsServerChainConfig, installationToolsConfig.getInstallDir() +
                        "/installationTools/share/templates/java" +
                        "/conf/");
                //下载探针
                installationToolsDeployRemoteExecutor.wgetArchiveFileUpload(installationToolsServerChainConfig,
                        new WgetConfig(installationToolsTdolphinConfig.getTdolphinAgentUrl(),
                                installationToolsConfig.getInstallDir() + "/installationTools/share/templates/java/conf/", true));
            }

            //替换agent链接环境参数
            ServiceSubstituteParameters serviceSubstituteParameters = new ServiceSubstituteParameters();
            serviceSubstituteParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/templates/java" +
                    "/conf/baili-java-agent/agent.config");
            serviceSubstituteParameters.setReplaceKeyValue(installationToolsTdolphinConfig.getAgentReplaceKeyValue());
            installationToolsDeployRemoteExecutor.replaceFileContent(installationToolsServerChainConfig, serviceSubstituteParameters);
            //修改部署平台中的java应用部署模板
            //修改jdk名称
            Map<String, String> javaDeploymentTemplateConfReplaceValue = new HashMap<>();
            javaDeploymentTemplateConfReplaceValue.put("'baili-jdk:1.8.0\"'", "'baili-jdk:1.8.0-internal\"'");
            Map<String, String> javaDeploymentTemplateJdkReplaceValue = new HashMap<>();
            javaDeploymentTemplateJdkReplaceValue.put("'1.8.0.tar.gz\"'", "'1.8.0-alpine.tar.gz\"'");
            List<Map<String, String>> javaDeploymetnTemplateReplaceValueList = new ArrayList<>();

            javaDeploymetnTemplateReplaceValueList.add(javaDeploymentTemplateJdkReplaceValue);
            javaDeploymetnTemplateReplaceValueList.add(javaDeploymentTemplateConfReplaceValue);

            ServiceSubstituteParameters javaDeploymentTemplateParameters = new ServiceSubstituteParameters();
            javaDeploymentTemplateParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/java-deployment-metadata.json");
            javaDeploymentTemplateParameters.setReplaceKeyValue(javaDeploymetnTemplateReplaceValueList);
            installationToolsDeployRemoteExecutor.replaceSpecifiedContentCommand(installationToolsServerChainConfig, javaDeploymentTemplateParameters);
            //添加baili-java-agent拷贝目录
            Map<String, String> javaPerfMaAgentConfAddValue = new HashMap<>();
            //空格不要删，为了适配部署平台模板中的内容
            //挂载探针目录到对应的docker内，这里的空格一定不要动！！！！！！！！！！！！！
            String perfMaAgentConfFileSets = "        {\\n          \"directory\": \"templates/java/conf\",\\n       " +
                    "   \"outputDirectory\": \"conf\",\\n          \"includes\": [\\n            \"./**\"\\n          ]\\n        },";
            javaPerfMaAgentConfAddValue.put("fileSets", perfMaAgentConfFileSets);
            List<Map<String, String>> javaPerfMaAgentConfValueList = new ArrayList<>();
            javaPerfMaAgentConfValueList.add(javaPerfMaAgentConfAddValue);

            ServiceSubstituteParameters javaPerfMaAgentConfParameters = new ServiceSubstituteParameters();
            javaPerfMaAgentConfParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/java-deployment-metadata.json");
            javaPerfMaAgentConfParameters.setReplaceKeyValue(javaPerfMaAgentConfValueList);

            List<String> javaPerfMaAgentConffilteringBasedLists = new ArrayList<>();
            javaPerfMaAgentConffilteringBasedLists.add("templates/java/conf");
            //TODO 由于需要替换的文本是多行，匹配时会导致与预期不符，所以临时参数指定匹配的项是什么
            installationToolsDeployRemoteExecutor.addSpecifiedContentCommand(installationToolsServerChainConfig,
                    javaPerfMaAgentConfParameters, javaPerfMaAgentConffilteringBasedLists);
            //修改Java应用部署docker模板
            Map<String, String> javaDockerComposeReplaceValue = new HashMap<>();
            //空格不要删，为了适配部署平台模板中的内容
            //挂载探针目录到对应的docker内
            //用来过滤文本
            String perfMaAgentVolumesFilteringBasedList = "./conf/baili-java-agent:/baili-java-agent";
            //文本替换的值
            String perfMaAgentVolumesDir = "            - ./conf/baili-java-agent:/baili-java-agent";
            javaDockerComposeReplaceValue.put("volumes:", perfMaAgentVolumesDir);
            //探针挂载后适配部署平台的javaagent参数，如果有需要可以根据参数控制appcode、envcode等
            String javaDockerComposeFilteringBasedList = "javaagent:";
            //com.baili.${(SERVICE_NAME?replace('xchaos','xhas','r'))!}可以处理freemaker中值为空报错的问题
            //
            String perfMaAgentJavaOPTS = "                    -javaagent://baili-java-agent/agent" +
                    "-bootstrap.jar -DXCENTER_DATA_PUBLISH=false -Dtdolphin.nodeType=master -Dtdolphin" +
                    //这里为了兼容xchaos项目名与包名路径不一致的问题，很恶心！！！！！！！！！！！！
                    ".packageName=com.baili.<#if SERVICE_NAME == '\\''xchaos'\\''>${SERVICE_NAME?replace('\\''xchaos'\\''," +
                    "'\\''xhas'\\''," +
                    "'\\''r'\\'')}<#else>${SERVICE_NAME}</#if> " +
                    "-DPERFMA_APP_CODE=${SERVICE_NAME}-" + installationToolsTdolphinConfig.getPerfMaAppCode() +
                    " -DPERFMA_ORG_CODE=" + installationToolsTdolphinConfig.getPerfMaOrgCode() +
                    " -DPERFMA_ENV_CODE=" + installationToolsTdolphinConfig.getPerfMaEnvCode() +
                    " -DPERFMA_XSKY_MAX_WAIT=30";
            javaDockerComposeReplaceValue.put("JAVA_OPTS:", perfMaAgentJavaOPTS);
            List<Map<String, String>> javaDockerComposeReplaceValueList = new ArrayList<>();
            javaDockerComposeReplaceValueList.add(javaDockerComposeReplaceValue);

            ServiceSubstituteParameters javaDockerComposeParameters = new ServiceSubstituteParameters();
            javaDockerComposeParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/templates/java/docker-compose.yml");
            javaDockerComposeParameters.setReplaceKeyValue(javaDockerComposeReplaceValueList);

            List<String> filteringBasedLists = new ArrayList<>();
            //这里添加的顺序要跟替换的值保持一致
            filteringBasedLists.add(perfMaAgentVolumesFilteringBasedList);
            filteringBasedLists.add(javaDockerComposeFilteringBasedList);
            installationToolsDeployRemoteExecutor.addSpecifiedContentCommand(installationToolsServerChainConfig,
                    javaDockerComposeParameters, filteringBasedLists);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //挂载单个应用的探针
    public boolean mountTdolphinAgentSingle(ServerConfig serverConfig, InstallationToolsConfig installationToolsConfig,
                                      InstallationToolsTdolphinConfig installationToolsTdolphinConfig) {
        try {
            //替换java运行环境基础镜像
            ServerChainConfig installationToolsServerChainConfig = new ServerChainConfig();
            installationToolsServerChainConfig.setServerConfig(serverConfig);
            //根据请求参数判断，是否需要重新下载jdk镜像并重新load
            if (installationToolsTdolphinConfig.getDownLoadJDKImage()) {
                //wget http://ftp.baili-inc.com/ftp/installationTools/images/baili-jdk-1.8.0-internal.tar.gz -O /tmp/baili-jdk-1.8.0-internal.tar.gz
                installationToolsDeployRemoteExecutor.wgetArchiveFileUpload(installationToolsServerChainConfig, new WgetConfig("http://ftp.baili-inc.com/ftp/installationTools/images/baili-jdk-1.8.0-internal.tar.gz", "/tmp/", false));
                //加载镜像命令
                installationToolsDeployRemoteExecutor.executeCmd(serverConfig, "docker load -i /tmp/baili-jdk-1.8.0-internal.tar.gz");
                installationToolsDeployRemoteExecutor.executeCmd(serverConfig, "docker tag docker.baili-inc.com/installationTools/baili-jdk:1.8.0-internal docker.baili-inc.com/installationTools/baili-jdk:1.8.0-alpine");
                installationToolsDeployRemoteExecutor.executeCmd(serverConfig, "docker save docker.baili-inc.com/installationTools/baili-jdk:1.8.0-internal | gzip > " + installationToolsConfig.getInstallDir()
                        + "/installationTools/share/images/baili-jdk-1.8.0-alpine.tar.gz");
            }
            if (installationToolsTdolphinConfig.getDownLoadAgentPackage()) {
                //TODO 下载探针前删除原探针,这里会把历史版本的包全部删掉
                //TODO 整体比较low随便写的，有缘人来优化，有缘人大概率是自己
                installationToolsDeployRemoteExecutor.deleteDirectory(installationToolsServerChainConfig, installationToolsConfig.getInstallDir() +
                        "/installationTools/share/templates/java" +
                        "/conf/");
                //下载探针
                installationToolsDeployRemoteExecutor.wgetArchiveFileUpload(installationToolsServerChainConfig,
                        new WgetConfig(installationToolsTdolphinConfig.getTdolphinAgentUrl(),
                                installationToolsConfig.getInstallDir() + "/installationTools/share/templates/java/conf/", true));
            }

            //替换agent链接环境参数
            ServiceSubstituteParameters serviceSubstituteParameters = new ServiceSubstituteParameters();
            serviceSubstituteParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/templates/java" +
                    "/conf/baili-java-agent/agent.config");
            serviceSubstituteParameters.setReplaceKeyValue(installationToolsTdolphinConfig.getAgentReplaceKeyValue());
            installationToolsDeployRemoteExecutor.replaceFileContent(installationToolsServerChainConfig, serviceSubstituteParameters);
            //修改部署平台中的java应用部署模板
            //修改jdk名称
            Map<String, String> javaDeploymentTemplateConfReplaceValue = new HashMap<>();
            javaDeploymentTemplateConfReplaceValue.put("'baili-jdk:1.8.0\"'", "'baili-jdk:1.8.0-internal\"'");
            Map<String, String> javaDeploymentTemplateJdkReplaceValue = new HashMap<>();
            javaDeploymentTemplateJdkReplaceValue.put("'1.8.0.tar.gz\"'", "'1.8.0-alpine.tar.gz\"'");
            List<Map<String, String>> javaDeploymetnTemplateReplaceValueList = new ArrayList<>();

            javaDeploymetnTemplateReplaceValueList.add(javaDeploymentTemplateJdkReplaceValue);
            javaDeploymetnTemplateReplaceValueList.add(javaDeploymentTemplateConfReplaceValue);

            ServiceSubstituteParameters javaDeploymentTemplateParameters = new ServiceSubstituteParameters();
            javaDeploymentTemplateParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/java-deployment-metadata.json");
            javaDeploymentTemplateParameters.setReplaceKeyValue(javaDeploymetnTemplateReplaceValueList);
            installationToolsDeployRemoteExecutor.replaceSpecifiedContentCommand(installationToolsServerChainConfig, javaDeploymentTemplateParameters);
            //添加baili-java-agent拷贝目录
            Map<String, String> javaPerfMaAgentConfAddValue = new HashMap<>();
            //空格不要删，为了适配部署平台模板中的内容
            //挂载探针目录到对应的docker内，这里的空格一定不要动！！！！！！！！！！！！！
            String perfMaAgentConfFileSets = "        {\\n          \"directory\": \"templates/java/conf\",\\n       " +
                    "   \"outputDirectory\": \"conf\",\\n          \"includes\": [\\n            \"./**\"\\n          ]\\n        },";
            javaPerfMaAgentConfAddValue.put("fileSets", perfMaAgentConfFileSets);
            List<Map<String, String>> javaPerfMaAgentConfValueList = new ArrayList<>();
            javaPerfMaAgentConfValueList.add(javaPerfMaAgentConfAddValue);

            ServiceSubstituteParameters javaPerfMaAgentConfParameters = new ServiceSubstituteParameters();
            javaPerfMaAgentConfParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/java-deployment-metadata.json");
            javaPerfMaAgentConfParameters.setReplaceKeyValue(javaPerfMaAgentConfValueList);

            List<String> javaPerfMaAgentConffilteringBasedLists = new ArrayList<>();
            javaPerfMaAgentConffilteringBasedLists.add("templates/java/conf");
            //TODO 由于需要替换的文本是多行，匹配时会导致与预期不符，所以临时参数指定匹配的项是什么
            installationToolsDeployRemoteExecutor.addSpecifiedContentCommand(installationToolsServerChainConfig,
                    javaPerfMaAgentConfParameters, javaPerfMaAgentConffilteringBasedLists);
            //修改Java应用部署docker模板
            Map<String, String> javaDockerComposeReplaceValue = new HashMap<>();
            //空格不要删，为了适配部署平台模板中的内容
            //挂载探针目录到对应的docker内
            //用来过滤文本
            String perfMaAgentVolumesFilteringBasedList = "./conf/baili-java-agent:/baili-java-agent";
            //文本替换的值
            String perfMaAgentVolumesDir = "            - ./conf/baili-java-agent:/baili-java-agent";
            javaDockerComposeReplaceValue.put("volumes:", perfMaAgentVolumesDir);
            //探针挂载后适配部署平台的javaagent参数，如果有需要可以根据参数控制appcode、envcode等
            String javaDockerComposeFilteringBasedList = "javaagent:";
            //com.baili.${(SERVICE_NAME?replace('xchaos','xhas','r'))!}可以处理freemaker中值为空报错的问题
            //
            String perfMaAgentJavaOPTS = "                    -javaagent://baili-java-agent/agent" +
                    "-bootstrap.jar -DXCENTER_DATA_PUBLISH=false -Dtdolphin.nodeType=master -Dtdolphin" +
                    //这里为了兼容xchaos项目名与包名路径不一致的问题，很恶心！！！！！！！！！！！！
                    ".packageName=com.baili.<#if SERVICE_NAME == '\\''xchaos'\\''>${SERVICE_NAME?replace('\\''xchaos'\\''," +
                    "'\\''xhas'\\''," +
                    "'\\''r'\\'')}<#else>${SERVICE_NAME}</#if> " +
                    "-DPERFMA_APP_CODE=${SERVICE_NAME}-" + installationToolsTdolphinConfig.getPerfMaAppCode() +
                    " -DPERFMA_ORG_CODE=" + installationToolsTdolphinConfig.getPerfMaOrgCode() +
                    " -DPERFMA_ENV_CODE=" + installationToolsTdolphinConfig.getPerfMaEnvCode() +
                    " -DPERFMA_XSKY_MAX_WAIT=30";
            javaDockerComposeReplaceValue.put("JAVA_OPTS:", perfMaAgentJavaOPTS);
            List<Map<String, String>> javaDockerComposeReplaceValueList = new ArrayList<>();
            javaDockerComposeReplaceValueList.add(javaDockerComposeReplaceValue);

            ServiceSubstituteParameters javaDockerComposeParameters = new ServiceSubstituteParameters();
            javaDockerComposeParameters.setFilelPath(installationToolsConfig.getInstallDir() + "/installationTools/share/templates/java/docker-compose.yml");
            javaDockerComposeParameters.setReplaceKeyValue(javaDockerComposeReplaceValueList);

            List<String> filteringBasedLists = new ArrayList<>();
            //这里添加的顺序要跟替换的值保持一致
            filteringBasedLists.add(perfMaAgentVolumesFilteringBasedList);
            filteringBasedLists.add(javaDockerComposeFilteringBasedList);
            installationToolsDeployRemoteExecutor.addSpecifiedContentCommand(installationToolsServerChainConfig,
                    javaDockerComposeParameters, filteringBasedLists);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
