/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver;

import org.codehaus.plexus.archiver.ArchivedFileSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.functions.InputStreamTransformer;

import java.io.File;

/**
 *
 */
class PrefixedArchivedFileSet implements ArchivedFileSet {

	private final static FileMapper[] EMPTY_FILE_MAPPERS_ARRAY = new FileMapper[0];

	private final String rootPrefix;

	private final ArchivedFileSet fileSet;

	private final FileSelector[] selectors;

	/**
	 * @param fileSet The archived file set.
	 * @param rootPrefix The root prefix.
	 * @param selectors The file selectors.
	 */
	PrefixedArchivedFileSet(ArchivedFileSet fileSet, String rootPrefix, FileSelector[] selectors) {
		this.fileSet = fileSet;
		this.selectors = selectors;

		if (rootPrefix.length() > 0 && !rootPrefix.endsWith("/")) {
			this.rootPrefix = rootPrefix + "/";
		} else {
			this.rootPrefix = rootPrefix;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getArchive() {
		return fileSet.getArchive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getExcludes() {
		return fileSet.getExcludes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileSelector[] getFileSelectors() {
		return PrefixedFileSet.combineSelectors(fileSet.getFileSelectors(), selectors);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getIncludes() {
		return fileSet.getIncludes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrefix() {
		String prefix = fileSet.getPrefix();
		if (prefix.startsWith("/")) {
			if (prefix.length() > 1) {
				prefix = prefix.substring(1);
			} else {
				prefix = "";
			}
		}

		return rootPrefix + prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCaseSensitive() {
		return fileSet.isCaseSensitive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIncludingEmptyDirectories() {
		return fileSet.isIncludingEmptyDirectories();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUsingDefaultExcludes() {
		return fileSet.isUsingDefaultExcludes();
	}

	@Override
	public InputStreamTransformer getStreamTransformer() {
		return fileSet.getStreamTransformer();
	}

	@Override
	public FileMapper[] getFileMappers() {
		return EMPTY_FILE_MAPPERS_ARRAY;
	}
}
