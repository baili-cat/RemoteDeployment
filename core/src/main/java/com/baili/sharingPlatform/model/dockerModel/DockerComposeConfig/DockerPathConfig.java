package com.baili.sharingPlatform.model.dockerModel.DockerComposeConfig;

import lombok.Data;

/**
 * @author baili
 * @date 2022年09月20日3:07 PM
 */
@Data
//额外部署应用的配置项
public class DockerPathConfig {

    //docker管理脚本
    protected final String DOCKER_SERVER_SCRIPT = "service.sh";

    //应用相关的脚本目录
    protected final String SCRIPT_PATH = "bin/";
    //docker-compose文件
    protected final String DOCKER_COPMOSE_FILE = "docker-compose.yml";

    //应用相关资源包目录
    protected final String JAR_PATH = "source/";
    //中间件nodeExport部署目录
    private String nodeExportDeployPath = "nodeExport/";

    //应用相关配置文件目录
    protected final String CONF_PATH = "conf/";
    //应用启动配置文件
    protected final String JAVA_CONF_FILE = "application.properties";
    //镜像目录
    protected final String IMAGE_PATH = "image/";
    //日志目录
    protected final String LOGS_PATH = "logs/";


}
