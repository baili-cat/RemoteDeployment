/*
 * Created by baili on 2021/02/20.
 */
package com.baili.sharingPlatform.common.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * @author baili
 * @date 2021/02/20.
 */
public class PropertiesUtils {

	public static Properties load(String source) {
		Properties properties = new Properties();
		if (source == null) {
			return properties;
		}
		try {
			properties.load(new StringReader(source));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}

}
