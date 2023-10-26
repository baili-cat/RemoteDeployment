package com.baili.sharingPlatform.web.requestModel;

import com.baili.sharingPlatform.config.ServerChainConfig;
import com.baili.sharingPlatform.config.WgetConfig;
import lombok.Data;

/**
 * @author baili
 * @date 2022年05月17日7:40 下午
 */
@Data
public class WgetArchiveFileUpload {
    private ServerChainConfig serverChainConfig;
    private WgetConfig wgetConfig;
}
