/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver.phase;

import com.baili.sharingPlatform.common.archiver.ArchiverException;
import com.baili.sharingPlatform.common.archiver.model.ArchiverSource;
import org.codehaus.plexus.archiver.Archiver;

/**
 * Handles one top-level section of the assembly descriptor, to determine which files to include in the assembly archive
 * for that section.
 */
public interface ArchiverPhase {

	/**
	 * Handle the associated section of the assembly descriptor.
	 *
	 * @param archiver The archiver used to create the assembly archive, to which files/directories/artifacts are
	 * added
	 * @param archiverSource The configuration for this assembly build, normally derived from the plugin that launched
	 * the assembly process.
	 */
	void execute(Archiver archiver, ArchiverSource archiverSource) throws ArchiverException;
}
