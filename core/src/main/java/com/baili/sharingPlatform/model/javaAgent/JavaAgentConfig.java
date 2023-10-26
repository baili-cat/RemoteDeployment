package com.baili.sharingPlatform.model.javaAgent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022年05月13日11:15 上午
 */
@Data
@ApiModel(value = "JavaAgentConfig", description = "javaAgent配置文件中需要修改的值")
public class JavaAgentConfig {

    /** JavaAgentConfig需要替换的服务参数 */
    /** example = "{\"PERFMA_APP_CODE\":\"test\"," +
     "\"baili.agent.xcenter.server\": \"10.10.x.x:port\"}"*/
    @ApiModelProperty(value = "javaAgent配置文件中需要修改的参数,示例：")
    private List<Map<String,String>> replaceKeyValue;

    /** javaAgent className*/
    @ApiModelProperty(value = "javaAgent的进程检索标识",  example = "" ,hidden = true)
    private String javaAgentName;

    /** javaAgent 部署目录*/
    private String installDir;

    /**
     * javaAgent解压后的目录
     */
    @ApiModelProperty(value = "javaAgent解压后目录", example = "/baili-java-agent")
    @NotEmpty
    private String javaAgentPath;

    /**
     * 独立探针模块相对路径
     */
    @ApiModelProperty(value = "javaAgent部署后模块存放路径", example = "/baili-java-agent/plugin/modules")
    private String modulesPath;

    /** javaAgent 挂在探针的agent-bootstrap.jar路径*/
    @ApiModelProperty(value = "javaAgent挂在探针的agent-bootstrap.jar路径", example = "/baili-java-agent/agent-bootstrap" +
            ".jar",hidden = true)
    @Value("${agentBootstrapRelativePath:/baili-java-agent/agent-bootstrap.jar}")
    private String agentBootstrapRelativePath;

    /** javaAgent配置文件相对路径*/
    @ApiModelProperty(value = "javaAgent部署后配置文件的相对路径",  example = "/baili-java-agent/agent.config" ,
            hidden = true)
    @Value("${configRelativePath:/baili-java-agent/agent.config}")
    private String configRelativePath;

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
