package com.baili.sharingPlatform.model.enums;

import com.baili.sharingPlatform.common.GenericEnum;
import lombok.Getter;

/**
 * @author baili
 * @date 2022年05月17日5:35 下午
 */
@Getter
public enum ApplicationAction implements GenericEnum<String> {
    //进程状态切换
    Start("start"),
    Stop("stop");

    private final String value;

    ApplicationAction(String value) {
        this.value = value;
    }
}
