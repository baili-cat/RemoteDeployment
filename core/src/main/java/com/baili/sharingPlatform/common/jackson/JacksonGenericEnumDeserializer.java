/*
 * Created by baili on 2019/05/17.
 */
package com.baili.sharingPlatform.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.baili.sharingPlatform.common.GenericEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Jackson通用枚举类型{@link GenericEnum}处理器
 *
 * @author baili
 * @date 2019/05/17.
 */
public class JacksonGenericEnumDeserializer<E extends Enum<?> & GenericEnum<?>> extends JsonDeserializer<GenericEnum<?>> {

	private final Class<E>       type;
	private final Map<String, E> enumConstants;

	JacksonGenericEnumDeserializer(Class<E> type) {
		ResolvableType resolvableType = ResolvableType.forClass(type).as(GenericEnum.class);
		ResolvableType[] generics = resolvableType.getGenerics();
		Class<?> valueType;
		if (generics.length != 1 || (valueType = generics[0].resolve()) == null) {
			throw new RuntimeException("继承通用枚举接口: " + GenericEnum.class.getName() + " 必须指定泛型<T>");
		}
		if (!GenericEnum.SUPPORT_TYPES.contains(valueType)) {
			throw new RuntimeException("继承通用枚举接口: " + GenericEnum.class.getName() + " 泛型<T>必须是以下类型: " + Jackson.toString(GenericEnum.SUPPORT_TYPES));
		}
		if (!type.isEnum()) {
			throw new RuntimeException("目标类型: " + type.getName() + " 必须为Enum类型才能继承通用枚举接口: " + GenericEnum.class.getName());
		}

		this.type = type;
		this.enumConstants = new HashMap<>();
		for (E e : type.getEnumConstants()) {
			this.enumConstants.put(String.valueOf(e.getValue()), e);
		}
	}

	@Override
	public GenericEnum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String value = p.getText();
		if (StringUtils.isEmpty(value)) {
			return null;
		} else if (!this.enumConstants.containsKey(value)) {
			throw new IllegalArgumentException("Cannot convert " + value + " to " + this.type.getName() + ".");
		}
		return this.enumConstants.get(value);
	}
}
