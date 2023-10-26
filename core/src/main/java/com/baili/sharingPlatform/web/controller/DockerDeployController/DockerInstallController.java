package com.baili.sharingPlatform.web.controller.DockerDeployController;

import com.baili.sharingPlatform.api.web.ResponseEntity;
import com.baili.sharingPlatform.api.web.requestModel.docker.InstallDocker;
import com.baili.sharingPlatform.common.TestCaseException;
import com.baili.sharingPlatform.service.ServiceDockerInstall;
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

/**
 * @author baili
 * @date 2022年11月14日09:56
 */
@Api(tags = "TestOps平台docker安装接口")
@Slf4j
@Validated
@RestController
@RequestMapping("/dockerInstall")
public class DockerInstallController {
    Logger logger = LoggerFactory.getLogger(DockerInstallController.class);

    @Resource
    private ServiceDockerInstall serviceDockerInstall;

    @ApiOperation(value = "服务器docker初始安装")
    @PostMapping("/init")
    public ResponseEntity init(@Valid @RequestBody InstallDocker installDocker) throws InterruptedException {
        try {
            if (!serviceDockerInstall.deployDockerInstallPackage(installDocker.getServerConfig(),
                    installDocker.getDockerInstallConfig())) {
                return ResponseEntity.fail("下载docker安装初始化文件失败");
            }

            if (!serviceDockerInstall.installDockerAction(installDocker.getServerConfig(),
                    installDocker.getDockerInstallConfig())) {
                return ResponseEntity.fail("安装docker失败");
            }
            return ResponseEntity.ok("服务器安装docker成功");
        } catch (FileNotFoundException e ) {
            throw new RuntimeException(e);
        } catch (TestCaseException e){
            throw e;
        }
    }

    @ApiOperation(value = "安装docker或者卸载docker：install,clean")
    @PostMapping("/dockerStateChanges")
    public ResponseEntity dockerStateChanges(@Valid @RequestBody InstallDocker installDocker) throws InterruptedException {
        if (!serviceDockerInstall.installDockerAction(installDocker.getServerConfig(),
                installDocker.getDockerInstallConfig())) {
            return ResponseEntity.fail("操作docker命令：" + installDocker.getDockerInstallConfig().getAction() + "失败");
        }
        return ResponseEntity.ok("操作docker命令：" + installDocker.getDockerInstallConfig().getAction() + "成功");
    }
}
