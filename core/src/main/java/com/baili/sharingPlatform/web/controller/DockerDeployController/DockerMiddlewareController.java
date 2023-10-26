package com.baili.sharingPlatform.web.controller.DockerDeployController;

import com.baili.sharingPlatform.api.web.ResponseEntity;
import com.baili.sharingPlatform.api.web.controller.RemoteDeploymentController;
import com.baili.sharingPlatform.api.web.requestModel.docker.DeployMiddlewareDocker;
import com.baili.sharingPlatform.service.ServiceDockerMiddleware;
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
 * @date 2022年11月09日09:42
 */

@Api(tags = "TestOps平台中间件应用docker部署接口")
@Slf4j
@Validated
@RestController
@RequestMapping("/dockerDeploy")
public class DockerMiddlewareController {
    Logger logger = LoggerFactory.getLogger(RemoteDeploymentController.class);

    @Resource
    private ServiceDockerMiddleware serviceDockerMiddleware;

    @ApiOperation(value = "中间件应用docker容器安装")
    @PostMapping("/middleware")
    public ResponseEntity mysqlDeploy(@Valid @RequestBody DeployMiddlewareDocker deployMiddlewareDocker) throws InterruptedException {
        try {
            serviceDockerMiddleware.deployDockerMiddleware(deployMiddlewareDocker.getServerConfig(),
                    deployMiddlewareDocker.getMiddlewareDockerConfig());
            return ResponseEntity.ok(deployMiddlewareDocker.getMiddlewareDockerConfig().getAppName() + "应用容器化部署成功");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "中间件应用docker容器状态维护")
    @PostMapping("/middlewareStateChanges")
    public ResponseEntity mysqlStateChanges(@Valid @RequestBody DeployMiddlewareDocker deployMiddlewareDocker) {
        try {
            serviceDockerMiddleware.dockerMiddlewareAction(deployMiddlewareDocker.getServerConfig(),
                    deployMiddlewareDocker.getMiddlewareDockerConfig());
            log.info(deployMiddlewareDocker.getMiddlewareDockerConfig().getActionType());
            return ResponseEntity.ok(String.format("变更容器" + deployMiddlewareDocker.getMiddlewareDockerConfig().getAppName()
                    + "状态为%s成功", deployMiddlewareDocker.getMiddlewareDockerConfig().getActionType()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
