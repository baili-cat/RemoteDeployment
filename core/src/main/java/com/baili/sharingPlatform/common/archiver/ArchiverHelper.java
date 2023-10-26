package com.baili.sharingPlatform.common.archiver;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.baili.sharingPlatform.common.archiver.model.ArchiverSource;
import com.baili.sharingPlatform.common.archiver.phase.ArchiverPhase;
import com.baili.sharingPlatform.common.archiver.phase.FileItemArchiverPhase;
import com.baili.sharingPlatform.common.archiver.phase.FileSetArchiverPhase;
import com.baili.sharingPlatform.common.archiver.phase.StreamItemArchiverPhase;
import com.baili.sharingPlatform.common.archiver.utils.ArchiverFileUtils;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.diags.DryRunArchiver;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarLongFileMode;
import org.codehaus.plexus.archiver.zip.AbstractZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.logging.slf4j.Slf4jLoggerManager;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArchiverHelper {

	private static List<ArchiverPhase> assemblyPhases = new ArrayList<>();
	private static BaseLoggerManager   loggerManager  = new Slf4jLoggerManager();

	static {
		loggerManager.initialize();
		assemblyPhases.add(new FileItemArchiverPhase());
		assemblyPhases.add(new FileSetArchiverPhase());
		assemblyPhases.add(new StreamItemArchiverPhase());
	}

	public static File createArchive(final ArchiverSource archiverSource) throws ArchiverException {
		String format = archiverSource.getFormat();
		String filename = archiverSource.getFinalName() + "." + format;

		final File outputDirectory = archiverSource.getArchiveOutputDir();
		final File destFile = new File(outputDirectory, filename);

		try {
			String basedir = archiverSource.getFinalName();
			String specifiedBasedir = StringUtils.trim(archiverSource.getArchiveBaseDir());

			if (StringUtils.isNotBlank(specifiedBasedir)) {
				basedir = ArchiverFileUtils.getOutputDirectory(specifiedBasedir);
			}

			Archiver archiver = createArchiver(format, archiverSource.isIncludeArchiveBaseDir(), basedir, archiverSource,
					archiverSource.isRecompressZippedFiles());
			archiver.setDestFile(destFile);

			for (ArchiverPhase phase : assemblyPhases) {
				phase.execute(archiver, archiverSource);
			}

			archiver.createArchive();
		} catch (final org.codehaus.plexus.archiver.ArchiverException | IOException e) {
			throw new ArchiverException("Error creating assembly archive: " + e.getMessage(), e);
		}

		return destFile;
	}

	/**
	 * Creates the necessary archiver to build the distribution file.
	 *
	 * @param format Archive format
	 * @param includeBaseDir the base directory for include.
	 * @param finalName The final name.
	 * @param archiverSource {@link ArchiverSource}
	 * @param recompressZippedFiles recompress zipped files.
	 * @return archiver Archiver generated
	 */
	private static Archiver createArchiver(final String format, final boolean includeBaseDir, final String finalName,
			final ArchiverSource archiverSource, boolean recompressZippedFiles) {
		Archiver archiver;
		if (format.startsWith("tar") || "txz".equals(format) || "tgz".equals(format) || "tbz2".equals(format)) {
			archiver = createTarArchiver(format, TarLongFileMode.valueOf(archiverSource.getTarLongFileMode()));
		} else if ("zip".equals(format)) {
			archiver = createZipArchiver(recompressZippedFiles);
		} else {
			throw new IllegalArgumentException("Unknown archive format: " + format);
		}

		String prefix = "";
		if (includeBaseDir) {
			prefix = finalName;
		}

		archiver = new ProxyArchiver(prefix, archiver, archiverSource.getWorkingDirectory(), loggerManager.getLoggerForComponent("ArchiverHelper"));
		if (archiverSource.isDryRun()) {
			archiver = new DryRunArchiver(archiver, loggerManager.getLoggerForComponent("ArchiverHelper"));
		}

		archiver.setUseJvmChmod(archiverSource.isUpdateOnly());
		archiver.setIgnorePermissions(archiverSource.isIgnorePermissions());
		archiver.setForced(!archiverSource.isUpdateOnly());

		if (archiverSource.getOverrideUid() != null) {
			archiver.setOverrideUid(archiverSource.getOverrideUid());
		}
		if (StringUtils.isNotBlank(archiverSource.getOverrideUserName())) {
			archiver.setOverrideUserName(StringUtils.trim(archiverSource.getOverrideUserName()));
		}
		if (archiverSource.getOverrideGid() != null) {
			archiver.setOverrideGid(archiverSource.getOverrideGid());
		}
		if (StringUtils.isNotBlank(archiverSource.getOverrideGroupName())) {
			archiver.setOverrideGroupName(StringUtils.trim(archiverSource.getOverrideGroupName()));
		}
		return archiver;
	}

	private static Archiver createTarArchiver(final String format, final TarLongFileMode tarLongFileMode) {
		final TarArchiver tarArchiver = (TarArchiver)getArchiver("tar");
		final int index = format.indexOf('.');
		if (index >= 0) {
			TarArchiver.TarCompressionMethod tarCompressionMethod;
			// TODO: this should accept gz and bz2 as well so we can skip
			// TODO: over the switch
			final String compression = format.substring(index + 1);
			if ("gz".equals(compression)) {
				tarCompressionMethod = TarArchiver.TarCompressionMethod.gzip;
			} else if ("bz2".equals(compression)) {
				tarCompressionMethod = TarArchiver.TarCompressionMethod.bzip2;
			} else if ("xz".equals(compression)) {
				tarCompressionMethod = TarArchiver.TarCompressionMethod.xz;
			} else if ("snappy".equals(compression)) {
				tarCompressionMethod = TarArchiver.TarCompressionMethod.snappy;
			} else {
				throw new IllegalArgumentException("Unknown compression format: " + compression);
			}
			tarArchiver.setCompression(tarCompressionMethod);
		} else if ("tgz".equals(format)) {
			tarArchiver.setCompression(TarArchiver.TarCompressionMethod.gzip);
		} else if ("tbz2".equals(format)) {
			tarArchiver.setCompression(TarArchiver.TarCompressionMethod.bzip2);
		} else if ("txz".equals(format)) {
			tarArchiver.setCompression(TarArchiver.TarCompressionMethod.xz);
		}
		tarArchiver.setLongfile(tarLongFileMode);
		return tarArchiver;
	}

	private static Archiver getArchiver(String format) {
		switch (format) {
			case "tar":
				return new TarArchiver();
			case "zip":
				return new ZipArchiver();
		}
		throw new IllegalArgumentException(String.format("Unsupported compression format: %s", format));
	}

	private static Archiver createZipArchiver(boolean recompressZippedFiles) {
		AbstractZipArchiver archiver = (AbstractZipArchiver)getArchiver("zip");
		archiver.setRecompressAddedZips(recompressZippedFiles);
		return archiver;
	}

}
