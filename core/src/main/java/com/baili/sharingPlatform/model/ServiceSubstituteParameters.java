package com.baili.sharingPlatform.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年05月10日10:58 上午
 */
@Data
@ApiModel(value = "ServiceSubstituteParameters", description = "需要替换的文件以及替换的值")
public class ServiceSubstituteParameters {

    /**
     * 需要替换的参数key
     */
    @ApiModelProperty(value = "需要配置的配置文件中的key,以及要配置的value", example = "baili.agent.xcenter.server=10.10.x" +
            ".x:port或{test}")
    @NotEmpty(message = "需要替换的参数key不能为空")
    private List<Map<String, String>> replaceKeyValue;

    /**
     * 需要调换的参数检索目录
     */
    @ApiModelProperty(value = "需要替换的配置文件绝对路径", example = "/home/baili/")
    @NotEmpty(message = "需要替换的参数检索目录不能为空")
    private String filelPath;
}
