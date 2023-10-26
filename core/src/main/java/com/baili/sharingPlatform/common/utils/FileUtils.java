/*
 * Created by baili on 2020/11/30.
 */
package com.baili.sharingPlatform.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author baili
 * @date 2020/11/30.
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

	public static boolean fileExists(String fileName) {
		return fileExists(new File(fileName));
	}

	public static boolean fileExists(File file) {
		return file.exists();
	}

	public static String fileRead(String file) throws IOException {
		return fileRead(new File(file));
	}

	public static String fileRead(File file) throws IOException {
		return readFileToString(file, StandardCharsets.UTF_8);
	}

}
