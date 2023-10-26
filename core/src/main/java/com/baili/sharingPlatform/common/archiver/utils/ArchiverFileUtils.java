/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver.utils;


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
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 *
 */
public final class ArchiverFileUtils {

	private ArchiverFileUtils() {
		// no op
	}

	/**
	 * This method exists because {@link File#isAbsolute()} is not OS independent.
	 * <p>
	 * In addition to the check in {@link File#isAbsolute()} we will also test for a leading '/'.
	 *
	 * @return {@code true} if {@code File#isAbsolute()} or starts with a '/'
	 */
	public static boolean isAbsolutePath(File dir) {
		return dir != null && (dir.isAbsolute() || dir.getPath().startsWith("\\")); // on Win* platforms
	}

	/**
	 * ORDER OF INTERPOLATION PRECEDENCE:
	 * <ol>
	 * <li>prefixed with "module.", if moduleProject != null
	 * <ol>
	 * <li>Artifact instance for module, if moduleArtifact != null</li>
	 * <li>ArtifactHandler instance for module, if moduleArtifact != null</li>
	 * <li>MavenProject instance for module</li>
	 * </ol>
	 * </li>
	 * <li>prefixed with "artifact."
	 * <ol>
	 * <li>Artifact instance</li>
	 * <li>ArtifactHandler instance for artifact</li>
	 * <li>MavenProject instance for artifact</li>
	 * </ol>
	 * </li>
	 * <li>prefixed with "pom." or "project."
	 * <ol>
	 * <li>MavenProject instance from current build</li>
	 * </ol>
	 * </li>
	 * <li>no prefix, using main project instance
	 * <ol>
	 * <li>MavenProject instance from current build</li>
	 * </ol>
	 * </li>
	 * <li>Support for special expressions, like ${dashClassifier?}</li>
	 * <li>user-defined properties from the command line</li>
	 * <li>properties from main project</li>
	 * <li>system properties, from the MavenSession instance (to support IDEs)</li>
	 * <li>environment variables.</li>
	 * </ol>
	 */

	@Nonnull
	public static String fixRelativeRefs(@Nonnull String src) {
		String value = src;

		String[] separators = {"/", "\\"};

		String finalSep = null;
		for (String sep : separators) {
			if (value.endsWith(sep)) {
				finalSep = sep;
			}

			if (value.contains("." + sep)) {
				List<String> parts = new ArrayList<>();
				parts.addAll(Arrays.asList(value.split(sep.replace("\\", "\\\\"))));

				for (ListIterator<String> it = parts.listIterator(); it.hasNext(); ) {
					String part = it.next();
					if (".".equals(part)) {
						it.remove();
					} else if ("..".equals(part)) {
						it.remove();
						if (it.hasPrevious()) {
							it.previous();
							it.remove();
						}
					}
				}

				value = StringUtils.join(parts.iterator(), sep);
			}
		}

		if (finalSep != null && value.length() > 0 && !value.endsWith(finalSep)) {
			value += finalSep;
		}

		return value;
	}

	public static String getOutputDirectory(final String output) throws ArchiverException {
		String value = output;
		if (value == null) {
			value = "";
		}

		if ((value.length() > 0) && !value.endsWith("/") && !value.endsWith("\\")) {
			value += "/";
		}

		if ((value.length() > 0) && (value.startsWith("/") || value.startsWith("\\"))) {
			value = value.substring(1);
		}

		value = StringUtils.replace(value, "//", "/");
		value = StringUtils.replace(value, "\\\\", "\\");
		value = fixRelativeRefs(value);

		return value;
	}
}
