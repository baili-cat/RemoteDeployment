package com.baili.sharingPlatform.model.enums;

import com.baili.sharingPlatform.common.GenericEnum;
import lombok.Getter;

/**
 * @author baili
 * @date 2022年10月27日09:33
 */
@Getter
public enum DockerApplicationAction  implements GenericEnum<String> {
    //进程状态切换
    Start("start"),
    Status("status"),
    Restart("resart");
    private final String value;

    DockerApplicationAction(String value) {
        this.value = value;
    }
}
