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
import com.baili.sharingPlatform.model.javaAgent.JavaAgentConfig;
import com.baili.sharingPlatform.model.mockServer.MockApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

/**
 * @author baili
 * @date 2022年05月19日9:57 下午
 */
@Slf4j
@Service
public class ServiceMockApplication {
    @Resource
    private final ServiceRemoteExecutor mockApplicationRemoteExecutor = new ServiceRemoteExecutor();
    @Resource
    private ServiceJavaAgent serviceJavaAgent;

    public boolean deployMockApplication(ServerConfig serverConfig,
                                         JavaAgentConfig javaAgentConfig, WgetConfig wgetConfig,
                                         MockApplicationConfig mockApplicationConfig) throws FileNotFoundException {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        //判断部署的是
        //部署javaAgent
        if (mockApplicationConfig.getDeployAgentEnable()) {
            if (serviceJavaAgent.deployJavaAgent(serverConfig, wgetConfig, javaAgentConfig)) {
                log.info("重新部署javaAgent成功");
            } else {
                log.info("重新部署javaAgent失败");
                return false;
            }
        }
        String mockApplicationInstallPath;
        mockApplicationInstallPath = mockApplicationRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(), false,
                mockApplicationConfig.getInstallDir(), true);

        //判断mock应用启动包是否存在
        //TODO  可能是目录，现在没处理后续处理
        if (ObjectUtils.isNotEmpty(mockApplicationConfig.getMockUrl())) {
            //停止原有进程
            //TODO 可能需要清除原来文件
            mockApplicationAction(serverConfig, javaAgentConfig, mockApplicationConfig, ApplicationAction.Stop.getValue());
            //为了适配linux往windows拷贝的时候路径的问题，这里转换一下
            System.out.println(mockApplicationInstallPath);
            mockApplicationRemoteExecutor.wgetArchiveFileUpload(serverChainConfig, new WgetConfig(mockApplicationConfig.getMockUrl(), mockApplicationInstallPath, true));
        } else {
            log.error("mockDemo不存在");
            return false;
        }
        //判断是否需要替换启动脚本中的参数，用于指定链接的环境
        if (mockApplicationConfig.getReplaceKeyValueEnable()) {
            //替换mock应用启动脚本中参数
            ServiceSubstituteParameters mockSubstituteParameters = new ServiceSubstituteParameters();
            //TODO mock应用的结构待优化
            String mockConfigPath;
            mockConfigPath = mockApplicationInstallPath +
                    mockApplicationRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(), false,
                            mockApplicationConfig.getMockConfigPath(), false);
            mockSubstituteParameters.setFilelPath(mockConfigPath);
            mockSubstituteParameters.setReplaceKeyValue(mockApplicationConfig.getReplaceKeyValue());
            mockApplicationRemoteExecutor.replaceFileContent(serverChainConfig, mockSubstituteParameters);
        } else {
            log.info("未修改mock应用启动参数，使用脚本中默认设置启动");
            return false;
        }
        return true;

}

    public boolean mockApplicationAction(ServerConfig serverConfig,
                                         JavaAgentConfig javaAgentConfig,
                                         MockApplicationConfig mockApplicationConfig,
                                         String actionType) {
        //判断是启动还是停止应用
        String installDir;
        String scriptPath;

        installDir = mockApplicationRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                false, mockApplicationConfig.getInstallDir(), true);
        //执行脚本路径
        if (ApplicationAction.Start.getValue().equalsIgnoreCase(actionType)) {
            scriptPath = installDir + mockApplicationRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(), false, mockApplicationConfig.getStartScriptPath(), false);

        } else if (ApplicationAction.Stop.getValue().equalsIgnoreCase(actionType)) {
            scriptPath = installDir + mockApplicationRemoteExecutor.windowsAndLinuxPathConversion(serverConfig.getOsType(),
                    false, mockApplicationConfig.getStopScriptPath(), false);

        } else {
            scriptPath = "未知";
        }
        //检查mock脚本是否存在
        //TODO windows 检查文件是否存在有问题
        if (!mockApplicationRemoteExecutor.fileCheck(serverConfig, scriptPath).isSuccess()) {
            log.error("脚本不存在，请检查mock应用包是否部署成功");
        }


        //检查javaagent是否存在

        if (mockApplicationConfig.getMountAgentEnable()) {
            if (!mockApplicationRemoteExecutor.fileCheck(serverConfig,
                    installDir +
                            mockApplicationConfig.getAgentBootstrapRelativePath()).isSuccess()) {
                log.error("javaagent不存在，请检查mock应用包是否挂载探针，只应用脚本");
            }

        }
        //启动应用
        log.info("开始执行：" + scriptPath);
        //执行启动脚本
        if (mockApplicationRemoteExecutor.executeScript(serverConfig, scriptPath)) {
            log.info("执行脚本：{} 成功", scriptPath);
            return true;
        } else {
            log.info("执行脚本：{} 失败", scriptPath);
            return false;
        }

    }

    public ExecuteResult<String> mockApplicationPid(ServerConfig serverConfig,
                                                    MockApplicationConfig mockApplicationConfig) {
        ExecuteResult<String> result = null;
        try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
            CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
            if ("linux".equals(serverConfig.getOsType())) {
                return executor.exec().execute("ps -ef | grep " + mockApplicationConfig.getPackageName() + " | grep -v grep | " +
                                "awk" +
                                " -F ' ' " +
                                "'{print$2}'", null,
                        String::toString);
            } else if ("windows".equals(serverConfig.getOsType())) {
                return executor.exec().execute("tasklist | grep " + mockApplicationConfig.getPackageName() + " | grep -v" +
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
