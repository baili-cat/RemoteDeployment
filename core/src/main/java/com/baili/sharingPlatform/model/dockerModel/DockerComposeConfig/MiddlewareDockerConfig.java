package com.baili.sharingPlatform.model.dockerModel.DockerComposeConfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年09月21日5:30 PM
 */
@Data
public class MiddlewareDockerConfig extends DockerPathConfig {

    //中间件 docker默认启动模板路径
    private String middlewareDockerTemplatesUrl;
    //中间件 应用基础镜像包
    private String middlewareDockerImageUrl;

    //中间件 应用部署目录
    private String middlewareDeployPath;
    //应用名称
    private String appName;

    //中间件nodeexport下载地址
    private String nodeExportUrl;

    @ApiModelProperty(value = "中间件 docker-compose文件中需要修改的参数，用于指定环境,示例：")
    private List<Map<String, String>> dockerComposeReplaceKeyValue;
    private String actionType;
}
