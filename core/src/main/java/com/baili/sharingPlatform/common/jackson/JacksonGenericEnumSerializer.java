/*
 * Created by baili on 2019/05/17.
 */
package com.baili.sharingPlatform.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.baili.sharingPlatform.common.GenericEnum;
import org.springframework.core.ResolvableType;

import java.io.IOException;

/**
 * Jackson通用枚举类型{@link GenericEnum}处理器
 *
 * @author baili
 * @date 2019/05/17.
 */
public class JacksonGenericEnumSerializer<E extends Enum<?> & GenericEnum<?>> extends JsonSerializer<GenericEnum<?>> {

	JacksonGenericEnumSerializer(Class<E> type) {
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
	}

	@Override
	public void serialize(GenericEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeObject(value.getValue());
	}
}
