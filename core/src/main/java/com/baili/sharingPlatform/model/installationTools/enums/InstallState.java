package com.baili.sharingPlatform.model.installationTools.enums;

import com.baili.sharingPlatform.common.GenericEnum;
import lombok.Getter;

/**
 * @author baili
 * @date 2022年05月17日5:35 下午
 */
@Getter
public enum InstallState implements GenericEnum<String> {
    //产品集安装阶段
    ProductSetInstall("productset-install"),
    ServerConfigure("server-configure"),
    DeploymentConfigure("deployment-configure"),
    BasicServiceInstall("basic-service-install"),
    DataInit("data-init"),
    LicenseImport("license-import"),
    PlatformServiceInstall("platform-service-install"),
    Initialized("initialized");

    private final String value;

    InstallState(String value) {
        this.value = value;
    }
}
