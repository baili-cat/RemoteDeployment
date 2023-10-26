/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver.phase;

import com.baili.sharingPlatform.common.archiver.ArchiverException;
import com.baili.sharingPlatform.common.archiver.model.ArchiverSource;
import com.baili.sharingPlatform.common.archiver.model.FileItem;
import com.baili.sharingPlatform.common.archiver.utils.ArchiverFileUtils;
import com.baili.sharingPlatform.common.archiver.utils.TypeConversionUtils;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.codehaus.plexus.components.io.resources.ResourceFactory.createResource;

/**
 * Handles the top-level &lt;files/&gt; section of the assembly descriptor.
 */
public class FileItemArchiverPhase extends AbstractLogEnabled implements ArchiverPhase {
    @Override
    public void execute(final Archiver archiver, final ArchiverSource archiverSource) throws ArchiverException {
        final List<FileItem> fileList = archiverSource.getFiles();
        final File basedir = archiverSource.getBasedir();

        for (final FileItem fileItem : fileList) {
            String destName = fileItem.getDestName();

            final String sourcePath;
            if (StringUtils.isNotBlank(fileItem.getSource())) {
                sourcePath = StringUtils.trim(fileItem.getSource());
            } else {
                throw new ArchiverException("Misconfigured file: specify destName when using sources");
            }

            // ensure source file is in absolute path for reactor build to work
            File source = new File(sourcePath);

            // save the original sourcefile's name, because filtration may
            // create a temp file with a different name.
            final String sourceName = source.getName();

            if (!ArchiverFileUtils.isAbsolutePath(source)) {
                source = new File(basedir, sourcePath);
            }
            if (StringUtils.isBlank(destName)) {
                destName = sourceName;
            }

            final String outputDirectory = ArchiverFileUtils.getOutputDirectory(fileItem.getOutputDirectory());
            String target;

            // omit the last char if ends with / or \\
            if (outputDirectory.endsWith("/") || outputDirectory.endsWith("\\")) {
                target = outputDirectory + destName;
            } else if (outputDirectory.length() < 1) {
                target = destName;
            } else {
                target = outputDirectory + "/" + destName;
            }

            try {
                PlexusIoResource restoUse = createResource(source, archiverSource.getTransformer());
                int mode = TypeConversionUtils.modeToInt(fileItem.getFileMode(), getLogger());
                archiver.addResource(restoUse, target, mode);
            } catch (final org.codehaus.plexus.archiver.ArchiverException | IOException e) {
                throw new ArchiverException("Error adding file to archive: " + e.getMessage(), e);
            }
        }
    }
}

