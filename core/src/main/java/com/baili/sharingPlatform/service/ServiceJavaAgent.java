package com.baili.sharingPlatform.service;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.ServiceSubstituteParameters;
import com.baili.sharingPlatform.model.javaAgent.JavaAgentConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author baili
 * @date 2022年05月13日11:29 上午
 */
@Slf4j
@Service
public class ServiceJavaAgent {
    @Resource
    private final ServiceRemoteExecutor javaAgentRemoteExecutor = new ServiceRemoteExecutor();

    public boolean deployJavaAgent(ServerConfig serverConfig,
                                   WgetConfig wgetConfig, JavaAgentConfig javaAgentConfig) throws FileNotFoundException {

        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        javaAgentConfig.setInstallDir(wgetConfig.getInstallDir());
        //判断javaAgent目录是否存在，存在则删除
        //探针解压后的目录的绝对路径
        String javaAgentInstallDir;
        String javaAgentPath;
        String javaAgentFileName;
        String javaAgentConfigFilePath;
        String javaAgentModulePath;
        //安装目录处理
        javaAgentInstallDir = javaAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(), false,
                wgetConfig.getInstallDir(), true);
        wgetConfig.setInstallDir(javaAgentInstallDir);
        //javaAgent目标服务器处理
        javaAgentPath = javaAgentInstallDir + javaAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(), false,
                javaAgentConfig.getJavaAgentPath(), true);
        //目标服务器javaAgent安装包绝对路径
        javaAgentFileName = javaAgentInstallDir + StringUtils.substringAfterLast(wgetConfig.getProbePackageUrl(), "/");
        //目标服务器javaAgent配置文件绝对路径
        javaAgentConfigFilePath = javaAgentInstallDir + javaAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(), false,
                javaAgentConfig.getJavaAgentPath(), true);
        //目标服务器javaAgent自定义module绝对路径
        javaAgentModulePath = javaAgentInstallDir + javaAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(), false,
                javaAgentConfig.getModulesPath(), true);

        //if (javaAgentRemoteExecutor.directoryCheck(serverConfig,
        //        javaAgentPath).isSuccess()) {
        //javaAgent不需要停止原有进程，保证mock应用重新启动加载即可
        //不用判断是否执行成功
        //直接删除javaAgent
        javaAgentRemoteExecutor.deleteDirectory(serverChainConfig, javaAgentPath);
        //}
        if (javaAgentRemoteExecutor.wgetArchiveFileUpload(serverChainConfig, wgetConfig)) {
            //判断javaAgent包是否下载成功
            javaAgentRemoteExecutor.fileCheck(serverConfig, javaAgentFileName);

            //替换探针包的配置文件
            ServiceSubstituteParameters serviceSubstituteParameters = new ServiceSubstituteParameters();

            serviceSubstituteParameters.setFilelPath(javaAgentConfigFilePath);
            if (javaAgentConfig.getModuleEnable()) {
                //判断modules目录是否存在，不存在创建

                File moduleDemoFile = ResourceUtils.getFile(javaAgentConfig.getModuleDemoPath());
                if (moduleDemoFile.exists()) {
                    //String javaAgentModulePathNew = javaAgentModulePath;
                    //if("windows".equals(serverConfig.getOsType())){
                    //   javaAgentModulePathNew =
                    //            javaAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),true,
                    //                    javaAgentModulePath,true);
                    //}
                    javaAgentRemoteExecutor.copyFileArchiveToRemote(serverChainConfig, moduleDemoFile,
                            javaAgentModulePath);
                } else {
                    log.error("moduleDemo不存在");
                    return false;
                }
            }
            if (ObjectUtils.isNotEmpty(javaAgentConfig.getReplaceKeyValue())) {
                serviceSubstituteParameters.setReplaceKeyValue(javaAgentConfig.getReplaceKeyValue());
                if (javaAgentRemoteExecutor.replaceFileContent(serverChainConfig, serviceSubstituteParameters)) {
                    log.info("javaAgent配置文件替换完成");
                } else {
                    log.info("javaAgent配置文件替换失败");
                }
            } else {
                log.info("javaAgent没有需要替换的配置文件");
            }
            return true;
        }
        return false;
    }

}
