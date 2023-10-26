package com.baili.sharingPlatform.web.controller;

import com.jcraft.jsch.JSchException;
import com.baili.sharingPlatform.api.web.ResponseCode;
import com.baili.sharingPlatform.api.web.ResponseEntity;
import com.baili.sharingPlatform.api.web.requestModel.*;
import com.baili.sharingPlatform.common.TestCaseException;
import com.baili.sharingPlatform.model.enums.ApplicationAction;
import com.baili.sharingPlatform.service.ServiceJavaAgent;
import com.baili.sharingPlatform.service.ServiceMockApplication;
import com.baili.sharingPlatform.service.ServiceProcessAgent;
import com.baili.sharingPlatform.service.ServiceRemoteExecutor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.util.Arrays;

/**
 * @author baili
 * @date 2022年04月21日11:15 下午
 */

@Api(tags = "TestOps平台探针部署接口")
@Slf4j
@Validated
@RestController
@RequestMapping("/deploy")
public class RemoteDeploymentController {
    Logger logger = LoggerFactory.getLogger(RemoteDeploymentController.class);

    @Resource
    private ServiceRemoteExecutor serviceRemoteExecutor;


    @Resource
    private ServiceProcessAgent serviceProcessAgent;

    @Resource
    private ServiceJavaAgent serviceJavaAgent;

    @Resource
    private ServiceMockApplication serviceMockApplication;

    /**
     * @author baili
     * @date 2022/5/09 5:37 下午
     * @Description: 远程wget下载文件并解压到指定目录
     */
    @ApiOperation(value = "远程wget下载文件并解压到指定目录")
    @PostMapping("/wgetArchiveFileUpload")
    public ResponseEntity wgetArchiveFileUpload(@Valid @RequestBody WgetArchiveFileUpload wgetArchiveFileUpload) {

        if (serviceRemoteExecutor.wgetArchiveFileUpload(wgetArchiveFileUpload.getServerChainConfig(),
                wgetArchiveFileUpload.getWgetConfig())) {
            return ResponseEntity.ok("wget下载文件并解压到服务器：" + wgetArchiveFileUpload.getServerChainConfig().getServerConfig().getHost() +
                    "的目录" + wgetArchiveFileUpload.getWgetConfig().getInstallDir() + "成功");
        } else {
            return ResponseEntity.fail("wget下载文件，请检查服务器或下载源是否有效");
        }

    }

    @ApiOperation(value = "检查服务器指定文件是否存在")
    @PostMapping("/checkFileExist")
    public ResponseEntity checkFileExist(@Valid @RequestBody CheckFileExist checkFileExist) {
        if (serviceRemoteExecutor.fileCheck(checkFileExist.getServerConfig(), checkFileExist.getFileName()).isSuccess()) {
            return ResponseEntity.ok("文件存在");
        } else {
            return ResponseEntity.fail("文件不存在");
        }
    }

    @ApiOperation(value = "检查服务器指定目录是否存在")
    @PostMapping("/checkDirExist")
    public ResponseEntity checkDirExist(@Valid @RequestBody CheckDirExist checkDirExist) {
        if (serviceRemoteExecutor.directoryCheck(checkDirExist.getServerConfig(), checkDirExist.getDirPath()).isSuccess()) {
            return ResponseEntity.ok("目录存在");
        } else {
            return ResponseEntity.fail("目录不存在");
        }
    }

    @ApiOperation(value = "到指定服务器执行脚本")
    @PostMapping("/scriptExecute")
    public ResponseEntity scriptExecute(@Valid @RequestBody ScriptExecute scriptExecute){
        if (serviceRemoteExecutor.executeScript(scriptExecute.getServerConfig(), scriptExecute.getScript())) {
            return ResponseEntity.ok("执行成功");
        } else {
            return ResponseEntity.fail("执行失败");
        }
    }
    //
    //@ApiOperation(value = "检查进程是否存在")
    //@PostMapping("/checkProcessExist")
    //public ResponseEntity checkProcessExist(@Valid @RequestBody CheckProcessExist checkProcessExist) {
    //    if (serviceRemoteExecutor.processCheck(checkProcessExist.getServerConfig(),
    //            checkProcessExist.getProcessName()).isSuccess()) {
    //        String[] pid = serviceRemoteExecutor.processCheck(checkProcessExist.getServerConfig(),
    //                checkProcessExist.getProcessName()).getResult().split("\n");
    //
    //        return ResponseEntity.build(ResponseCode.OK.getCode(), "进程存在", Arrays.toString(pid));
    //    } else {
    //        return ResponseEntity.fail("进程不存在");
    //    }
    //}

    @ApiOperation(value = "切换java版本")
    @PostMapping("/changeJavaVersion")
    public ResponseEntity changeJavaVersion(@Valid @RequestBody UpdateJavaVersion updateJavaVersion)  {
        try {
            serviceRemoteExecutor.changerVersion(updateJavaVersion.getServerConfig(), updateJavaVersion.getJavaVersion().getKey());
            return ResponseEntity.ok("切换成功,当前版本为" + updateJavaVersion.getJavaVersion().getValue());
        } catch (TestCaseException e) {
            e.printStackTrace();
            return ResponseEntity.fail("切换失败，%s" + e.getMessage());
        }
    }

    @ApiOperation(value = "部署独立探针")
    @PostMapping("/processAgent")
    public ResponseEntity processAgent(@Valid @RequestBody DeployPorcessAgent deployPorcessAgent) throws JSchException {
        //下载并解压目录成功,如果配置了moduleDemo则会安装module
        try {
            serviceProcessAgent.deployProcessAgent(deployPorcessAgent.getServerConfig(),
                    deployPorcessAgent.getWgetConfig(), deployPorcessAgent.getProcessAgentConfig());
            //默认启动
            serviceProcessAgent.processAgentAction(deployPorcessAgent.getServerConfig(),
                    deployPorcessAgent.getProcessAgentConfig(), ApplicationAction.Start.getValue());
            return ResponseEntity.ok("部署成功");
        } catch (TestCaseException | FileNotFoundException e) {
            return ResponseEntity.fail("部署探针失败，错误原因：" + e.getMessage());
        }
    }

    @ApiOperation(value = "独立探针启动管理")
    @PostMapping("/processAgentStatus")
    public ResponseEntity processAgentStatus(@Valid @RequestBody ChangeProcessAgentStauts changeProcessAgentStauts)  {

        try {
            String[] pid;
            if (serviceProcessAgent.processAgentAction(changeProcessAgentStauts.getServerConfig(),
                    changeProcessAgentStauts.getProcessAgentConfig(), changeProcessAgentStauts.getActionType()
            )) {
                pid = serviceProcessAgent.processAgentPid(changeProcessAgentStauts.getServerConfig(),
                        changeProcessAgentStauts.getProcessAgentConfig()).getResult().split("\n");
                if (ApplicationAction.Start.getValue().equals(changeProcessAgentStauts.getActionType()) && pid.length > 0) {
                    return ResponseEntity.build(ResponseCode.OK.getCode(), "独立探针已启动", Arrays.toString(pid));
                }
                if (ApplicationAction.Stop.getValue().equals(changeProcessAgentStauts.getActionType()) && pid.length == 0) {
                    return ResponseEntity.ok("停止独立进程成功，已不存在独立探针进程");
                }
            }
            return ResponseEntity.fail("%s独立探针运行状态失败" + changeProcessAgentStauts.getActionType());
        } catch (TestCaseException e) {
            return ResponseEntity.fail("执行脚本失败，错误原因：" + e.getMessage());
        }
    }

    @ApiOperation(value = "部署javaAgent")
    @PostMapping("/javaAgent")
    public ResponseEntity javaAgent(@Valid @RequestBody DeployJavaAgent deployJavaAgent) {
        //下载并解压目录成功
        try {
            if (serviceJavaAgent.deployJavaAgent(deployJavaAgent.getServerConfig(),
                    deployJavaAgent.getWgetConfig(), deployJavaAgent.getJavaAgentConfig())) {
                return ResponseEntity.ok("部署成功");
            } else {
                return ResponseEntity.fail("部署失败");
            }
        } catch (TestCaseException | FileNotFoundException  e) {
            return ResponseEntity.fail("部署javaAgent失败，错误原因：" + e.getMessage());
        }
    }

    @ApiOperation(value = "部署应用并启动应用")
    @PostMapping("/mockApplicationMountAgent")
    public ResponseEntity mockApplicationMountAgent(@Valid @RequestBody DeployMockApplicationMountJavaAgent deployMockApplicationMountJavaAgent) throws JSchException {
        //下载并解压目录成功
        try {
            //部署应用
            serviceMockApplication.deployMockApplication(deployMockApplicationMountJavaAgent.getServerConfig(),
                    deployMockApplicationMountJavaAgent.getJavaAgentConfig(),
                    deployMockApplicationMountJavaAgent.getWgetConfig(),
                    deployMockApplicationMountJavaAgent.getMockApplicationConfig());
            //启动应用
            try {
                serviceMockApplication.mockApplicationAction(deployMockApplicationMountJavaAgent.getServerConfig(),
                        deployMockApplicationMountJavaAgent.getJavaAgentConfig(),
                        deployMockApplicationMountJavaAgent.getMockApplicationConfig(),
                        deployMockApplicationMountJavaAgent.getActionType());
                return ResponseEntity.ok("应用部署并启动成功");

            } catch (TestCaseException e) {
                return ResponseEntity.fail("启动应用失败，错误原因：" + e.getMessage());

            }
        } catch (TestCaseException | FileNotFoundException  e) {
            return ResponseEntity.fail("部署javaAgent失败，错误原因：" + e.getMessage());
        }
    }

    //
    //@ApiOperation(value = "test")
    //@PostMapping("/test")
    //public void test(@Valid @RequestBody ServerConfig serverConfig) throws JSchException {
    //    //ServiceCommonConfig serviceCommonConfig = new ServiceCommonConfig();
    //    //
    //    //serviceCommonConfig.setLogger(new Slf4jCommandLogger(log));
    //    //serviceCommonConfig.setServerConfig(serverConfig);
    //    //serviceCommonConfig.setServiceName("process-agent");
    //    ////配置目标服务器部署目录
    //    //serviceCommonConfig.setInstallDir("/home/baili/baili/");
    //    //////配置devOpsTools安装包地址
    //    //serviceCommonConfig.setProbePackageUrl("http://ftp.baili-inc.com/devOpsTools/delivery/resource/baili-agent/1.3.4-SNAPSHOT/baili-agent-1.3.4-SNAPSHOT-processagent-x64_linux.tar.gz");
    //    //////配置xcenter地址
    //    //serviceCommonConfig.setXcenterAddress("10.10.16.207:port");
    //    JSchExecutor jSchExecutor = new JSchExecutor(serverConfig);
    //
    //    System.out.println(jSchExecutor.exec().execute("ls -l").getResult());
    //}
}
