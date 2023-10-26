package com.baili.sharingPlatform.web.requestModel.ApolloConfigModel;

import lombok.Data;

import java.util.Map;

/**
 * @author baili
 * @date 2023年01月31日18:02
 */
@Data
public class ApolloConfig {
    private String appId;
    private String namespace;
    private Map<String, String> configs;
    private NeedRestartApp needRestartApp;
    private ApolloLoginMessage apolloLoginMessage;
}
