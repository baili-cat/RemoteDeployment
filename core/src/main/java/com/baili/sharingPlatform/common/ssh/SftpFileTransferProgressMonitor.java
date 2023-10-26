/*
 * Created by baili on 2021/04/16.
 */
package com.baili.sharingPlatform.common.ssh;

import com.jcraft.jsch.SftpProgressMonitor;
import com.baili.sharingPlatform.common.utils.DateUtils;
import com.baili.sharingPlatform.common.utils.SizeUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author baili
 * @date 2021/04/16.
 */
@Slf4j
public class SftpFileTransferProgressMonitor implements SftpProgressMonitor {

	private long   startTime;
	private long   totalBytes;// 传输文件总字节数
	private long   transferedBytes;// 已传输文件字节数
	private long   lastTimeMillis;// 上一次传输进度输出时间戳
	private String dest;
	private String opName;
	private int    op;

	@Override
	public void init(int op, String src, String dest, long max) {
		this.startTime = System.currentTimeMillis();
		this.totalBytes = max;
		this.dest = dest;
		this.op = op;
		this.opName = (op == SftpProgressMonitor.PUT) ? "Upload" : "Download";
		log.info("Begin {} from {} ({}) to {}", opName.toLowerCase(), src, SizeUtils.convert(max), dest);
	}

	@Override
	public boolean count(long count) {
		this.transferedBytes += count;
		// 每隔2秒输出一次传输进度
		if (System.currentTimeMillis() - lastTimeMillis > 2000) {
			lastTimeMillis = System.currentTimeMillis();
			float transferedPercentage = (float)(this.transferedBytes) / (this.totalBytes) * 100.0f;
			log.info("{}ing {} remote: {} ({}% {}/{})", opName, (op == SftpProgressMonitor.PUT ? "to" : "from"), dest,
					Math.ceil(transferedPercentage), SizeUtils.convert(this.transferedBytes), SizeUtils.convert(this.totalBytes));
		}
		return true;
	}

	@Override
	public void end() {
		long took = System.currentTimeMillis() - startTime;
		log.info("{}ed {} finished, took {} ({})", opName, dest, DateUtils.toDurationString(took), SizeUtils.convert(this.transferedBytes));
	}
}
