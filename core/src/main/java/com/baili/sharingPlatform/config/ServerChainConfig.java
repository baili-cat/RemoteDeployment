package com.baili.sharingPlatform.config;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.common.ssh.chain.CommandLogger;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月16日4:41 下午
 */
@Data
public class ServerChainConfig {

    /**日志记录器*/
    @ApiModelProperty(hidden = true)
    private CommandLogger logger;
    /** 远程服务器连接配置*/
    private ServerConfig serverConfig;
    /** 服务名称*/
    @ApiModelProperty(hidden = true)
    private String        serviceName;
}
