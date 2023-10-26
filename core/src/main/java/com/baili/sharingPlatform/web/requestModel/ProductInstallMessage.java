package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.model.installationTools.http.LoginMessage;
import com.baili.sharingPlatform.model.installationTools.http.ServerHostMessage;
import lombok.Data;

/**
 * @author baili
 * @date 2022年08月20日3:33 PM
 */
@Data
public class ProductInstallMessage {
    private String installationToolsHostIp;
    //是否已安装部署平台
    private boolean installationToolsInstalled;

    private LoginMessage loginMessage;


    private String productName;
    private ServerHostMessage serverHostMessage;

    private DeployInstallationTools deployInstallationTools;
    //private String dataDir;
}
