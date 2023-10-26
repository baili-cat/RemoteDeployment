/*
 * Created by baili on 2019/04/28.
 */
package com.baili.sharingPlatform.common.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baili.sharingPlatform.common.jackson.Jackson;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean对象转换工具类
 * <p>
 * org.apache.commons.beanutils.BeanUtils.copyProperties拷贝对象属性的时候有问题
 * 如果对象Class上使用@Accessors，则获取不到set方法
 *
 * @author baili
 * @date 2019/04/28.
 */
@Slf4j
public final class BeanUtils extends org.springframework.beans.BeanUtils {

	private BeanUtils() {
	}

	/**
	 * 普通对象转换
	 * <p>
	 * 注意：目标对象类型，必须含有无参构造函数，如果目标对象<targetClass>字段是简单类型，则字段的类型必须和被转换的对象<source>字段类型一致。
	 * <p>
	 *
	 * @param targetClass 目标对象类型
	 * @param source 被转换对象
	 */
	public static <T> T transformInJSON(Class<T> targetClass, Object source) {
		if (source == null) {
			return null;
		}
		try {
			String jsonText = Jackson.toString(source);
			return Jackson.parseObject(jsonText, targetClass);
		} catch (Exception e) {
			log.error("Bean对象转换出错：目标象类类型:{}, 源对象类型:{}", targetClass, source.getClass(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 普通对象转换(List)
	 * <p>
	 * 注意：目标对象类型，必须含有无参构造函数，如果目标对象<targetClass>字段是简单类型，则字段的类型必须和被转换的对象<source>字段类型一致。
	 * <p>
	 *
	 * @param targetClass 目标对象类型
	 * @param sources 被转换对象集合
	 */
	public static <T> List<T> transformInJSON(Class<T> targetClass, List<?> sources) {
		if (sources == null) {
			return null;
		}
		try {
			String jsonText = Jackson.toString(sources);
			return Jackson.parseArray(jsonText, targetClass);
		} catch (Exception e) {
			log.error("Bean对象转换出错：目标象类类型:{}, 源对象类型:{}", targetClass, sources.getClass(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 普通对象转换
	 * <p>
	 * 注意：目标对象类型，必须含有无参构造函数，并且目标对象<targetClass>字段的类型必须和被转换的对象<source>字段类型一致。
	 * 如果字段类型是自定义对象，并且2边类型不一致，请使用 {@link BeanUtils#transformInJSON(Class, Object)}
	 * <p>
	 *
	 * @param targetClass 目标对象类型
	 * @param source 被转换对象
	 * @param ignoreFields 忽略字段
	 */
	public static <T> T transform(Class<T> targetClass, Object source, String... ignoreFields) {
		if (source == null) {
			return null;
		}
		try {
			T dest = targetClass.newInstance();
			BeanUtils.copyProperties(source, dest, ignoreFields);
			return dest;
		} catch (Exception e) {
			log.error("Bean对象转换出错：目标象类类型:{}, 源对象类型:{}", targetClass, source.getClass(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 普通对象转换(List)
	 * <p>
	 * 注意：目标对象类型，必须含有无参构造函数，并且目标对象<targetClass>字段的类型必须和被转换的对象<source>字段类型一致。
	 * 如果字段类型是自定义对象，并且2边类型不一致，请使用 {@link BeanUtils#transformInJSON(Class, List)}
	 * <p>
	 *
	 * @param targetClass 目标对象类型
	 * @param sources 被转换对象集合
	 * @param ignoreFields 忽略字段
	 */
	public static <T> List<T> transform(Class<T> targetClass, List<?> sources, String... ignoreFields) {
		if (sources == null) {
			return null;
		}
		//noinspection unchecked
		List<T> results = (List<T>)newList(sources.getClass(), sources.size());
		for (Object obj : sources) {
			results.add(transform(targetClass, obj, ignoreFields));
		}
		return results;
	}

	private static List<?> newList(@SuppressWarnings("rawtypes") Class<? extends List> type, int len) {
		if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
			try {
				return (List<?>)type.newInstance();
			} catch (Exception e) {
				// ignore
			}
		}
		return new ArrayList<>(len);
	}

	public static Map<String, Object> beanToMap(Object bean) {
		return beanToMap(bean, false, false);
	}

	public static Map<String, Object> beanToMap(Object bean, boolean isToUnderlineCase, boolean ignoreNullValue) {
		return BeanUtil.beanToMap(bean, isToUnderlineCase, ignoreNullValue);
	}

}
