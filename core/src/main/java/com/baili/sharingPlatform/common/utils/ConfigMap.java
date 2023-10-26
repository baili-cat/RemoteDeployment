/*
 * Created by baili on 2021/01/19.
 */
package com.baili.sharingPlatform.common.utils;

import com.baili.sharingPlatform.common.GenericEnum;
import com.baili.sharingPlatform.common.jackson.Jackson;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author baili
 * @date 2021/01/19.
 */
public class ConfigMap extends HashMap<String, Object> {

	public ConfigMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public ConfigMap(int initialCapacity) {
		super(initialCapacity);
	}

	public ConfigMap() {
	}

	public ConfigMap(Map<? extends String, ?> m) {
		super(m);
	}

	public static ConfigMap fromJSON(String json) {
		//noinspection unchecked
		Map<String, Object> map = Jackson.parseObject(json, HashMap.class);
		return new ConfigMap(map);
	}

	static <T> Supplier<T> orNullableThrow(String key, boolean... required) {
		return () -> {
			if (required.length > 0 && required[0]) {
				throw new IllegalArgumentException(String.format("key \"%s\" value is null", key));
			}
			return null;
		};
	}

//	public <T> T toValidJavaObject(Class<T> clazz) {
//		T obj = toJavaObject(clazz);
//		ValidatorUtils.validate(obj);
//		return obj;
//	}

	public <T> T toJavaObject(Class<T> clazz) {
		return Jackson.parseObject(Jackson.toString(this), clazz);
	}

	public <T> List<T> getListInJSON(String key, Class<T> clazz, boolean... required) {
		return getRawValue(key).map(o -> {
			String json = Jackson.toString(o);
			return Jackson.parseArray(json, clazz);
		}).orElseGet(orNullableThrow(key, required));
	}

	public <T> T getObjectInJSON(String key, Class<T> clazz, boolean... required) {
		return getRawValue(key).map(o -> {
			String json = Jackson.toString(o);
			return Jackson.parseObject(json, clazz);
		}).orElseGet(orNullableThrow(key, required));
	}

	public <T> T getObject(String key, boolean... required) {
		//noinspection unchecked
		return (T)getRawValue(key).orElseGet(orNullableThrow(key, required));
	}

	public <T extends Enum> T getEnum(String key, Class<T> type, boolean... required) {
		String value = getString(key, required);
		if (value == null) {
			return null;
		}
		if (GenericEnum.class.isAssignableFrom(type)) {
			for (T e : type.getEnumConstants()) {
				if (((GenericEnum<?>)e).getValue().toString().equals(value)) {
					return e;
				}
			}
			throw new IllegalArgumentException("Cannot convert " + value + " to " + type.getName() + ".");
		} else {
			return (T)Enum.valueOf(type, value);
		}
	}

	public String getString(String key, boolean... required) {
		return getRawValue(key).map(o -> {
			String value = TypeConvertUtils.convertToString(o);
			return StringUtils.trimToNull(value);
		}).orElseGet(orNullableThrow(key, required));
	}

	public String getString(String key, String defaultValue) {
		return getRawValue(key).map(o -> {
			String value = TypeConvertUtils.convertToString(o);
			return StringUtils.trimToNull(value);
		}).orElse(defaultValue);
	}

	public void setString(String key, String value) {
		setValue(key, value);
	}

	public Integer getInteger(String key, boolean... required) {
		return getRawValue(key).map(TypeConvertUtils::convertToInt).orElseGet(orNullableThrow(key, required));
	}

	public int getInteger(String key, int defaultValue) {
		return getRawValue(key).map(TypeConvertUtils::convertToInt).orElse(defaultValue);
	}

	public void setInteger(String key, int value) {
		setValue(key, value);
	}

	public Long getLong(String key, boolean... required) {
		return getRawValue(key).map(TypeConvertUtils::convertToLong).orElseGet(orNullableThrow(key, required));
	}

	public long getLong(String key, long defaultValue) {
		return getRawValue(key).map(TypeConvertUtils::convertToLong).orElse(defaultValue);
	}

	public void setLong(String key, long value) {
		setValue(key, value);
	}

	public Boolean getBoolean(String key, boolean... required) {
		return getRawValue(key).map(TypeConvertUtils::convertToBoolean).orElseGet(orNullableThrow(key, required));
	}

	/*public boolean getBoolean(String key, boolean defaultValue) {
		return getRawValue(key).map(TypeConvertUtils::convertToBoolean).orElse(defaultValue);
	}*/

	public void setBoolean(String key, boolean value) {
		setValue(key, value);
	}

	public Float getFloat(String key, boolean... required) {
		return getRawValue(key).map(TypeConvertUtils::convertToFloat).orElseGet(orNullableThrow(key, required));
	}

	public float getFloat(String key, float defaultValue) {
		return getRawValue(key).map(TypeConvertUtils::convertToFloat).orElse(defaultValue);
	}

	public void setFloat(String key, float value) {
		setValue(key, value);
	}

	public Double getDouble(String key, boolean... required) {
		return getRawValue(key).map(TypeConvertUtils::convertToDouble).orElseGet(orNullableThrow(key, required));
	}

	public double getDouble(String key, double defaultValue) {
		return getRawValue(key).map(TypeConvertUtils::convertToDouble).orElse(defaultValue);
	}

	public void setDouble(String key, double value) {
		setValue(key, value);
	}

	public String[] getStringArray(String key, boolean... required) {
		return getRawValue(key).map(o -> {
			if (o.getClass().equals(String[].class)) {
				return (String[])o;
			} else {
				throw new IllegalArgumentException(String.format("Cannot evaluate value %s as a String[] value", o));
			}
		}).orElseGet(orNullableThrow(key, required));
	}

	public String[] getStringArray(String key, String separator, boolean... required) {
		return getRawValue(key).map(o -> {
			if (o.getClass() == String.class) {
				return splitValue((String)o, separator);
			} else {
				throw new IllegalArgumentException(String.format("Cannot evaluate value %s as a String[] value", o));
			}
		}).orElseGet(orNullableThrow(key, required));
	}

	public int[] getIntegerArray(String key, boolean... required) {
		return getRawValue(key).map(o -> {
			if (o.getClass().equals(int[].class)) {
				return (int[])o;
			} else if (o.getClass().equals(Integer[].class)) {
				return Arrays.stream((Integer[])o).mapToInt(value -> value).toArray();
			} else {
				throw new IllegalArgumentException(String.format("Cannot evaluate value %s as a int[] value", o));
			}
		}).orElseGet(orNullableThrow(key, required));
	}

	public int[] getIntegerArray(String key, String separator, boolean... required) {
		return getRawValue(key).map(o -> {
			if (o.getClass() == String.class) {
				return Arrays.stream(splitValue((String)o, separator)).mapToInt(TypeConvertUtils::convertToInt).toArray();
			} else {
				throw new IllegalArgumentException(String.format("Cannot evaluate value %s as a int[] value", o));
			}
		}).orElseGet(orNullableThrow(key, required));
	}

	public long[] getLongArray(String key, boolean... required) {
		return getRawValue(key).map(o -> {
			if (o.getClass().equals(long[].class)) {
				return (long[])o;
			} else if (o.getClass().equals(Long[].class)) {
				return Arrays.stream((Long[])o).mapToLong(value -> value).toArray();
			} else {
				throw new IllegalArgumentException(String.format("Cannot evaluate value %s as a long[] value", o));
			}
		}).orElseGet(orNullableThrow(key, required));
	}

	public long[] getLongArray(String key, String separator, boolean... required) {
		return getRawValue(key).map(o -> {
			if (o.getClass() == String.class) {
				return Arrays.stream(splitValue((String)o, separator)).mapToLong(TypeConvertUtils::convertToLong).toArray();
			} else {
				throw new IllegalArgumentException(String.format("Cannot evaluate value %s as a long[] value", o));
			}
		}).orElseGet(orNullableThrow(key, required));
	}

	private String[] splitValue(String value, String separator) {
		if (StringUtils.isEmpty(separator)) {
			throw new NullPointerException("Separator must not be null.");
		}
		return value.split(separator);
	}

	private <T> void setValue(String key, T value) {
		if (StringUtils.isEmpty(key)) {
			throw new NullPointerException("Key must not be null.");
		}
		this.put(key, value);
	}

	private Optional<Object> getRawValue(String key) {
		if (StringUtils.isEmpty(key)) {
			throw new NullPointerException("Key must not be null.");
		}

		Object value = this.get(key);
		if (value instanceof String && StringUtils.isEmpty((String)value)) {
			value = null;
		}
		return Optional.ofNullable(value);
	}
}
