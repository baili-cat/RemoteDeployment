// =================== DO NOT EDIT THIS FILE ====================
// Generated by Modello 1.9.1,
// any modifications will be overwritten.
// ==============================================================

package com.baili.sharingPlatform.common.archiver.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.InputStream;

@Data
@SuperBuilder
@NoArgsConstructor
public class StreamItem {

	private InputStream source;

	private String outputDirectory;

	private String destName;

	/**
	 * Similar to a UNIX permission, sets the file mode
	 * of the files included.
	 * THIS IS AN OCTAL VALUE.
	 * Format: (User)(Group)(Other) where each
	 * component is a sum of Read = 4,
	 * Write = 2, and Execute = 1.  For example, the
	 * value 0644
	 * translates to User read-write, Group and Other
	 * read-only. The default value is 0644.
	 * <a
	 * href="http://www.onlamp.com/pub/a/bsd/2000/09/06/FreeBSD_Basics.html">(more
	 * on unix-style permissions)</a>
	 */
	private String fileMode;

}
