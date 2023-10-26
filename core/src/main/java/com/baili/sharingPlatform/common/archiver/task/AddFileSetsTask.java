/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver.task;


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

import com.baili.sharingPlatform.common.archiver.ArchiverException;
import com.baili.sharingPlatform.common.archiver.model.ArchiverSource;
import com.baili.sharingPlatform.common.archiver.model.FileSet;
import com.baili.sharingPlatform.common.archiver.utils.ArchiverFileUtils;
import com.baili.sharingPlatform.common.archiver.utils.TypeConversionUtils;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class AddFileSetsTask {

	private final List<FileSet> fileSets;

	private Logger logger;

	public AddFileSetsTask(final List<FileSet> fileSets) {
		this.fileSets = fileSets;
	}

	public AddFileSetsTask(final FileSet... fileSets) {
		this.fileSets = new ArrayList<>(Arrays.asList(fileSets));
	}

	public void execute(final Archiver archiver, final ArchiverSource archiverSource) throws ArchiverException {
		// don't need this check here. it's more efficient here, but the logger is not actually
		// used until addFileSet(..)...and the check should be there in case someone extends the
		// class.
		// checkLogger();

		for (final FileSet fileSet : fileSets) {
			addFileSet(fileSet, archiver, archiverSource);
		}
	}

	void addFileSet(final FileSet fileSet, final Archiver archiver, final ArchiverSource archiverSource) throws ArchiverException {
		// throw this check in just in case someone extends this class...
		checkLogger();

		final File basedir = archiverSource.getBasedir();

		String destDirectory = fileSet.getOutputDirectory();

		if (destDirectory == null) {
			destDirectory = fileSet.getDirectory();
		}

		destDirectory = ArchiverFileUtils.getOutputDirectory(destDirectory);

		File fileSetDir = getFileSetDirectory(fileSet, basedir);

		if (fileSetDir.exists()) {
			if (fileSetDir.getPath().equals(File.separator)) {
				throw new ArchiverException("Your assembly descriptor specifies a directory of " + File.separator
						+ ", which is your *entire* file system.\nThese are not the files you are looking for");
			}
			final AddDirectoryTask task = new AddDirectoryTask(fileSetDir, archiverSource.getTransformer());

			final int dirMode = TypeConversionUtils.modeToInt(fileSet.getDirectoryMode(), logger);
			if (dirMode != -1) {
				task.setDirectoryMode(dirMode);
			}

			final int fileMode = TypeConversionUtils.modeToInt(fileSet.getFileMode(), logger);
			if (fileMode != -1) {
				task.setFileMode(fileMode);
			}

			task.setUseDefaultExcludes(fileSet.isUseDefaultExcludes());
			task.setExcludes(fileSet.getExcludes());
			task.setIncludes(fileSet.getIncludes());
			task.setOutputDirectory(destDirectory);

			task.execute(archiver);
		}
	}

	private File getFileSetDirectory(final FileSet fileSet, final File basedir) throws ArchiverException {
		String sourceDirectory = fileSet.getDirectory();

		if (sourceDirectory == null || sourceDirectory.trim().length() < 1) {
			sourceDirectory = basedir.getAbsolutePath();
		}

		File fileSetDir = new File(sourceDirectory);

		// If the file is not absolute then it's a subpath of the current project basedir
		// For OS compatibility we also must treat any path starting with "/" as absolute
		// as File#isAbsolute() returns false for /absolutePath under Windows :(
		// Note that in Windows an absolute path with / will be on the 'current drive'.
		// But I think we can live with this.
		if (!ArchiverFileUtils.isAbsolutePath(fileSetDir)) {
			fileSetDir = new File(basedir, sourceDirectory);
		}
		return fileSetDir;
	}

	private void checkLogger() {
		if (logger == null) {
			logger = new ConsoleLogger(Logger.LEVEL_INFO, "AddFileSetsTask-internal");
		}
	}

	public void setLogger(final Logger logger) {
		this.logger = logger;
	}

}

