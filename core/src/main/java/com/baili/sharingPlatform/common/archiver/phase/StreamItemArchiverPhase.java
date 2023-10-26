/*
 * Created by baili on 2021/01/14.
 */
package com.baili.sharingPlatform.common.archiver.phase;

import com.baili.sharingPlatform.common.archiver.ArchiverException;
import com.baili.sharingPlatform.common.archiver.model.ArchiverSource;
import com.baili.sharingPlatform.common.archiver.model.StreamItem;
import com.baili.sharingPlatform.common.archiver.utils.ArchiverFileUtils;
import com.baili.sharingPlatform.common.archiver.utils.TypeConversionUtils;
import lombok.SneakyThrows;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.components.io.resources.AbstractPlexusIoResource;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class StreamItemArchiverPhase extends AbstractLogEnabled implements ArchiverPhase {
    @Override
    public void execute(final Archiver archiver, final ArchiverSource archiverSource) throws ArchiverException {
        final List<StreamItem> streamList = archiverSource.getStreams();

        for (final StreamItem streamItem : streamList) {
            final InputStream source = streamItem.getSource();
            if (source == null) {
                throw new ArchiverException("source is null");
            }

            final String destName = streamItem.getDestName();
            if (StringUtils.isBlank(destName)) {
                throw new ArchiverException("destName is empty");
            }

            final String outputDirectory = ArchiverFileUtils.getOutputDirectory(streamItem.getOutputDirectory());
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
                PlexusIoResource restoUse = new PlexusIoStreamResource(source, source.available());
                int mode = TypeConversionUtils.modeToInt(streamItem.getFileMode(), getLogger());
                archiver.addResource(restoUse, target, mode);
            } catch (final org.codehaus.plexus.archiver.ArchiverException | IOException e) {
                throw new ArchiverException("Error adding file to archive: " + e.getMessage(), e);
            }
        }
    }

    static class PlexusIoStreamResource extends AbstractPlexusIoResource {

        private InputStream inputStream;

        @SneakyThrows
        public PlexusIoStreamResource(InputStream inputStream, int size) {
            super("<stream>", 0, size, true, false, true);
            this.inputStream = inputStream;
        }

        protected PlexusIoStreamResource(@Nonnull String name, long lastModified, long size, boolean isFile, boolean isDirectory,
                                         boolean isExisting) {
            super(name, lastModified, size, isFile, isDirectory, isExisting);
        }

        @Nonnull
        @Override
        public InputStream getContents() throws IOException {
            return inputStream;
        }

        @Override
        public URL getURL() throws IOException {
            return null;
        }
    }
}

