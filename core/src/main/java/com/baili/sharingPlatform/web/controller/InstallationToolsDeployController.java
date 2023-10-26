package com.baili.sharingPlatform.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.baili.sharingPlatform.api.web.ResponseEntity;
import com.baili.sharingPlatform.api.web.requestModel.DeployInstallationTools;
import com.baili.sharingPlatform.api.web.requestModel.DeployInstallationToolsAppMessage;
import com.baili.sharingPlatform.api.web.requestModel.DeployInstallationToolsTdolphin;
import com.baili.sharingPlatform.service.ServiceProductInstall;
import com.baili.sharingPlatform.service.ServiceInstallationToolsDeploy;
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

/**
 * @author baili
 * @date 2022年08月18日11:00 PM
 */
@Api(tags = "TestOps平台部署平台部署接口")
@Slf4j
@Validated
@RestController
@RequestMapping("/installationToolsAutoDeploy")
public class InstallationToolsDeployController {
    Logger logger = LoggerFactory.getLogger(RemoteDeploymentController.class);
    @Resource
    private ServiceInstallationToolsDeploy serviceInstallationToolsDeploy;

    @Resource
    private ServiceProductInstall serviceProductInstall;

    @ApiOperation(value = "部署部署平台到指定服务指定目录")
    @PostMapping("/InstallationToolsInstall")
    public ResponseEntity install(@Valid @RequestBody DeployInstallationTools deployInstallationTools) {

        boolean res = serviceInstallationToolsDeploy.deployInstallationTools(deployInstallationTools.getServerConfig(), deployInstallationTools.getWgetConfig(),
                deployInstallationTools.getInstallationToolsConfig());
        if (res) {
            return ResponseEntity.ok("wget下载部署平台并解压部署到服务器：" + deployInstallationTools.getServerConfig().getHost() +
                    "的目录" + deployInstallationTools.getWgetConfig().getInstallDir() + "成功");
        } else {
            return ResponseEntity.fail("wget下载部署平台部署文件，请检查服务器信息或下载源是否有效");
        }
    }

    @ApiOperation(value = "卸载指定服务部署平台")
    @PostMapping("/InstallationToolsUninstall")
    public ResponseEntity uninstall(@Valid @RequestBody DeployInstallationTools deployInstallationTools) {

        boolean res = serviceInstallationToolsDeploy.uninstallInstallationTools(deployInstallationTools.getServerConfig(),
                deployInstallationTools.getInstallationToolsConfig());
        if (res) {
            return ResponseEntity.ok("卸载服务器" + deployInstallationTools.getServerConfig().getHost() +
                    "上的部署平台成功");
        } else {
            return ResponseEntity.fail("卸载部署平台失败");
        }
    }

    @ApiOperation(value = "部署平台指定精准探针")
    @PostMapping("/InstallationToolsTdolphinDeploy")
    public ResponseEntity tdolphinDeploy(@Valid @RequestBody DeployInstallationToolsTdolphin deployInstallationToolsTdolphin) {

        boolean res = serviceInstallationToolsDeploy.mountTdolphinAgent(deployInstallationToolsTdolphin.getServerConfig(),
                deployInstallationToolsTdolphin.getInstallationToolsConfig(), deployInstallationToolsTdolphin.getInstallationToolsTdolphinConfig());
        if (res) {
            //是否需要重启部署平台
            if (deployInstallationToolsTdolphin.getInstallationToolsTdolphinConfig().getRestartInstallationTools()) {
                try {
                    serviceInstallationToolsDeploy.restartInstallationTools(deployInstallationToolsTdolphin.getServerConfig(),
                            deployInstallationToolsTdolphin.getInstallationToolsConfig());
                    log.info("开始重启部署平台");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return ResponseEntity.ok("配置tdolphin到" + deployInstallationToolsTdolphin.getServerConfig().getHost() +
                    "上的部署平台成功,已自动重启部署平台等待部署平台重启成功后重新部署应用即会挂载精准探针并上报数据");
        } else {
            return ResponseEntity.fail("配置tdolphin失败");
        }
    }

    @ApiOperation(value = "重新部署后端应用")
    @PostMapping("/InstallationToolsDeployBackendService")
    public ResponseEntity InstallationToolsDeployBackendService(@Valid @RequestBody DeployInstallationToolsAppMessage deployInstallationToolsAppMessage) {
        serviceProductInstall.initInstallationToolsUrl(deployInstallationToolsAppMessage.getInstallationToolsHostIp());
        serviceProductInstall.login(deployInstallationToolsAppMessage.getLoginMessage());
        boolean res = false;
        try {
            res = serviceProductInstall.deploySpecifiedService("service", deployInstallationToolsAppMessage.getDeployBackendName());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (res) {
            return ResponseEntity.ok("重新部署" + deployInstallationToolsAppMessage.getDeployBackendName() +
                    "接口调用成功，请去页面确认状态");
        } else {
            return ResponseEntity.fail("重新部署" + deployInstallationToolsAppMessage.getDeployBackendName() +
                    "接口调用失败，请查看部署日志检查参数填入的服务是否存在或联系管理员");
        }
    }
}
