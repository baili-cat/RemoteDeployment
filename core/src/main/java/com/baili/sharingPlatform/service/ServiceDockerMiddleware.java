package com.baili.sharingPlatform.service;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.ServiceSubstituteParameters;
import com.baili.sharingPlatform.model.dockerModel.DockerComposeConfig.MiddlewareDockerConfig;
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
public class ServiceDockerMiddleware {
    @Resource
    private final ServiceDockerTemplates middlewareDockerApplication = new ServiceDockerTemplates();
    @Resource
    private final ServiceRemoteExecutor middlewareDockerRemoteExeutor = new ServiceRemoteExecutor();


    public boolean deployDockerMiddleware (ServerConfig serverConfig,
                                           MiddlewareDockerConfig middlewareDockerConfig) throws FileNotFoundException {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        //最终应用部署目录
        String appDeployPath =
                middlewareDockerRemoteExeutor.windowsAndLinuxPathConversion(serverConfig.getOsType()
                        , false, middlewareDockerConfig.getMiddlewareDeployPath(), true) + middlewareDockerConfig.getAppName() + "/";
        //如果存在则删除目录
        //TODO 可能会有问题
        if(!middlewareDockerConfig.getAppName().isEmpty()){
            middlewareDockerRemoteExeutor.deleteDirectory(serverChainConfig,appDeployPath);
        }
        //TODO 判断是否需要安装docker
        //下载部署模板
        middlewareDockerApplication.deployDockerTemplates(serverConfig, middlewareDockerConfig.getMiddlewareDockerTemplatesUrl(),
                appDeployPath);

        //修改docker-compose配置
        ServiceSubstituteParameters javaDockerSubstituteParameters = new ServiceSubstituteParameters();
        javaDockerSubstituteParameters.setFilelPath(appDeployPath);
        javaDockerSubstituteParameters.setReplaceKeyValue(middlewareDockerConfig.getDockerComposeReplaceKeyValue());
        middlewareDockerRemoteExeutor.replaceDockerComposeContent(serverChainConfig, javaDockerSubstituteParameters);

        //下载基础镜像

        middlewareDockerRemoteExeutor.wgetArchiveFileUpload(serverChainConfig,
                new WgetConfig(middlewareDockerConfig.getMiddlewareDockerImageUrl(),
                        appDeployPath + middlewareDockerConfig.getIMAGE_PATH(), false));
        //配置nodeexport
        //下载nodeexport包
        middlewareDockerRemoteExeutor.wgetArchiveFileUpload(serverChainConfig,
                new WgetConfig(middlewareDockerConfig.getNodeExportUrl(),
                appDeployPath + middlewareDockerConfig.getNodeExportDeployPath(),true));

        //TODO 下面的用不到应该可以删除
        ////修改配置文件：conf/application.yml
        ////最终应用配置文件目录
        //String appConfigPath =
        //        appDeployPath + middlewareDockerConfig.getCONF_PATH() + middlewareDockerConfig.getJAVA_CONF_FILE();
        //ServiceSubstituteParameters applicationSubstituteParameters = new ServiceSubstituteParameters();
        //applicationSubstituteParameters.setFilelPath(appConfigPath);
        //applicationSubstituteParameters.setReplaceKeyValue(middlewareDockerConfig.getDockerComposeReplaceKeyValue());
        //middlewareDockerRemoteExeutor.replaceFileContent(serverChainConfig, applicationSubstituteParameters);
        return true;

    }

    public boolean dockerMiddlewareAction(ServerConfig serverConfig,
                                          MiddlewareDockerConfig middlewareDockerConfig) throws FileNotFoundException {


        //最终java应用部署目录
        String appPath =
                middlewareDockerRemoteExeutor.windowsAndLinuxPathConversion(serverConfig.getOsType()
                        , false, middlewareDockerConfig.getMiddlewareDeployPath(), true) + middlewareDockerConfig.getAppName() + "/";
        //执行启动脚本
        //启动脚本绝对路径
        String scriptPath = appPath + middlewareDockerConfig.getDOCKER_SERVER_SCRIPT();
        //检查启动脚本是否存在
        //TODO windows 检查文件是否存在有问题
        if (!middlewareDockerRemoteExeutor.fileCheck(serverConfig, scriptPath).isSuccess()) {
            log.error("脚本不存在，请检查{}:{}是否部署成功",serverConfig.getHost(),middlewareDockerConfig.getAppName());
        }


        //执行脚本
        if(StringUtils.isEmpty( middlewareDockerConfig.getActionType())){
            log.info("docker状态命令不允许为空");
            return false;
        }
        if(EnumUtils.isValidEnum(DockerApplicationAction.class, middlewareDockerConfig.getActionType())){
            log.info("{}不在脚本支持范围内",middlewareDockerConfig.getActionType());
            return false;
        }
        String cmd = scriptPath.concat(" ").concat(middlewareDockerConfig.getActionType());
        log.info("需要执行的命令为：{}",cmd);
        if (middlewareDockerRemoteExeutor.executeScript(serverConfig, cmd)) {
            log.info("执行脚本：{} 成功", scriptPath);
            return true;
        } else {
            log.info("执行脚本：{} 失败", scriptPath);
            return false;
        }
    }


}