package com.baili.sharingPlatform.web.controller.DockerDeployController;

import com.baili.sharingPlatform.api.web.ResponseEntity;
import com.baili.sharingPlatform.api.web.controller.RemoteDeploymentController;
import com.baili.sharingPlatform.api.web.requestModel.docker.DeployJavaDocker;
import com.baili.sharingPlatform.service.ServiceDockerJavaApplication;
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
 * @date 2022年09月24日22:40
 */
@Api(tags = "TestOps平台java应用docker部署接口")
@Slf4j
@Validated
@RestController
@RequestMapping("/dockerDeploy")
public class DockerJavaController {
    Logger logger = LoggerFactory.getLogger(RemoteDeploymentController.class);

    @Resource
    private ServiceDockerJavaApplication serviceDockerJavaApplication;

    @ApiOperation(value = "java应用docker容器安装")
    @PostMapping("/java")
    public ResponseEntity javaDeploy(@Valid @RequestBody DeployJavaDocker deployJavaDocker) throws InterruptedException {
        try {
            serviceDockerJavaApplication.deployDockerJavaApplication(deployJavaDocker.getServerConfig(),
                    deployJavaDocker.getJavaDockerConfig());
            return  ResponseEntity.ok("java应用容器化部署成功");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @ApiOperation(value = "java应用docker容器状态维护")
    @PostMapping("/javaStateChanges")
    public ResponseEntity javaStateChanges(@Valid @RequestBody DeployJavaDocker deployJavaDocker) {
        try{
            serviceDockerJavaApplication.dockerJavaAction(deployJavaDocker.getServerConfig(),
                    deployJavaDocker.getJavaDockerConfig());
            return ResponseEntity.ok(String.format("变更容器状态为%s成功",deployJavaDocker.getJavaDockerConfig().getActionType()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
