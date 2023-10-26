package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import com.baili.sharingPlatform.model.enums.JavaVersion;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日6:55 下午
 */
@Data
public class UpdateJavaVersion {

    private ServerConfig serverConfig;
    private JavaVersion javaVersion;
}
