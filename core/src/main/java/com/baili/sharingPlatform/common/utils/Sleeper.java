package com.baili.sharingPlatform.common.utils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author baili
 * @date 2020/10/29.
 */
public final class Sleeper {

	private Sleeper() {
	}

	public static void sleep(Duration duration) {
		sleep(duration.toMillis(), TimeUnit.MILLISECONDS);
	}

	public static void sleep(long time, TimeUnit timeUnit) {
		try {
			Thread.sleep(TimeUnit.MILLISECONDS.convert(time, timeUnit));
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
}
