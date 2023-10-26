package com.baili.sharingPlatform.web.requestModel.ApolloConfigModel;

import com.baili.sharingPlatform.api.web.requestModel.DeployInstallationToolsAppMessage;
import lombok.Data;

/**
 * @author baili
 * @date 2023年02月01日09:46
 */
@Data
public class NeedRestartApp {
    private boolean needRestartApp;
    private DeployInstallationToolsAppMessage deployInstallationToolsAppMessage;
}
