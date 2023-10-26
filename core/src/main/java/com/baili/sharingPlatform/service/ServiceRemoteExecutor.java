package com.baili.sharingPlatform.service;

import com.jcraft.jsch.JSchException;
import com.baili.sharingPlatform.common.TestCaseException;
import com.baili.sharingPlatform.common.ssh.CommonCommands;
import com.baili.sharingPlatform.common.ssh.ExecuteResult;
import com.baili.sharingPlatform.common.ssh.JSchExecutor;
import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.common.ssh.chain.CommandChain;
import com.baili.sharingPlatform.common.ssh.chain.CommandContext;
import com.baili.sharingPlatform.common.ssh.chain.Slf4jCommandLogger;
import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.ServiceSubstituteParameters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import static com.baili.sharingPlatform.common.ssh.chain.FailPolicy.Interrupt;

/**
 * @author baili
 * @date 2022年05月09日5:46 下午
 */
@Slf4j
@Service
public final class ServiceRemoteExecutor extends CommonCommands {

    /**
     * 用于处理windows和linux远程访问时路径的问题
     * @author baili
     * @date 2022/7/14 11:27 上午
     * @param backslashEnabled ：是否需要转换成反斜线/,并且开头带/，windows远程传文件会用
     * @return null
    */

    public String windowsAndLinuxPathConversion(String osType, Boolean backslashEnabled, String dir,Boolean withSuffix){
        if ("windows".equals(osType)) {
            //如果需要转义
            if(backslashEnabled) {
                //windows下scp拷贝文件路径需要转义，原始路径示例：C:\Users\baili\baili\auto_deplo
                // 最终要转成的路径示例：/C:/Users/baili/baili/auto_deploy/
                if(withSuffix){
                    dir = StringUtils.removeEnd(StringUtils.prependIfMissing( dir,"\\"), "\\").replace("\\","/") + "/";
                }else {
                    dir = StringUtils.removeEnd(StringUtils.prependIfMissing( dir,"\\"), "\\").replace("\\","/");
                }

            }else {
                if(withSuffix){
                    // windows连接后powershell执行命令路径C:\Users\baili\baili\auto_deplo\
                    dir = StringUtils.removeEnd(dir.replace("/","\\") ,"\\") + "\\";
                }else {
                    // windows连接后powershell执行命令路径C:\Users\baili\baili\auto_deplo\
                    dir = StringUtils.removeEnd(dir.replace("/","\\") ,"\\");
                }

            }

        } else {
            if(withSuffix){
                dir = StringUtils.removeEnd(dir, "/") + "/";
            }else {
                dir = StringUtils.removeEnd(dir, "/");
            }

        }
        return dir;
    }

    /**
     * 远程wget下载文件并解压到指定目录
     */

    public boolean wgetArchiveFileUpload(ServerChainConfig serverChainConfig, WgetConfig wgetConfig) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            serverChainConfig.setLogger(new Slf4jCommandLogger(log));
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            if("Aix".equals(serverChainConfig.getServerConfig().getOsType())){
                chain.add(CommonCommands.wgetArchiveFileUploadCommandsAix(wgetConfig.getProbePackageUrl(),
                        wgetConfig.getInstallDir(),wgetConfig.getIsDecompress()));
            } else {
                chain.add(CommonCommands.wgetArchiveFileUploadCommands(wgetConfig.getProbePackageUrl(),
                        wgetConfig.getInstallDir(),wgetConfig.getIsDecompress()));
            }
            return chain.invoke();

        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 替换指定目录下的文件整行内容
     */
    public boolean replaceFileContent(ServerChainConfig serverChainConfig,
                                      ServiceSubstituteParameters serviceSubstituteParameters) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            serverChainConfig.setLogger(new Slf4jCommandLogger(log));
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            chain.add(CommonCommands.buildReplaceFileContentCommand(serviceSubstituteParameters.getReplaceKeyValue(),
                    serviceSubstituteParameters.getFilelPath()));

            return chain.invoke();
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 替换指定目录下的文件{}内特定值的内容，目前用来替换yml文件
     */
    public boolean replaceDockerComposeContent(ServerChainConfig serverChainConfig,
                                      ServiceSubstituteParameters serviceSubstituteParameters) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            serverChainConfig.setLogger(new Slf4jCommandLogger(log));
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            chain.add(CommonCommands.buildReplaceDockerCopmposeContentCommand(serviceSubstituteParameters.getReplaceKeyValue(),
                    serviceSubstituteParameters.getFilelPath()));

            return chain.invoke();
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 只替换指定目录下单行匹配到的特定值的内容，目前用来替换部署平台模板配置内容
     */
    public boolean replaceSpecifiedContentCommand(ServerChainConfig serverChainConfig,
                                               ServiceSubstituteParameters serviceSubstituteParameters) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            serverChainConfig.setLogger(new Slf4jCommandLogger(log));
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            chain.add(CommonCommands.buildReplaceSpecifiedContentCommand(serviceSubstituteParameters.getReplaceKeyValue(),
                    serviceSubstituteParameters.getFilelPath()));

            return chain.invoke();
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 指定目录下单行匹配到的下一行添加指定内容，目前用来添加docker-compose的配置
     */
    public boolean addSpecifiedContentCommand(ServerChainConfig serverChainConfig,
                                                  ServiceSubstituteParameters serviceSubstituteParameters,
                                              List<String> replaceBasis) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            serverChainConfig.setLogger(new Slf4jCommandLogger(log));
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            chain.add(CommonCommands.buildAddSpecifiedContentCommand(
                    serviceSubstituteParameters.getReplaceKeyValue(),
                    serviceSubstituteParameters.getFilelPath(),replaceBasis));

            return chain.invoke();
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }




    /**
     * 执行命令
     */
    public boolean executeCmd(ServerConfig serverConfig,
                              String cmd) {
        ExecuteResult<String> result = null;
        try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
            CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
                executor.exec().execute(cmd);
                log.info(cmd + "执行成功");
                return true;
            //TODO windows暂时不提供支持，有需要在改
            //} else if ("windows".equals(serverConfig.getOsType())) {
            //    executor.exec().execute("cd " + dir);
            //    return true;
            //}
        } catch (JSchException e) {
            e.printStackTrace();
            return false;

        }

    }

    ///**
    // * 执行脚本
    // *TODO 执行windows脚本时有问题，所以先不使用
    // * @param script:       执行脚本的绝对路径
    // * @param serverConfig: 目标服务器的信息
    // */
    //public ExecuteResult executeScript(ServerConfig serverConfig, String script) {
    //    ExecuteResult result = null;
    //    try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
    //        CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
    //        //执行bat脚本
    //        if("windows".equals(serverConfig.getOsType())){
    //            return executor.exec().execute(".\\" + script);
    //        }
    //        else {
    //            return executor.exec().execute(script);
    //        }
    //
    //    } catch (JSchException e) {
    //        e.printStackTrace();
    //    }
    //    return result;
    //}
    /**
     * 执行脚本
     *
     * @param script:       执行脚本的绝对路径
     * @param serverConfig: 目标服务器的信息
     */
    public boolean executeScript(ServerConfig serverConfig, String script) {
        ServerChainConfig serverChainConfig = new ServerChainConfig();
        serverChainConfig.setServerConfig(serverConfig);
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            CommandChain chain = new CommandChain(executor,serverChainConfig.getLogger());
            //执行bat脚本
            if("windows".equals(serverChainConfig.getServerConfig().getOsType())){
                chain.add(CommonCommands.buildStartServiceCommandWindows(script));
            }else {
                chain.add(CommonCommands.buildStartServiceCommand(script));
            }
            return chain.invoke();

        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }



    /**
     * 拷贝文件到远程指定目录
     */
    public boolean copyFileToRemote(ServerChainConfig serverChainConfig, File file, String deistDir) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            chain.add(CommonCommands.buildFileRemoteUploadCommands(file, deistDir));
            return chain.invoke();
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 拷贝文件到远程指定目录,并解压
     */
    public boolean copyFileArchiveToRemote(ServerChainConfig serverChainConfig, File file, String deistDir) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            String deistDirWindows = windowsAndLinuxPathConversion(serverChainConfig.getServerConfig().getOsType(),
                    true,deistDir,false);
            chain.add(CommonCommands.buildFileUploadCommand("拷贝文件",file.getPath(),deistDirWindows,Interrupt,false));
            if("windows".equals(serverChainConfig.getServerConfig().getOsType())){
                chain.add(CommonCommands.buildArchiveFileDecompressionCommandsWindows(file,deistDir));
            }else if ("Solaris".equals(serverChainConfig.getServerConfig().getOsType())){
                chain.add(CommonCommands.buildArchiveFileDecompressionCommandsSolaris(file,deistDir));
            }else if("Aix".equals(serverChainConfig.getServerConfig().getOsType())){
                chain.add(CommonCommands.buildArchiveFileDecompressionCommandsAix(file,deistDir));
            }
            else {
                chain.add(CommonCommands.buildArchiveFileDecompressionCommands(file, deistDir));
            }
            return chain.invoke();
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 检查目录是否存在
     */
    public ExecuteResult directoryCheck(ServerConfig serverConfig, String dir) {
        try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
            CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
            //TODO windows时判断目录有问题
            return CommonCommands.buildDirectoryCheckCommand("检查目录", dir, true, Interrupt).invoke(context);
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 删除目录
     */
    public boolean deleteDirectory(ServerChainConfig serverChainConfig, String dir) {
        try (JSchExecutor executor = new JSchExecutor(serverChainConfig.getServerConfig())) {
            serverChainConfig.setLogger(new Slf4jCommandLogger(log));
            CommandChain chain = new CommandChain(executor, serverChainConfig.getLogger());
            if("windows".equals(serverChainConfig.getServerConfig().getOsType())){
                chain.add(CommonCommands.buildDeleteDirectoryCommandWindows(dir));
            }else {
                chain.add(CommonCommands.buildDeleteDirectoryCommand(dir));
            }
            return chain.invoke();
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    public ExecuteResult fileCheck(ServerConfig serverConfig, String fileName) {
        try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
            CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
            return CommonCommands.buildFileCheckCommand("检查文件", fileName, true, Interrupt).invoke(context);
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    /**
     * linux检查进程是否存在
     *
     * @return
     */
    public ExecuteResult<String> portCheck(ServerConfig serverConfig, String port) {
        try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
            CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
                return executor.exec().execute("netstat -anp | grep " + port , null, String::toString);
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }



    /**
     * 切换java版本
     * 暂时保留，通过shell脚本切换
     * TODO
     */
    public String changerVersion(ServerConfig serverConfig, String version) {
        ExecuteResult<String> result = null;
        try (JSchExecutor executor = new JSchExecutor(serverConfig)) {
            CommandContext context = new CommandContext(new Slf4jCommandLogger(log), executor);
            executor.exec().changeJavaVersion(version);
            return executor.exec().changeJavaVersion(version);
        } catch (JSchException e) {
            throw new TestCaseException(e.getMessage());
        }
    }

    //public ExecuteResult startProcessAgent(ServerConfig serverConfig) {
    //    //启动独立探针
    //
    //    return executeScript(serverConfig, script);
    //}
}
