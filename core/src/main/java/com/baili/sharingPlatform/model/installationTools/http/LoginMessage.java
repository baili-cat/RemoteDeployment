package com.baili.sharingPlatform.model.installationTools.http;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author baili
 * @date 2022年06月13日11:50 上午
 */
@Data
public class LoginMessage {

    private String username;

    private String password;


    public Map<String, Object> getLoginMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        return map;
    }
}
