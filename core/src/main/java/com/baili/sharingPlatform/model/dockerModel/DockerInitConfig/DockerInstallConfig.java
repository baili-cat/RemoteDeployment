package com.baili.sharingPlatform.model.dockerModel.DockerInitConfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年11月14日14:09
 */
@Data
public class DockerInstallConfig extends DockerInitConfig{
    //docker初始化安装文件下载url
    private String dockerInstallPackageUrl;
    //docker部署目录
    private String dockerInstallPath;
    @ApiModelProperty(value = "docker初始化安装时conf中需要修改的参数")
    private List<Map<String, String>> dockerReplaceKeyValue;
    //docker操作
    @NotNull
    private String action;

}
