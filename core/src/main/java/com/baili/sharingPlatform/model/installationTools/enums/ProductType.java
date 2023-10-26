package com.baili.sharingPlatform.model.installationTools.enums;

import com.baili.sharingPlatform.common.GenericEnum;
import lombok.Getter;

@Getter
public enum ProductType implements GenericEnum<String> {
    //产品集类型,这里是按照现有命名规则抽出来的通用判断条件用于区分stressTestingTools产品集是V4还是V5，如果后面规则修改可能需要修改
    //v4产品集4.7以下和4.7以上产品集初始化sql不一样
    StressTestingToolsV5("-5"),
    StressTestingToolsV4Low("-4.7"),
    StressTestingToolsV4("-4");


    private final String value;

    ProductType(String value) {
        this.value = value;
    }

}
