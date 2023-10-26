/*
 * Created by baili on 2020/10/29.
 */
package com.baili.sharingPlatform.common.utils;

/**
 * 通用数据转换接口
 *
 * @author baili
 * @date 2020/10/29.
 */
public interface Converter<T, E> {

	T convert(E value);
}
