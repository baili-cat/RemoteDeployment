package com.baili.sharingPlatform.model.dockerModel.DockerInitConfig;

import lombok.Data;

/**
 * @author baili
 * @date 2022年11月14日13:49
 */
@Data
public class DockerInitConfig {
    //docker自身安装包的安装脚本
    private final String DOCKER_INIT_PATH = "docker-installer/";
    //docker安装脚本名称
    private final String DOCKER_INIT_SCRIPT_NAME = "install.sh";
    //docker安装时配置文件
    private final String DOCKER_INIT_SCRIPT_CONF = "docker.conf";
}
