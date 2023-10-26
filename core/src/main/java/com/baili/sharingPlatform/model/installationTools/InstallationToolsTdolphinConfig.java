package com.baili.sharingPlatform.model.installationTools;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年09月21日5:30 PM
 */
@Data
public class InstallationToolsTdolphinConfig {

    //tdolphineAgent 下载路径

    private String tdolphinAgentUrl = "http://ftp-project.baili-inc.com/baili/java/agent/tdolphin/doraemon_encryption/baili-one-agent-tdolphin-deploy-3.0.0-SNAPSHOT-ear.tar.gz";

    //中间件 应用部署目录
    private String middlewareDeployPath;
    //应用名称
    private String appName;

    private String perfMaAppCode = "default";
    private String perfMaOrgCode = "PerfMa";
    private String perfMaEnvCode = "Init";
    @ApiModelProperty(value = "中间件 docker-compose文件中需要修改的参数，用于指定环境,示例：")
    private List<Map<String, String>> agentReplaceKeyValue;
    private String actionType;

    //是否需要替换jdk镜像
    private Boolean downLoadJDKImage;
    //是否需要替换agent包版本
    private Boolean downLoadAgentPackage;
    //是否需要重启部署平台
    @NotNull
    @ApiModelProperty(value = "表示是否需要重启部署平台，如果修改java-deployment-metadata.json中的参数需要重启部署平台才生效，其他场景不需要重启部署平台")
    private Boolean restartInstallationTools;
}
