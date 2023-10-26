package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.model.installationTools.http.LoginMessage;
import lombok.Data;

/**
 * @author baili
 * @date 2022年08月20日3:33 PM
 */
@Data
public class DeployInstallationToolsAppMessage {
    private String installationToolsHostIp;
    private LoginMessage loginMessage;
    private String deployBackendName;
}
