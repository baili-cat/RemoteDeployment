package com.baili.sharingPlatform.common.ssh;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * @author baili
 * @date 2020/10/29.
 */
public class SftpExecutor {

	private Session     session;
	private ChannelSftp channel;
	private int         connectTimeout;

	public SftpExecutor(Session session, int connectTimeout) {
		this.session = session;
		this.connectTimeout = connectTimeout;
	}

	void disconnect() {
		if (channel != null) {
			channel.disconnect();
		}
	}

	private ChannelSftp openChannel() throws JSchException {
		ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
		channel.connect(connectTimeout);
		return channel;
	}

	public ChannelSftp channel() {
		if (channel != null) {
			return channel;
		}
		synchronized (this) {
			if (channel != null) {
				return channel;
			}
			try {
				channel = openChannel();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return channel;
	}

	public SftpResult<Void> get(String src, String dest) {
		try {
			channel().get(src, dest);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("get: [src=%s, dest=%s]", src, dest), e));
		}
	}

	public SftpResult<Void> get(String src, String dest, SftpProgressMonitor monitor) {
		try {
			channel().get(src, dest, monitor);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("get: [src=%s, dest=%s, <monitor>]", src, dest), e));
		}
	}

	public SftpResult<Void> get(String src, OutputStream dest) {
		try {
			channel().get(src, dest);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("get: [src=%s, dest=<stream>]", src), e));
		}
	}

	public SftpResult<Void> get(String src, OutputStream dest, SftpProgressMonitor monitor) {
		try {
			channel().get(src, dest, monitor);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("get: [src=%s, dest=<stream>, <monitor>]", src), e));
		}
	}

	public SftpResult<Void> put(String src, String dest) {
		try {
			channel().put(src, dest);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("put: [src=%s, dest=%s]", src, dest), e));
		}
	}

	public SftpResult<Void> put(String src, String dest, SftpProgressMonitor monitor) {
		try {
			channel().put(src, dest, monitor);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("put: [src=%s, dest=%s, <monitor>]", src, dest), e));
		}
	}

	public SftpResult<Void> put(InputStream src, String dest) {
		try {
			channel().put(src, dest);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("put: [src=%s, dest=%s]", src, dest), e));
		}
	}

	public SftpResult<Void> put(InputStream src, String dest, SftpProgressMonitor monitor) {
		try {
			channel().put(src, dest, monitor);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("put: [src=<stream>, dest=%s, <monitor>]", dest), e));
		}
	}

	public SftpResult<Vector<ChannelSftp.LsEntry>> ls(String path) {
		try {
			//noinspection unchecked
			return SftpResult.ok(channel().ls(path));
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("ls: [path=%s]", path), e));
		}
	}

	public SftpResult<Void> rm(String path) {
		try {
			channel().rm(path);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("rm: [path=%s]", path), e));
		}
	}

	public SftpResult<Void> rmdir(String path) {
		try {
			channel().rmdir(path);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("rmdir: [path=%s]", path), e));
		}
	}

	public SftpResult<Void> mkdir(String path) {
		try {
			channel().mkdir(path);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("mkdir: [path=%s]", path), e));
		}
	}

	public SftpResult<Void> rename(String oldpath, String newpath) {
		try {
			channel().rename(oldpath, newpath);
			return SftpResult.ok();
		} catch (Exception e) {
			return SftpResult.fail(new SshException(String.format("rename: [oldpath=%s, newpath=%s]", oldpath, newpath), e));
		}
	}

	public SftpResult<SftpATTRS> stat(String path) {
		try {
			return SftpResult.ok(channel().stat(path));
		} catch (Exception e) {
			if (e instanceof SftpException) {
				if (((SftpException)e).id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					return SftpResult.ok(null);
				}
			}
			return SftpResult.fail(new SshException(String.format("stat: [path=%s]", path), e));
		}
	}

	public SftpResult<Boolean> exists(String path) {
		SftpResult<SftpATTRS> result = stat(path);
		if (result.isSuccess()) {
			return SftpResult.ok(result.getResult() != null);
		} else {
			return SftpResult.fail(result.getError());
		}
	}

}
