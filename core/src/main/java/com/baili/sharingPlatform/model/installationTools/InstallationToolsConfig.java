package com.baili.sharingPlatform.model.installationTools;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年05月19日11:19 上午
 */

@Data
public class InstallationToolsConfig {


    /**
     * 部署平台安装目录
     */
    @NotEmpty
    @ApiModelProperty(value = "部署平台安装路径", example = "/data ")
    private String installDir;


    /**
     * 部署平台启动脚本路径
     */
    @ApiModelProperty(value = "部署平台脚本路径", example = "/installationTools/installationTools.sh ")
    private String scriptPath = "/installationTools/installationTools.sh";

    /**
     * 部署平台产品集存放目录
     */
    @ApiModelProperty(value = "部署平台中产品集目录", example = "/installationTools/productsets")
    private String productsetsPath;

    /**
     * 是否拷贝产品集
     */
    @ApiModelProperty(value = "是否拷贝产品集", example = "true")
    private boolean wgetProductSetEnable;

    /**
     * 产品集URL路径
     */
    @ApiModelProperty(value = "是否拷贝产品集", example = "http://ftp.baili-inc.com/devOpsTools/productset/StressTestingTools/prod/StressTestingTools-4.9.1-RELEASE.tar")
   private String  probePackageUrl;


    /**
     * 部署平台配置文件中是否需要替换的参数
     */
    @ApiModelProperty(value = "部署平台配置文件中是否需要修改参数")
    private Boolean replaceKeyValueEnable;

    /**
     * 部署平台配置文件路径
     */
    @ApiModelProperty(value = "部署平台配置文件路径")
    private String installationToolsConfigPath;

    /**
     * 部署平台配置文件中需要替换的参数
     */
    @ApiModelProperty(value = "部署平台配置文件中需要修改的参数，用于指定环境,示例：")
    private List<Map<String, String>> replaceKeyValue;
    /**
     * 部署平台启动并且下载完产品集的总时间
     */
    @ApiModelProperty(value = "部署平台启动并完成下载产品级的总时间")
    private String timeout = "10000";


}
