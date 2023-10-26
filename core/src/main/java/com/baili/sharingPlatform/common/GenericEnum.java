/*
 * Created by baili on 2019/05/17.
 */
package com.baili.sharingPlatform.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 通用枚举类型接口
 *
 * @author baili
 * @date 2019/05/17.
 */
public interface GenericEnum<T> {

	Set<Class<?>> SUPPORT_TYPES = new HashSet<>(Arrays.asList(String.class, Integer.class, Long.class));

	/**
	 * 枚举值，支持以下类型{@link GenericEnum#SUPPORT_TYPES}
	 */
	T getValue();

}
