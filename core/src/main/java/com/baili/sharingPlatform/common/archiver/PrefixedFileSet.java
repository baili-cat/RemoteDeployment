/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver;

import org.codehaus.plexus.archiver.FileSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.functions.InputStreamTransformer;

import java.io.File;

/**
 *
 */
class PrefixedFileSet implements FileSet {

	private final static FileMapper[] EMPTY_FILE_MAPPERS_ARRAY = new FileMapper[0];

	private final String rootPrefix;

	private final FileSet fileSet;

	private final FileSelector[] selectors;

	/**
	 * @param fileSet The file set.
	 * @param rootPrefix The root prefix
	 * @param selectors The file selectors.
	 */
	PrefixedFileSet(final FileSet fileSet, final String rootPrefix, final FileSelector[] selectors) {
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
	static FileSelector[] combineSelectors(FileSelector[] first, FileSelector[] second) {
		if ((first != null) && (second != null)) {
			final FileSelector[] temp = new FileSelector[first.length + second.length];

			System.arraycopy(first, 0, temp, 0, first.length);
			System.arraycopy(second, 0, temp, first.length, second.length);

			first = temp;
		} else if ((first == null) && (second != null)) {
			first = second;
		}

		return first;
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
		FileSelector[] sel = fileSet.getFileSelectors();
		final FileSelector[] selectors1 = selectors;
		return combineSelectors(sel, selectors1);
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
		if (prefix == null) {
			return rootPrefix;
		}

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getDirectory() {
		return fileSet.getDirectory();
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