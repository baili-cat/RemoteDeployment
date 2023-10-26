/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver.utils;

import com.baili.sharingPlatform.common.archiver.ArchiverException;
import org.codehaus.plexus.logging.Logger;

import java.util.List;

/**
 *
 */
public final class TypeConversionUtils {

	private static final int U_R = 256;

	private static final int U_W = 128;

	private static final int U_X = 64;

	private static final int G_R = 32;

	private static final int G_W = 16;

	private static final int G_X = 8;

	private static final int W_R = 4;

	private static final int W_W = 2;

	private static final int W_X = 1;

	private TypeConversionUtils() {
	}

	public static String[] toStringArray(final List<String> list) {
		String[] result = null;

		if ((list != null) && !list.isEmpty()) {
			result = list.toArray(new String[list.size()]);
		}

		return result;
	}

	public static int modeToInt(final String mode, final Logger logger) throws ArchiverException {
		if (mode == null || mode.trim().length() < 1) {
			return -1;
		}

		try {
			final int value = Integer.parseInt(mode, 8);

			// discard sanity assessment here; we're pushing ahead.
			verifyModeSanity(value, logger);

			return value;
		} catch (final NumberFormatException e) {
			throw new ArchiverException("Failed to parse mode as an octal number: \'" + mode + "\'.", e);
		}
	}

	// the boolean return type is for people who want to make a decision based on the sanity
	// assessment.
	public static boolean verifyModeSanity(final int mode, final Logger logger) {
		final StringBuilder messages = new StringBuilder();

		messages.append("The mode: ").append(Integer.toString(mode, 8)).append(" contains nonsensical permissions:");

		boolean warn = false;

		// read-access checks.
		if (((mode & U_R) == 0) && ((mode & G_R) == G_R)) {
			messages.append("\n- Group has read access, but user does not.");
			warn = true;
		}

		if (((mode & U_R) == 0) && ((mode & W_R) == W_R)) {
			messages.append("\n- World has read access, but user does not.");
			warn = true;
		}

		if (((mode & G_R) == 0) && ((mode & W_R) == W_R)) {
			messages.append("\n- World has read access, but group does not.");
			warn = true;
		}
		// end read-access checks.

		// write-access checks.
		if (((mode & U_W) == 0) && ((mode & G_W) == G_W)) {
			messages.append("\n- Group has write access, but user does not.");
			warn = true;
		}

		if (((mode & U_W) == 0) && ((mode & W_W) == W_W)) {
			messages.append("\n- World has write access, but user does not.");
			warn = true;
		}

		if (((mode & G_W) == 0) && ((mode & W_W) == W_W)) {
			messages.append("\n- World has write access, but group does not.");
			warn = true;
		}
		// end write-access checks.

		// execute-/list-access checks.
		if (((mode & U_X) == 0) && ((mode & G_X) == G_X)) {
			messages.append("\n- Group has execute/list access, but user does not.");
			warn = true;
		}

		if (((mode & U_X) == 0) && ((mode & W_X) == W_X)) {
			messages.append("\n- World has execute/list access, but user does not.");
			warn = true;
		}

		if (((mode & G_X) == 0) && ((mode & W_X) == W_X)) {
			messages.append("\n- World has execute/list access, but group does not.");
			warn = true;
		}
		// end execute-/list-access checks.

		if (warn) {
			logger.warn(messages.toString());
		}

		return !warn;
	}

}
