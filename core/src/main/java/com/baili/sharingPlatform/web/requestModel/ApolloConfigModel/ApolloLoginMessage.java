package com.baili.sharingPlatform.web.requestModel.ApolloConfigModel;

import lombok.Data;

/**
 * @author baili
 * @date 2023年02月01日09:36
 */
@Data
public class ApolloLoginMessage {
    private String apolloAddress;
    private String userName;
    private String passWord;
}
