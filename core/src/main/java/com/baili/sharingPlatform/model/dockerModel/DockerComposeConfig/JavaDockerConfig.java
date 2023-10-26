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
public class JavaDockerConfig extends DockerPathConfig {

    //java应用docker默认启动模板路径
    private String javaDockerTemplatesUrl;
    private Boolean javaDockerTemplatesDownEnable = true;
    //java应用基础镜像包
    private String javaDockerImageUrl;

    //java应用部署目录
    private String javaDeployPath;
    //应用名称
    private String appName;
    ////jar包应用启动脚本
    //private String startScriptName;
    //要部署的jar包应用启动包下载地址
    private String jarPackageSrcUrl;
    //是否挂载javaAgent探针
    private Boolean deployAgentEnable = true;
    //要部署的javaAgent包
    private String javaAgentPackageSrcUrl;
    //jar应用配置文件
    private List<Map<String, String>> javaReplaceKeyValue;
    ////jar包启动应用jdk镜像地址
    //private String javaImageUrl;
    ////镜像名称
    //private String javaImageName;

    ////jar应用启动端口
    //private String port;
    ////jar应用启动后docker端口
    //private String dockerPort;
    ////jar应用启动命令
    //private String commands;
    /**
     * java应用docker-compose文件中需要替换的参数
     */
    @ApiModelProperty(value = "java应用docker-compose文件中需要修改的参数，用于指定环境,示例：")
    private List<Map<String, String>> dockerComposeReplaceKeyValue;
    private String actionType;
}
