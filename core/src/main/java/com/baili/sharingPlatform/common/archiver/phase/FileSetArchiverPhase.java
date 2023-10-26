/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver.phase;

import com.baili.sharingPlatform.common.archiver.ArchiverException;
import com.baili.sharingPlatform.common.archiver.model.ArchiverSource;
import com.baili.sharingPlatform.common.archiver.model.FileSet;
import com.baili.sharingPlatform.common.archiver.task.AddFileSetsTask;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.List;

public class FileSetArchiverPhase extends AbstractLogEnabled implements ArchiverPhase {
	@Override
	public void execute(Archiver archiver, ArchiverSource archiverSource) throws ArchiverException {
		List<FileSet> fileSets = archiverSource.getFileSets();

		if ((fileSets != null) && !fileSets.isEmpty()) {
			AddFileSetsTask task = new AddFileSetsTask(fileSets);
			task.setLogger(getLogger());
			task.execute(archiver, archiverSource);
		}
	}
}
