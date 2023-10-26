package com.baili.sharingPlatform.model.processAgent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年05月12日10:45 下午
 */
@Data
@ApiModel(value = "ProcessAgentConfig", description = "独立探针的目录以及配置文件")
public class ProcessAgentConfig {

    /**
     * 独立探针部署目录
     */
    @NotEmpty
    private String installDir;

    /**
     * 独立探针解压后的目录
     */
    @ApiModelProperty(value = "独立探针解压后目录", example = "/baili-process-agent")
    @NotEmpty
    private String processAgentPath;

    /**
     * 独立探针需要替换的服务参数
     */
    @ApiModelProperty(value = "独立探针配置文件中需要修改的参数")
    private List<Map<String, String>> replaceKeyValue;

    /**
     * 独立探针className
     */
    @ApiModelProperty(value = "独立探针启动后进程检索标识", example = "PerfMaAgentLauncher", hidden = true)
    @Value("${processName:PerfMaAgentLauncher")
    private String processName;

    /**
     * 独立探针配置文件相对路径
     */
    @ApiModelProperty(value = "独立探针部署后配置文件的相对路径", example = "/baili-process-agent/config/process-agent.properties", hidden = true)
    @Value("${configRelativePath:/baili-java-agent/agent.config}")
    private String configRelativePath;

    /**
     * 独立探针模块相对路径
     */
    @ApiModelProperty(value = "独立探针部署后模块存放路径", example = "/baili-process-agent/modules")
    private String modulesPath;

    /**
     * 独立探针启动脚本路径
     */
    @ApiModelProperty(value = "独立探针启动脚本相对路径", example = "/baili-process-agent/start.sh")
    @Value("${startScriptPath:/baili-process-agent/start.sh")
    private String startScriptPath;


    /**
     * 独立探针停止脚本路径
     */
    @ApiModelProperty(value = "独立探针停止脚本相对路径", example = "/baili-process-agent/stop.sh")
    @Value("${startScriptPath:/baili-process-agent/stop.sh")
    private String stopScriptPath;

    /**
     * 是否加载测试模块
     */
    @ApiModelProperty(value = "是否加载测试模块", example = "true")
    private Boolean moduleEnable;

    /**
     * 测试模块存放路径
     */
    @ApiModelProperty(value = "自定义测试模块存放路径")
    private String moduleDemoPath;

}
