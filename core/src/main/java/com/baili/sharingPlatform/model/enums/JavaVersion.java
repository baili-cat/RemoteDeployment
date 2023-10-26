package com.baili.sharingPlatform.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.baili.sharingPlatform.common.GenericEnum;
import lombok.Getter;

/**
 * @author baili
 * @date 2022/5/17 5:54 下午
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum JavaVersion implements GenericEnum<String> {
    //jdk版本对应服务器上的切换信息
    //OPENJDK11
    //OPENJDK8(1.8.0_322-b06)
    //OPENJDK7(1.7.0_80-b15)
    //OPENJDK6(1.6.0_45-b06)
    //IBM8(pxa6480sr5fp41-20190919_01)
    //IBM7(pxa6470sr10fp55-20191010_01)
    //IBM6(pxa6460sr12-20121025_01)

    OPENJDK8("OPENJDK8(1.8.0_322-b06)","1"),
    OPENJDK7("OPENJDK7(1.7.0_80-b15)","2"),
    OPENJDK6("OPENJDK6(1.6.0_45-b06)","3"),
    IBM8("IBM8(pxa6480sr5fp41-20190919_01)","4"),
    IBM7("IBM7(pxa6470sr10fp55-20191010_01)","5"),
    IBM6("IBM6(pxa6460sr12-20121025_01)","6"),
    OPENJDK11("OPENJDK11", "7");

    private final String key;
    private final String value;


    JavaVersion(String value,String key) {
        this.key = key;
        this.value = value;
    }

}
