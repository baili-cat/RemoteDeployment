package com.baili.sharingPlatform.model.mockServer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年05月19日11:19 上午
 */

@Data
public class MockApplicationConfig {

    /**
     * mock应用原始安装包路径
     */
    @ApiModelProperty(value = "需要部署的mock应用下载地址", example = "http://ftp-project.baili-inc.com/baili/java/applicationJarPackage/springboot/tdolphin/IncludeStartupScript/tdolphinMockIncludeStartupScript.tar.gz")
    private String mockUrl;

    /**
     * mock应用安装目录
     */
    @NotEmpty
    private String installDir;

    /**
     * mock应用的包名
     */
    @ApiModelProperty(value = "mock启动后进程检索标识,建议使用包名，不会产生重复数据", example = "process-agent-test-case")
    private String packageName;

    /**
     * mock应用启动脚本路径
     */
    @ApiModelProperty(value = "mock服务启动脚本相对路径", example = "/mock/start.sh")
    @Value("${startScriptPath:/mock/start.sh")
    private String startScriptPath;

    /**
     * mock应用停止脚本路径
     */
    @ApiModelProperty(value = "mock服务停止脚本相对路径", example = "/mock/stop.sh")
    @Value("${startScriptPath:/mock/stop.sh")
    private String stopScriptPath;

    /**
     * mock应用启动脚本中需要替换的参数
     */
    @ApiModelProperty(value = "mock应用启动脚本中是否需要修改参数")
    private Boolean replaceKeyValueEnable;

    /**
     * mock应用启动脚本中需要替换的参数的配置文件路径
     */
    @ApiModelProperty(value = "mock应用启动时配置文件路径")
    private String mockConfigPath;

    /**
     * mock应用启动脚本中需要替换的参数
     */
    @ApiModelProperty(value = "mock应用启动脚本中需要修改的参数，用于指定环境,示例：")
    private List<Map<String, String>> replaceKeyValue;

    /**
     * 是否重新部署javaAgent包
     * 默认为true
     */
    @Value("${deployAgentEnable:true}")
    private Boolean deployAgentEnable;

    /**
     * javaagent路径
     */
    private String agentBootstrapRelativePath;
    /**
     * -DPERFMA_APP_CODE
     */
    private String bailiAppCode;
    /**
     * 启动时需要的jvm参数
     */
    private String jvmParameters;

    /**
     * 是否挂在探针
     * 默认为true
     */
    @Value("${mountAgentEnable:true}")
    private Boolean mountAgentEnable;

}
