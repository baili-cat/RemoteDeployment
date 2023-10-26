package com.baili.sharingPlatform.service;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

/**
 * @author baili
 * @date 2022年05月19日9:57 下午
 */
@Slf4j
@Service
public class ServiceDockerTemplates {
    @Resource
    private final ServiceRemoteExecutor dockerApplicationRemoteExecutor = new ServiceRemoteExecutor();


    public boolean deployDockerTemplates(ServerConfig serverConfig,
                                         String templatesUrl, String deployPath) throws FileNotFoundException {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        //判断模板是否存在
        //下载docker部署模板到目标节点,并解压
        if (!dockerApplicationRemoteExecutor.wgetArchiveFileUpload(serverChainConfig,new WgetConfig(templatesUrl,
                deployPath, true))){
            log.error("docker部署模板下载到:" + serverConfig.getHost() + "目录" + deployPath + "失败");
            return false;
        }
        log.info("docker部署模板下载到:" + serverConfig.getHost() + "目录" + deployPath + "完成");
        return true;
    }

    public boolean deleteDockerTemplates(ServerConfig serverConfig, String deployPath) throws FileNotFoundException {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        //判断模板是否存在
        //下载docker部署模板到目标节点
        if (!dockerApplicationRemoteExecutor.deleteDirectory(serverChainConfig,deployPath)){
            log.error("删除:" + serverConfig.getHost() + "目录" + deployPath + "下的文件失败");
            return false;
        }
        log.info("删除:" + serverConfig.getHost() + "目录" + deployPath + "下的文件完成");
        return true;
    }

}
