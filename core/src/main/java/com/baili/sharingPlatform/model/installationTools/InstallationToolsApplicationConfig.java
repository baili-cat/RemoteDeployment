package com.baili.sharingPlatform.model.installationTools;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author baili
 * @date 2022年05月19日11:19 上午
 */

@Data
public class InstallationToolsApplicationConfig {


    /**
     * 部署平台应用安装路径
     */
    @NotEmpty
    @ApiModelProperty(value = "部署平台应用安装路径", example = "/data/app_data ")
    private String appInstallPath;


    /**
     * 部署平台启动脚本路径
     */
    @ApiModelProperty(value = "部署平台脚本路径", example = "/installationTools/installationTools.sh ")
    private String scriptPath = "/installationTools/installationTools.sh";





}
