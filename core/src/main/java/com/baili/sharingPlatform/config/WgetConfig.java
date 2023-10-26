package com.baili.sharingPlatform.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author baili
 * @date 2022年05月16日4:42 下午
 */
@Data
@ApiModel(value = "wget配置")
public class WgetConfig {

    /**
     * 安装目录 示例:/home/baili/service/gateway
     */
    @ApiModelProperty(value = "安装目录", example = "/home/baili/process/")
    @NotEmpty(message = "安装目录不能为空")
    private String installDir;
    /**
     * 要部署的探针包下载地址
     */
    @ApiModelProperty(value = "要部署的探针包下载地址", example = "http://ftp.baili-inc.com/devOpsTools/delivery/resource/baili-agent/" +
            "1.3.4-RELEASE/baili-agent-1.3.4-RELEASE-processagent-x64_linux.tar.gz")
    @NotEmpty(message = "下载地址不能为空")
    private String probePackageUrl;

    /**
     * 是否解压，默认解压
     */
    @ApiModelProperty(value = "是否解压下载的包", example = "true")
    private Boolean isDecompress;

    public WgetConfig(String probePackageUrl, String installDir, boolean isDecompress) {
        this.probePackageUrl = probePackageUrl;
        this.installDir = installDir;
        this.isDecompress = isDecompress;
    }

    public WgetConfig() {
    }
}
