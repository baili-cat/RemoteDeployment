package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.model.processAgent.ProcessAgentConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日7:00 下午
 */

@Data
public class ChangeProcessAgentStauts {
    private ServerConfig serverConfig;
    private ProcessAgentConfig processAgentConfig;
    @ApiModelProperty(value = "独立探针状态", example = "start")
    private String actionType;
}
