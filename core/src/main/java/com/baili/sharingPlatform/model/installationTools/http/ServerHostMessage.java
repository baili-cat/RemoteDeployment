package com.baili.sharingPlatform.model.installationTools.http;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

/**
 * @author baili
 * @date 2022年05月19日11:19 上午
 */

@Data
public class ServerHostMessage {

    @ApiModelProperty(hidden = true)
    private  String groupId;
    /**
     * 需要添加的服务器ip
     */
    @NotEmpty
    @ApiModelProperty(value = "需要添加的服务器IP", example = "ip")
    private String serverHostIp;

    /**
     * 需要添加的服务器端口
     */
    @NotEmpty
    @ApiModelProperty(value = "需要添加的服务器ssh端口", example = "22")
    private String serverHostPort;

    /**
     * 需要添加的服务器用户名
     */
    @NotEmpty
    @ApiModelProperty(value = "需要添加的服务器用户名", example = "root")
    private String serverHostUserName;
    /**
     * 需要添加的服务器密码
     */
    @NotEmpty
    @ApiModelProperty(value = "需要添加的服务器密码", example = "password")
    private String serverHostpassword;

    @ApiModelProperty(value = "需要添加的服务器鉴权方式", example = "password", hidden = true)
    private String serverHostAutuType;
    @ApiModelProperty(value = "额外信息", hidden = true)
    private Map<String,Object> extensions;

    private String serverHostInstallDir;
    private String serverDockerDataDir;
    private String initType;

    //private String groupId;

    public Map<String, Object> getServerHostMessage() {
        Map<String, Object> map = new HashMap<>();
        setExtensions();
        map.put("groupId",groupId);
        map.put("host",serverHostIp);
        map.put("user", serverHostUserName);
        map.put("password", serverHostpassword);
        map.put("port",serverHostPort);
        map.put("authType",serverHostAutuType);
        map.put("extensions",extensions);
        return map;
    }

    public void setExtensions() {
        Map<String, Object> map = new HashMap<>();
        map.put("installDir",serverHostInstallDir);
        map.put("dockerDataDir", serverDockerDataDir);
        map.put("initType","auto");
        this.extensions = map;
    }


}
