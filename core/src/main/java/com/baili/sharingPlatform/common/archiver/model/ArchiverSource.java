/*
 * Created by baili on 2020/12/01.
 */
package com.baili.sharingPlatform.common.archiver.model;

import lombok.Data;
import org.codehaus.plexus.components.io.functions.InputStreamTransformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author baili
 * @date 2020/12/01.
 */
@Data
public class ArchiverSource {

	// 归档名称
	private String           finalName;
	// 归档格式
	private String           format;
	// 输入basedir目录
	private File             basedir;
	// 归档输出目录
	private File             archiveOutputDir;
	// 归档basedir
	private String           archiveBaseDir;
	// 归档是否添加basedir
	private boolean          includeArchiveBaseDir = true;
	// 归档文件集
	private List<FileSet>    fileSets;
	// 归档文件
	private List<FileItem>   files;
	// 归档文件流
	private List<StreamItem> streams;

	// 当使用zip方式压缩时，是否对归档文集中的(jar,zip etc)再次进行压缩
	private boolean                recompressZippedFiles = true;
	// 文件流处理器
	private InputStreamTransformer transformer;

	private String  tarLongFileMode   = "warn";
	private boolean dryRun;
	private boolean updateOnly        = false;
	private boolean ignorePermissions = false;
	private Integer overrideUid;
	private String  overrideUserName;
	private Integer overrideGid;
	private String  overrideGroupName;

	public File getWorkingDirectory() {
		return new File(archiveOutputDir, "/assembly/work");
	}

	/*public File getTemporaryRootDirectory() {
		return new File(outputDirectory, "/archive-tmp");
	}*/

	public List<FileSet> getFileSets() {
		if (this.fileSets == null) {
			this.fileSets = new ArrayList<>();
		}
		return this.fileSets;
	}

	public List<FileItem> getFiles() {
		if (this.files == null) {
			this.files = new ArrayList<>();
		}
		return this.files;
	}

	public List<StreamItem> getStreams() {
		if (this.streams == null) {
			this.streams = new ArrayList<>();
		}
		return this.streams;
	}

	public void addFileSet(FileSet fileSet) {
		getFileSets().add(fileSet);
	}

	public void addFile(FileItem fileItem) {
		getFiles().add(fileItem);
	}

	public void addStream(StreamItem streamItem) {
		getStreams().add(streamItem);
	}

}
