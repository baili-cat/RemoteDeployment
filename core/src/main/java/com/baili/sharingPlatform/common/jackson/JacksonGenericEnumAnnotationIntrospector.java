/*
 * Created by baili on 2019/05/21.
 */
package com.baili.sharingPlatform.common.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.baili.sharingPlatform.common.GenericEnum;

/**
 * Jackson通用枚举类型{@link GenericEnum}处理器
 *
 * @author baili
 * @date 2019/05/21.
 */
public class JacksonGenericEnumAnnotationIntrospector extends JacksonAnnotationIntrospector {

	@Override
	public Version version() {
		return PackageVersion.VERSION;
	}

	@Override
	public Object findSerializer(Annotated am) {
		if (am instanceof AnnotatedClass) {
			if (GenericEnum.class.isAssignableFrom(am.getRawType())) {
				Class<?> type = am.getRawType();
				//noinspection unchecked,rawtypes
				return new JacksonGenericEnumSerializer(type);
			}
		}
		return super.findSerializer(am);
	}

	@Override
	public Object findDeserializer(Annotated am) {
		if (am instanceof AnnotatedClass) {
			if (GenericEnum.class.isAssignableFrom(am.getRawType())) {
				Class<?> type = am.getRawType();
				//noinspection unchecked,rawtypes
				return new JacksonGenericEnumDeserializer(type);
			}
		}
		return super.findDeserializer(am);
	}
}
