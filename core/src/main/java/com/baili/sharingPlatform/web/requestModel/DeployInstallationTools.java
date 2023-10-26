package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import com.baili.sharingPlatform.model.installationTools.InstallationToolsConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年08月18日11:06 PM
 */
@Data
public class DeployInstallationTools {

    private ServerConfig serverConfig;
    private WgetConfig wgetConfig;
    private InstallationToolsConfig installationToolsConfig;
}
