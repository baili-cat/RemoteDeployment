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
public enum OSType implements GenericEnum<String> {
    //部署操作系统类型
    Centos7("centos7","linux"),
    RedHat7("redhat7","linux"),
    Ubuntu16("ubuntu16","linux"),
    Kylin20("kylin20","linux"),
    Solaris11("solaris11","solaris"),
    MacOS("macos","mocos"),
    AIX("aix","aix"),
    Windows("windows10","windows"),
    unknown("unknown","unknown");

    private final String value;
    private final String key;


    OSType(String value,String key) {
        this.key = key;
        this.value = value;
    }

}
