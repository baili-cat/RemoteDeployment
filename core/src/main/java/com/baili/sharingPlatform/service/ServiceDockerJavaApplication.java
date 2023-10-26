package com.baili.sharingPlatform.service;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.ServiceSubstituteParameters;
import com.baili.sharingPlatform.model.dockerModel.DockerComposeConfig.JavaDockerConfig;
import com.baili.sharingPlatform.model.enums.DockerApplicationAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

/**
 * @author baili
 * @date 2022/9/22 5:55 PM
 */
@Slf4j
@Service
public class ServiceDockerJavaApplication {
    @Resource
    private final ServiceDockerTemplates javaDockerApplication = new ServiceDockerTemplates();
    @Resource
    private final ServiceRemoteExecutor javaDockerRemoteExeutor = new ServiceRemoteExecutor();


    public boolean deployDockerJavaApplication(ServerConfig serverConfig,
                                               JavaDockerConfig javaDockerConfig) throws FileNotFoundException {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        //最终应用部署目录
        String JAVAPP_DEPLOY_PATH =
                javaDockerRemoteExeutor.windowsAndLinuxPathConversion(serverConfig.getOsType()
                        , false, javaDockerConfig.getJavaDeployPath(), true) + javaDockerConfig.getAppName() + "/";

        //如果不存在则下载模板
        //TODO 可能会有问题,重复请求重复下载的问题暂时通过参数控制,默认为true，直接删除目录会导致docker 端口有问题，待解决
        if(javaDockerConfig.getJavaDockerTemplatesDownEnable()){
            //下载部署模板
            javaDockerApplication.deployDockerTemplates(serverConfig, javaDockerConfig.getJavaDockerTemplatesUrl(),
                    JAVAPP_DEPLOY_PATH);
        }
        //TODO 判断是否需要安装docker
        //修改docker-compose配置
        String JAVA_COMPOSE_CONFIG_PATH =
                JAVAPP_DEPLOY_PATH + javaDockerConfig.getDOCKER_COPMOSE_FILE();
        ServiceSubstituteParameters javaDockerSubstituteParameters = new ServiceSubstituteParameters();
        javaDockerSubstituteParameters.setFilelPath(JAVA_COMPOSE_CONFIG_PATH);
        javaDockerSubstituteParameters.setReplaceKeyValue(javaDockerConfig.getDockerComposeReplaceKeyValue());
        javaDockerRemoteExeutor.replaceDockerComposeContent(serverChainConfig, javaDockerSubstituteParameters);

        //下载基础镜像

        javaDockerRemoteExeutor.wgetArchiveFileUpload(serverChainConfig,
                new WgetConfig(javaDockerConfig.getJavaDockerImageUrl(),
                        JAVAPP_DEPLOY_PATH + javaDockerConfig.getIMAGE_PATH(), false));
        //下载应用包
        javaDockerRemoteExeutor.wgetArchiveFileUpload(serverChainConfig,
                new WgetConfig(javaDockerConfig.getJarPackageSrcUrl(),
                        JAVAPP_DEPLOY_PATH + javaDockerConfig.getJAR_PATH(), false));

        //是否下载探针包
        if(javaDockerConfig.getDeployAgentEnable()){
            javaDockerRemoteExeutor.wgetArchiveFileUpload(serverChainConfig,
                    new WgetConfig(javaDockerConfig.getJavaAgentPackageSrcUrl(),
                            JAVAPP_DEPLOY_PATH + javaDockerConfig.getCONF_PATH(), true));
        }
        //修改jar配置文件：conf/application.yml
        //最终应用配置文件目录
        String JAVAPP_CONFIG_PATH = JAVAPP_DEPLOY_PATH + javaDockerConfig.getCONF_PATH() + javaDockerConfig.getJAVA_CONF_FILE();
        ServiceSubstituteParameters javaApplicationSubstituteParameters = new ServiceSubstituteParameters();
        javaApplicationSubstituteParameters.setFilelPath(JAVAPP_CONFIG_PATH);
        javaApplicationSubstituteParameters.setReplaceKeyValue(javaDockerConfig.getJavaReplaceKeyValue());
        javaDockerRemoteExeutor.replaceFileContent(serverChainConfig, javaApplicationSubstituteParameters);
        return true;

    }

    public boolean dockerJavaAction(ServerConfig serverConfig,
                                    JavaDockerConfig javaDockerConfig) throws FileNotFoundException {


        //最终java应用部署目录
        String appPath =
                javaDockerRemoteExeutor.windowsAndLinuxPathConversion(serverConfig.getOsType()
                        , false, javaDockerConfig.getJavaDeployPath(), true) + javaDockerConfig.getAppName() + "/";
        //执行启动脚本
        //启动脚本绝对路径
        String scriptPath = appPath + javaDockerConfig.getDOCKER_SERVER_SCRIPT();
        //检查启动脚本是否存在
        //TODO windows 检查文件是否存在有问题
        if (!javaDockerRemoteExeutor.fileCheck(serverConfig, scriptPath).isSuccess()) {
            log.error("脚本不存在，请检查{}:{}是否部署成功",serverConfig.getHost(),javaDockerConfig.getAppName());
        }

        //启动应用
        log.info("开始执行：" + scriptPath);
        //执行脚本
        if(StringUtils.isEmpty( javaDockerConfig.getActionType())){
            log.info("docker状态命令不允许为空");
            return false;
        }
        if(EnumUtils.isValidEnum(DockerApplicationAction.class, javaDockerConfig.getActionType())){
            log.info("{}不在脚本支持范围内",javaDockerConfig.getActionType());
            return false;
        }

        String cmd = scriptPath.concat(" ").concat(javaDockerConfig.getActionType());

        if (javaDockerRemoteExeutor.executeScript(serverConfig, cmd)) {
            log.info("执行脚本：{} 成功", scriptPath);
            return true;
        } else {
            log.info("执行脚本：{} 失败", scriptPath);
            return false;
        }
    }

}