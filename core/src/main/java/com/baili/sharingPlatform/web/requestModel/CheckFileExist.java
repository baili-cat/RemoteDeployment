package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.common.ssh.ServerConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日7:32 下午
 */
@Data
public class CheckFileExist {
    private ServerConfig serverConfig;
    private String fileName;
}
