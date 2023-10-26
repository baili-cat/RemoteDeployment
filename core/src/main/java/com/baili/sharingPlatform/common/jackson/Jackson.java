/*
 * Created by baili on 2021/06/01.
 */
package com.baili.sharingPlatform.common.jackson;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * JSON转换工具
 * <p>
 * Jackson 框架的高阶应用: https://www.ibm.com/developerworks/cn/java/jackson-advanced-application/index.html
 *
 * @author baili
 * @date 2021/06/01.
 */
public final class Jackson {

	private static final ObjectMapper MAPPER;

	static {
		MAPPER = createMapper();
		// 对象字段值为NULL的不显示
		MAPPER.setSerializationInclusion(Include.NON_NULL);
		// Map中键值为NULL的不显示
		MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	}

	private Jackson() {
	}

	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// 字段排序，保证顺序生成JSON
		mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		// 输入时忽略JSON字符串中存在而Java对象实际没有的属性
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// 日期格式
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		// 时区设置
		mapper.setTimeZone(TimeZone.getTimeZone("GMT+08"));
		// 通用枚举类型转换处理
		mapper.setAnnotationIntrospector(new JacksonGenericEnumAnnotationIntrospector());
		return mapper;
	}

	public static String toString(Object object) {
		try {
			return MAPPER.writeValueAsString(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] toBytes(Object object) {
		try {
			return MAPPER.writeValueAsBytes(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> parseArray(String text, Class<T> clazz) {
		try {
			JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
			return MAPPER.readValue(text, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parseObject(String text, Class<T> clazz) {
		try {
			return MAPPER.readValue(text, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> valueToArray(JsonNode jsonNode, Class<T> clazz) {
		try {
			JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
			ObjectReader reader = MAPPER.readerFor(type);
			return reader.readValue(jsonNode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T valueToObject(JsonNode jsonNode, Class<T> clazz) {
		try {
			return MAPPER.treeToValue(jsonNode, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode readValue(String text) {
		try {
			return MAPPER.readTree(text);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode readValue(InputStream in) {
		try {
			return MAPPER.readTree(in);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode readValue(byte[] content) {
		try {
			return MAPPER.readTree(content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T readValue(String text, TypeReference<T> typeRef) {
		try {
			return MAPPER.readValue(text, typeRef);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeValue(File file, Object object) {
		try {
			MAPPER.writeValue(file, object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeValue(OutputStream out, Object object) {
		try {
			MAPPER.writeValue(out, object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeValue(Writer w, Object object) {
		try {
			MAPPER.writeValue(w, object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
