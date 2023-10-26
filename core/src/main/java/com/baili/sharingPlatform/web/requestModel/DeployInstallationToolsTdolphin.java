package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.model.installationTools.InstallationToolsConfig;
import com.baili.sharingPlatform.model.installationTools.InstallationToolsTdolphinConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年08月18日11:06 PM
 */
@Data
public class DeployInstallationToolsTdolphin {
    private ServerConfig serverConfig;
    private InstallationToolsConfig installationToolsConfig;
    private InstallationToolsTdolphinConfig installationToolsTdolphinConfig;
}
