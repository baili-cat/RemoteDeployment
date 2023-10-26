package com.baili.sharingPlatform.service;

import com.jcraft.jsch.JSchException;
import com.baili.sharingPlatform.common.ssh.ExecuteResult;
import com.baili.sharingPlatform.common.ssh.JSchExecutor;
import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.common.ssh.chain.CommandContext;
import com.baili.sharingPlatform.common.ssh.chain.Slf4jCommandLogger;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.ServiceSubstituteParameters;
import com.baili.sharingPlatform.model.enums.ApplicationAction;
import com.baili.sharingPlatform.model.processAgent.ProcessAgentConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author baili
 * @date 2022年05月13日10:49 上午
 */

@Slf4j
@Service
public class ServiceProcessAgent {
    @Resource
    private ServiceRemoteExecutor processAgentRemoteExecutor = new ServiceRemoteExecutor();


    public boolean deployProcessAgent(ServerConfig serverConfig,
                                      WgetConfig wgetConfig, ProcessAgentConfig processAgentConfig) throws FileNotFoundException {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);

        String processInstallPath = processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                false, processAgentConfig.getInstallDir(), true);
        wgetConfig.setInstallDir(processInstallPath);

        //探针解压后的目录的绝对路径
        String processAgentPath = processInstallPath + processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                false, processAgentConfig.getProcessAgentPath(), true);

        //if (processAgentRemoteExecutor.directoryCheck(serverConfig,
        //        processAgentPath).isSuccess()) {
            //停止原有进程
            //TODO 不用判断是否执行成功,windows停止应用脚本不存在时，ssh会卡死导致删除文件失败，目前不影响
            processAgentAction(serverConfig, processAgentConfig, ApplicationAction.Stop.getValue());
            processAgentRemoteExecutor.deleteDirectory(serverChainConfig,
                    processAgentPath);
        //}
        if (processAgentRemoteExecutor.wgetArchiveFileUpload(serverChainConfig, wgetConfig)) {

            //获取独立探针包的路径
            //判断独立探针包是否下载成功
            //独立探针包
            String processAgentPackage =
                    processInstallPath + StringUtils.substringAfterLast(wgetConfig.getProbePackageUrl(), "/");
            processAgentRemoteExecutor.fileCheck(serverConfig, processAgentPackage);

            //判断是否需要添加自定义模块
            if (processAgentConfig.getModuleEnable()) {
                File moduleDemoFile = ResourceUtils.getFile(processAgentConfig.getModuleDemoPath());
                if (moduleDemoFile.exists()) {
                    //独立探针自定义模块路径
                    String processAgentModulePath =
                            processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),true,
                                    processInstallPath,false) +
                                    processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                                    false, processAgentConfig.getModulesPath(), true);
                    processAgentRemoteExecutor.copyFileToRemote(serverChainConfig,
                            moduleDemoFile, processAgentModulePath);
                } else {
                    log.error("moduleDemo不存在");
                    return false;
                }

            }
            ServiceSubstituteParameters serviceSubstituteParameters = new ServiceSubstituteParameters();
            //独立探针包配置文件觉得路径
            String processAgentConfigPath =
                    processInstallPath + processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                            false, processAgentConfig.getConfigRelativePath(), false);
            //替换独立探针包的配置文件
            serviceSubstituteParameters.setFilelPath(processAgentConfigPath);
            if (!processAgentConfig.getReplaceKeyValue().isEmpty()) {
                serviceSubstituteParameters.setReplaceKeyValue(processAgentConfig.getReplaceKeyValue());
                processAgentRemoteExecutor.replaceFileContent(serverChainConfig,
                        serviceSubstituteParameters);
            } else {
                log.info("processAgent没有需要修改的配置文件参数");
            }
            return true;
        }
        return false;
    }

    public boolean processAgentAction(ServerConfig serverConfig,
                                            ProcessAgentConfig processAgentConfig, String actionType) {

        //判断是启动还是停止应用
        String installDir;
        String scriptPath;

        installDir = processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                false, processAgentConfig.getInstallDir(), false);
        //执行脚本路径
        if (ApplicationAction.Start.getValue().equalsIgnoreCase(actionType)) {
            scriptPath = installDir + processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                    false, processAgentConfig.getStartScriptPath(), false);
                    ;
        } else if (ApplicationAction.Stop.getValue().equalsIgnoreCase(actionType)) {
            scriptPath = installDir + processAgentRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                    false, processAgentConfig.getStopScriptPath(), false);
        } else {
            scriptPath = "未知";
        }
        log.info("启动脚本路径scriptPath:" + scriptPath);
        //TODO windows判断文件存在有问题 ，暂时不判断文件是否存在
        //if (!processAgentRemoteExecutor.fileCheck(serverConfig,
        //        scriptPath).isSuccess()) {
        //    log.info("脚本不存在，请检查独立探针包是否部署成功");
        //    return processAgentRemoteExecutor.fileCheck(serverConfig,
        //            scriptPath);
        //}
        //执行启动脚本
        if(processAgentRemoteExecutor.executeScript(serverConfig, scriptPath)){
            log.info("执行脚本：{} 成功",scriptPath);
            return true;
        }else {
            log.info("执行脚本：{} 失败",scriptPath);
            return false;
        }

    }

    public ExecuteResult<String> processAgentPid(ServerConfig serverConfig, ProcessAgentConfig processAgentConfig) {
        ExecuteResult<String> result = null;
        try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
            CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
            if ( "linux".equals(serverConfig.getOsType())) {
                return executor.exec().execute("ps -ef | grep " + processAgentConfig.getProcessName() + " | grep -v grep | awk -F ' ' " +
                                "'{print$2}'", null,
                        String::toString);
            } else if ("windows".equals(serverConfig.getOsType())) {
                return executor.exec().execute("tasklist | grep " + processAgentConfig.getProcessName() + " | grep -v" +
                                " grep | awk -F ' ' " +
                                "'{print$2}'", null,
                        String::toString);
            }

        } catch (JSchException e) {
            e.printStackTrace();
        }
        return result;
    }

}
