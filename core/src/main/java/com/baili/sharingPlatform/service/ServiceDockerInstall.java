package com.baili.sharingPlatform.service;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.ServiceSubstituteParameters;
import com.baili.sharingPlatform.model.dockerModel.DockerInitConfig.DockerInstallConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

/**
 * @author baili
 * @date 2022年05月19日9:57 下午
 */
@Slf4j
@Service
public class ServiceDockerInstall {
    @Resource
    private final ServiceRemoteExecutor dockerApplicationRemoteExecutor = new ServiceRemoteExecutor();

    public boolean deployDockerInstallPackage(ServerConfig serverConfig,
                                              DockerInstallConfig dockerInstallConfig) throws FileNotFoundException {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        //判断模板是否存在
        //下载docker安装包到目标节点
        if (!dockerApplicationRemoteExecutor.wgetArchiveFileUpload(serverChainConfig, new WgetConfig(dockerInstallConfig.getDockerInstallPackageUrl(),
                dockerInstallConfig.getDockerInstallPath(), true))) {
            log.error("docker安装包下载到:" + serverConfig.getHost() + "目录" + dockerInstallConfig.getDockerInstallPath() + "失败");
            return false;
        }
        try {
            String dockerInitPath = dockerApplicationRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType()
                    , false, dockerInstallConfig.getDockerInstallPath(), true) + dockerInstallConfig.getDOCKER_INIT_PATH();
            //修改docker.conf配置
            ServiceSubstituteParameters dockerSubstituteParameters = new ServiceSubstituteParameters();
            dockerSubstituteParameters.setFilelPath(dockerInitPath + dockerInstallConfig.getDOCKER_INIT_SCRIPT_CONF());
            dockerSubstituteParameters.setReplaceKeyValue(dockerInstallConfig.getDockerReplaceKeyValue());
            dockerApplicationRemoteExecutor.replaceFileContent(serverChainConfig, dockerSubstituteParameters);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public boolean installDockerAction(ServerConfig serverConfig, DockerInstallConfig dockerInstallConfig) {

        //服务器docker安装脚本目录
        String dockerInitPath = dockerApplicationRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType()
                , false, dockerInstallConfig.getDockerInstallPath(), true) + dockerInstallConfig.getDOCKER_INIT_PATH();
        String installScriptPath = dockerInitPath + dockerInstallConfig.getDOCKER_INIT_SCRIPT_NAME();
        //执行启动脚本

        //检查启动脚本是否存在
        //TODO windows 检查文件是否存在有问题
        if (!dockerApplicationRemoteExecutor.fileCheck(serverConfig, installScriptPath).isSuccess()) {
            log.error("安装脚本不存在，请检查{}:{}是否存在", serverConfig.getHost(), dockerInstallConfig.getDockerInstallPath());
        }

        //执行脚本
        if (StringUtils.isEmpty(dockerInstallConfig.getAction())) {
            log.info("docker状态命令不允许为空");
            return false;
        }
        //启动脚本绝对路径
        String cmd = installScriptPath.concat(" ").concat(dockerInstallConfig.getAction());
        log.info("需要执行的命令为：{}", cmd);
        if (dockerApplicationRemoteExecutor.executeScript(serverConfig, cmd)) {
            log.info("执行脚本：{} 成功", installScriptPath);
            return true;
        } else {
            log.info("执行脚本：{} 失败", installScriptPath);
            return false;
        }
    }

}
