/*
 * Created by baili on 2020/10/24.
 */
package com.baili.sharingPlatform.common.archiver;

import com.baili.sharingPlatform.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.tar.TarUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.logging.slf4j.Slf4jLoggerManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author baili
 * @date 2020/10/24.
 */
@Slf4j
public final class ArchiverUtils {

	public static final String FORMAT_TAR    = "tar";
	public static final String FORMAT_TAR_GZ = "tar.gz";
	public static final String FORMAT_ZIP    = "zip";

	private static BaseLoggerManager loggerManager = new Slf4jLoggerManager();

	static {
		loggerManager.initialize();
	}

	private ArchiverUtils() {
	}

	public static void compress(String sourceDirectory, String destFile, String format) throws IOException {
		compress(new File(sourceDirectory), new File(destFile), format);
	}

	/**
	 * 压缩打包
	 *
	 * @param sourceDirectory 压缩目录
	 * @param destFile 输出文件
	 * @param format 压缩格式
	 */
	public static void compress(File sourceDirectory, File destFile, String format) throws IOException {
		AbstractArchiver archiver = getArchiver(format);
		log.info("building {}: {} to {}", format, sourceDirectory, destFile);
		long start = System.currentTimeMillis();
		archiver.addDirectory(sourceDirectory);
		archiver.setDestFile(destFile);
		archiver.createArchive();
		Duration timeConsuming = Duration.ofMillis(System.currentTimeMillis() - start);
		log.info("building {}: {} to {} finished, time cost: {}", format, sourceDirectory, destFile, DateUtils.toDurationString(timeConsuming));
	}

	/**
	 * 解压文件
	 *
	 * @param sourceFile 源文件路径
	 * @param destDirectory 解压目录路径
	 * @param overwrite 是否覆盖
	 */
	public static void extract(String sourceFile, String destDirectory, boolean overwrite) {
		extract(sourceFile, destDirectory, null, overwrite);
	}

	public static void extract(String sourceFile, String destDirectory) {
		extract(sourceFile, destDirectory, true);
	}

	public static List<String> extract(String sourceFile, String destDirectory, FileSelector fileSelector) {
		return extract(sourceFile, destDirectory, fileSelector, true);
	}

	public static List<String> extract(String sourceFile, String destDirectory, FileSelector fileSelector, boolean overwrite) {
		return extract(new File(sourceFile), new File(destDirectory), fileSelector, overwrite);
	}

	public static void extract(File sourceFile, File destDirectory) {
		extract(sourceFile, destDirectory, true);
	}

	public static void extract(File sourceFile, File destDirectory, boolean overwrite) {
		extract(sourceFile, destDirectory, null, overwrite);
	}

	/**
	 * 解压文件
	 *
	 * @param sourceFile 源文件
	 * @param destDirectory 解压目录
	 * @param fileSelector 文件选择器
	 * @param overwrite 是否覆盖
	 */
	public static List<String> extract(File sourceFile, File destDirectory, FileSelector fileSelector, boolean overwrite) {
		if (!destDirectory.exists()) {
			//noinspection ResultOfMethodCallIgnored
			destDirectory.mkdirs();
		}

		log.info("extracting: {} into {}", sourceFile, destDirectory);
		long start = System.currentTimeMillis();
		UnArchiver ua = getUnArchiver(sourceFile);
		ua.setSourceFile(sourceFile);
		ua.setDestDirectory(destDirectory);
		ua.setOverwrite(overwrite);
		List<String> selectPaths = Collections.synchronizedList(new ArrayList<>());
		if (fileSelector != null) {
			ua.setFileSelectors(new FileSelector[]{fileInfo -> {
				if (fileSelector.isSelected(fileInfo)) {
					selectPaths.add(fileInfo.getName());
					return true;
				}
				return false;
			}});
		}
		ua.extract();
		Duration timeConsuming = Duration.ofMillis(System.currentTimeMillis() - start);
		log.info("extracting: {} into {} finished, time cost: {}", sourceFile, destDirectory, DateUtils.toDurationString(timeConsuming));
		return selectPaths;
	}

	public static String getArchiveExt(String path) {
		String archiveExt = FileUtils.getExtension(path).toLowerCase(Locale.ENGLISH);
		if ("gz".equals(archiveExt) || "bz2".equals(archiveExt) || "xz".equals(archiveExt) || "snappy".equals(archiveExt)) {
			String[] tokens = StringUtils.split(path, ".");
			if (tokens.length > 2 && "tar".equals(tokens[tokens.length - 2].toLowerCase(Locale.ENGLISH))) {
				archiveExt = "tar." + archiveExt;
			}
		}
		return archiveExt;
	}

	private static UnArchiver getUnArchiver(File file) {
		String archiveExt = getArchiveExt(file.getAbsolutePath());
		switch (archiveExt) {
			case FORMAT_TAR:
				TarUnArchiver tarUnArchiver = new TarUnArchiver();
				tarUnArchiver.enableLogging(loggerManager.getLoggerForComponent("TarUnArchiver"));
				return tarUnArchiver;
			case FORMAT_TAR_GZ:
				TarGZipUnArchiver tarGZipUnArchiver = new TarGZipUnArchiver();
				tarGZipUnArchiver.enableLogging(loggerManager.getLoggerForComponent("TarGZipUnArchiver"));
				return tarGZipUnArchiver;
			case FORMAT_ZIP:
				ZipUnArchiver zipUnArchiver = new ZipUnArchiver();
				zipUnArchiver.enableLogging(loggerManager.getLoggerForComponent("ZipUnArchiver"));
				return zipUnArchiver;
			default:
				throw new IllegalArgumentException(String.format("压缩文件格式(%s)不支持", archiveExt));
		}
	}

	private static AbstractArchiver getArchiver(String format) {
		switch (format) {
			case FORMAT_TAR:
			case FORMAT_TAR_GZ:
				TarArchiver tarArchiver = new TarArchiver();
				if (format.equals(FORMAT_TAR_GZ)) {
					tarArchiver.setCompression(TarArchiver.TarCompressionMethod.gzip);
					tarArchiver.enableLogging(loggerManager.getLoggerForComponent("TarGZipArchiver"));
				} else {
					tarArchiver.enableLogging(loggerManager.getLoggerForComponent("TarArchiver"));
				}
				return tarArchiver;
			case FORMAT_ZIP:
				ZipArchiver zipArchiver = new ZipArchiver();
				zipArchiver.setRecompressAddedZips(false);
				zipArchiver.enableLogging(loggerManager.getLoggerForComponent("ZipArchiver"));
				return zipArchiver;
			default:
				throw new IllegalArgumentException(String.format("不支持的压缩格式: %s", format));
		}
	}
}
