package com.baili.sharingPlatform.model.enums;

import com.baili.sharingPlatform.common.GenericEnum;
import lombok.Getter;

/**
 * @author baili
 * @date 2022年05月20日3:24 下午
 */
@Getter
public enum MockApplicationStatus implements GenericEnum<String> {
    //进程状态切换
    Start("start"),
    Stop("stop");

    private final String value;

    MockApplicationStatus(String value) {
        this.value = value;
    }
}
