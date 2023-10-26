/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver;

import org.codehaus.plexus.archiver.*;
import org.codehaus.plexus.archiver.util.DefaultArchivedFileSet;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.components.io.resources.PlexusIoResourceCollection;
import org.codehaus.plexus.logging.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Delegating archiver implementation that supports:
 * <ul>
 * <li>dry-running (where the delegate archiver is never actually called)</li>
 * <li>prefixing (where all paths have a set global prefix prepended before addition)</li>
 * <li>duplication checks on archive additions (for archive-file path + prefix)</li>
 * </ul>
 *
 * @author jdcasey
 */
public class ProxyArchiver implements Archiver {

	private final Archiver delegate;

	private final ThreadLocal<Boolean> inPublicApi = new ThreadLocal<>();

	private final Logger logger;

	private final String assemblyWorkPath;

	private String rootPrefix;

	private boolean forced;
	/**
	 * @since 2.2
	 */
	private boolean useJvmChmod;

	public ProxyArchiver(final String rootPrefix, final Archiver delegate, final File assemblyWorkDir, final Logger logger) {
		this.rootPrefix = rootPrefix;
		this.delegate = delegate;
		this.assemblyWorkPath = assemblyWorkDir.getAbsolutePath().replace('\\', '/');
		this.logger = logger;
		if (!"".equals(rootPrefix) && !rootPrefix.endsWith("/")) {
			this.rootPrefix += "/";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addArchivedFileSet(@Nonnull final File archiveFile, final String prefix, final String[] includes, final String[] excludes) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultArchivedFileSet fs = new DefaultArchivedFileSet(archiveFile);

			fs.setIncludes(includes);
			fs.setExcludes(excludes);
			fs.setPrefix(rootPrefix + prefix);

			debug("Adding archived file-set in: " + archiveFile + " to archive location: " + fs.getPrefix());

			delegate.addArchivedFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	private void debug(final String message) {
		if ((logger != null) && logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addArchivedFileSet(@Nonnull final File archiveFile, final String prefix) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultArchivedFileSet fs = new DefaultArchivedFileSet(archiveFile);
			fs.setPrefix(rootPrefix + prefix);

			debug("Adding archived file-set in: " + archiveFile + " to archive location: " + fs.getPrefix());

			delegate.addArchivedFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addArchivedFileSet(final File archiveFile, final String[] includes, final String[] excludes) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultArchivedFileSet fs = new DefaultArchivedFileSet(archiveFile);
			fs.setIncludes(includes);
			fs.setExcludes(excludes);
			fs.setPrefix(rootPrefix);

			debug("Adding archived file-set in: " + archiveFile + " to archive location: " + fs.getPrefix());

			delegate.addArchivedFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addArchivedFileSet(@Nonnull final File archiveFile) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultArchivedFileSet fs = new DefaultArchivedFileSet(archiveFile);
			fs.setPrefix(rootPrefix);

			debug("Adding archived file-set in: " + archiveFile + " to archive location: " + fs.getPrefix());

			delegate.addArchivedFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDirectory(@Nonnull final File directory, final String prefix, final String[] includes, final String[] excludes) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultFileSet fs = new DefaultFileSet();

			fs.setDirectory(directory);
			fs.setIncludes(includes);
			fs.setExcludes(excludes);
			fs.setPrefix(rootPrefix + prefix);

			debug("Adding directory file-set in: " + directory + " to archive location: " + fs.getPrefix());

			doAddFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addSymlink(String symlinkName, String symlinkDestination) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.addSymlink(symlinkName, symlinkDestination);
		} finally {
			inPublicApi.set(null);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addSymlink(String symlinkName, int permissions, String symlinkDestination) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.addSymlink(symlinkName, permissions, symlinkDestination);
		} finally {
			inPublicApi.set(null);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDirectory(@Nonnull final File directory, final String prefix) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultFileSet fs = new DefaultFileSet();

			fs.setDirectory(directory);
			fs.setPrefix(rootPrefix + prefix);

			debug("Adding directory file-set in: " + directory + " to archive location: " + fs.getPrefix());

			doAddFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDirectory(@Nonnull final File directory, final String[] includes, final String[] excludes) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultFileSet fs = new DefaultFileSet();

			fs.setDirectory(directory);
			fs.setIncludes(includes);
			fs.setExcludes(excludes);
			fs.setPrefix(rootPrefix);

			debug("Adding directory file-set in: " + directory + " to archive location: " + fs.getPrefix());

			doAddFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDirectory(@Nonnull final File directory) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final DefaultFileSet fs = new DefaultFileSet();

			fs.setDirectory(directory);
			fs.setPrefix(rootPrefix);

			debug("Adding directory file-set in: " + directory + " to archive location: " + fs.getPrefix());

			doAddFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFile(@Nonnull final File inputFile, @Nonnull final String destFileName, final int permissions) {
		if (acceptFile(inputFile)) {
			inPublicApi.set(Boolean.TRUE);
			try {
				debug("Adding file: " + inputFile + " to archive location: " + rootPrefix + destFileName);

				delegate.addFile(inputFile, rootPrefix + destFileName, permissions);
			} finally {
				inPublicApi.set(null);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFile(@Nonnull final File inputFile, @Nonnull final String destFileName) {
		if (acceptFile(inputFile)) {
			inPublicApi.set(Boolean.TRUE);
			try {
				debug("Adding file: " + inputFile + " to archive location: " + rootPrefix + destFileName);

				delegate.addFile(inputFile, rootPrefix + destFileName);
			} finally {
				inPublicApi.set(null);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createArchive() throws IOException {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setForced(forced);
			delegate.createArchive();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDefaultDirectoryMode() {
		inPublicApi.set(Boolean.TRUE);
		try {
			return delegate.getDefaultDirectoryMode();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultDirectoryMode(final int mode) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setDefaultDirectoryMode(mode);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDefaultFileMode() {
		inPublicApi.set(Boolean.TRUE);
		try {
			return delegate.getDefaultFileMode();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultFileMode(final int mode) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setDefaultFileMode(mode);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getDestFile() {
		inPublicApi.set(Boolean.TRUE);
		try {
			return delegate.getDestFile();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDestFile(final File destFile) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setDestFile(destFile);
		} finally {
			inPublicApi.set(null);
		}
	}

	@Override
	@SuppressWarnings({"deprecation"})
	public Map<String, ArchiveEntry> getFiles() {
		inPublicApi.set(Boolean.TRUE);
		try {
			return delegate.getFiles();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getIncludeEmptyDirs() {
		inPublicApi.set(Boolean.TRUE);
		try {
			return delegate.getIncludeEmptyDirs();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIncludeEmptyDirs(final boolean includeEmptyDirs) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setIncludeEmptyDirs(includeEmptyDirs);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isForced() {
		inPublicApi.set(Boolean.TRUE);
		try {
			return delegate.isForced();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setForced(final boolean forced) {
		inPublicApi.set(Boolean.TRUE);
		try {
			this.forced = forced;
			delegate.setForced(forced);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSupportingForced() {
		inPublicApi.set(Boolean.TRUE);
		try {
			return delegate.isSupportingForced();
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDotFileDirectory(final File dotFileDirectory) {
		throw new UnsupportedOperationException("Undocumented feature of plexus-archiver; this is not yet supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addArchivedFileSet(final ArchivedFileSet fileSet) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final PrefixedArchivedFileSet fs = new PrefixedArchivedFileSet(fileSet, rootPrefix, null);

			debug("Adding archived file-set in: " + fileSet.getArchive() + " to archive location: " + fs.getPrefix());

			delegate.addArchivedFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	@Override
	public void addArchivedFileSet(ArchivedFileSet archivedFileSet, Charset charset) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final PrefixedArchivedFileSet fs = new PrefixedArchivedFileSet(archivedFileSet, rootPrefix, null);

			debug("Adding archived file-set in: " + archivedFileSet.getArchive() + " to archive location: " + fs.getPrefix());

			delegate.addArchivedFileSet(fs, charset);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFileSet(@Nonnull final FileSet fileSet) {
		inPublicApi.set(Boolean.TRUE);
		try {
			final PrefixedFileSet fs = new PrefixedFileSet(fileSet, rootPrefix, null);

			debug("Adding file-set in: " + fileSet.getDirectory() + " to archive location: " + fs.getPrefix());

			doAddFileSet(fs);
		} finally {
			inPublicApi.set(null);
		}
	}

	private void doAddFileSet(final FileSet fs) {
		final String fsPath = fs.getDirectory().getAbsolutePath().replace('\\', '/');

		if (fsPath.equals(assemblyWorkPath)) {
			logger.debug("SKIPPING fileset with source directory matching assembly working-directory: " + fsPath);
		} else if (assemblyWorkPath.startsWith(fsPath)) {
			final List<String> newEx = new ArrayList<>();
			if (fs.getExcludes() != null) {
				newEx.addAll(Arrays.asList(fs.getExcludes()));
			}

			final String workDirExclude = assemblyWorkPath.substring(fsPath.length() + 1);

			logger.debug("Adding exclude for assembly working-directory: " + workDirExclude + "\nFile-Set source directory: " + fsPath);

			newEx.add(workDirExclude);

			final List<String> newIn = new ArrayList<>();
			if (fs.getIncludes() != null) {
				for (final String include : fs.getIncludes()) {
					if (!include.startsWith(workDirExclude)) {
						newIn.add(include);
					}
				}
			}

			final DefaultFileSet dfs = new DefaultFileSet();

			dfs.setCaseSensitive(fs.isCaseSensitive());
			dfs.setDirectory(fs.getDirectory());
			dfs.setExcludes(newEx.toArray(new String[0]));
			dfs.setFileSelectors(fs.getFileSelectors());
			dfs.setIncludes(newIn.toArray(new String[0]));
			dfs.setIncludingEmptyDirectories(fs.isIncludingEmptyDirectories());
			dfs.setPrefix(fs.getPrefix());
			dfs.setStreamTransformer(fs.getStreamTransformer());

			delegate.addFileSet(dfs);
		} else {
			delegate.addFileSet(fs);
		}
	}

	private boolean acceptFile(final File inputFile) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addResource(final PlexusIoResource resource, final String destFileName, final int permissions) {
		File file = new File(resource.getName()); // zOMG.
		if (acceptFile(file)) {
			inPublicApi.set(Boolean.TRUE);
			try {
				delegate.addResource(resource, rootPrefix + destFileName, permissions);
			} finally {
				inPublicApi.set(null);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addResources(final PlexusIoResourceCollection resources) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.addResources(resources);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nonnull
	public ResourceIterator getResources() {
		return delegate.getResources();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDuplicateBehavior() {
		return delegate.getDuplicateBehavior();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDuplicateBehavior(final String duplicate) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setDuplicateBehavior(duplicate);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDirectoryMode() {
		return delegate.getDirectoryMode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDirectoryMode(final int mode) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setDirectoryMode(mode);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFileMode() {
		return delegate.getFileMode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFileMode(final int mode) {
		inPublicApi.set(Boolean.TRUE);
		try {
			delegate.setFileMode(mode);
		} finally {
			inPublicApi.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOverrideDirectoryMode() {
		return delegate.getOverrideDirectoryMode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOverrideFileMode() {
		return delegate.getOverrideFileMode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUseJvmChmod() {
		return useJvmChmod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUseJvmChmod(final boolean useJvmChmod) {
		this.useJvmChmod = useJvmChmod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIgnorePermissions() {
		return delegate.isIgnorePermissions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIgnorePermissions(final boolean ignorePermissions) {
		delegate.setIgnorePermissions(ignorePermissions);
	}

	@Override
	public Date getLastModifiedDate() {
		return delegate.getLastModifiedDate();
	}

	@Override
	public void setLastModifiedDate(Date lastModifiedDate) {
		delegate.setLastModifiedDate(lastModifiedDate);
	}

	@Override
	public void setFilenameComparator(Comparator<String> filenameComparator) {
		delegate.setFilenameComparator(filenameComparator);
	}

	@Override
	public void configureReproducible(Date outputTimestamp) {
		delegate.configureReproducible(outputTimestamp);
	}

	@Override
	public int getOverrideUid() {
		return delegate.getOverrideUid();
	}

	@Override
	public void setOverrideUid(int uid) {
		delegate.setOverrideUid(uid);
	}

	@Override
	public String getOverrideUserName() {
		return delegate.getOverrideUserName();
	}

	@Override
	public void setOverrideUserName(String userName) {
		delegate.setOverrideUserName(userName);
	}

	@Override
	public int getOverrideGid() {
		return delegate.getOverrideGid();
	}

	@Override
	public void setOverrideGid(int gid) {
		delegate.setOverrideGid(gid);
	}

	@Override
	public String getOverrideGroupName() {
		return delegate.getOverrideGroupName();
	}

	@Override
	public void setOverrideGroupName(String groupName) {
		delegate.setOverrideGroupName(groupName);
	}

}
