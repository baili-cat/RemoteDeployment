package com.baili.sharingPlatform.common.ssh;


import com.google.common.collect.Lists;
import com.jcraft.jsch.SftpATTRS;
import com.baili.sharingPlatform.common.TestCaseException;
import com.baili.sharingPlatform.common.archiver.ArchiverUtils;
import com.baili.sharingPlatform.common.ssh.chain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.internal.Utils;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.baili.sharingPlatform.common.ssh.chain.FailPolicy.Ignore;
import static com.baili.sharingPlatform.common.ssh.chain.FailPolicy.Interrupt;

/**
 * @author baili
 * @date 2022年04月02日1:56 下午
 */
@Slf4j
public class CommonCommands {

    public static final Duration SECONDS_5 = Duration.ofSeconds(5);
    public static final Duration SECONDS_10 = Duration.ofSeconds(10);
    public static final Duration SECONDS_15 = Duration.ofSeconds(15);
    public static final Duration SECONDS_30 = Duration.ofSeconds(30);
    public static final Duration SECONDS_45 = Duration.ofSeconds(45);
    public static final Duration SECONDS_60 = Duration.ofSeconds(60);
    public static final Duration MINUTES_1 = Duration.ofMinutes(1);
    public static final Duration MINUTES_2 = Duration.ofMinutes(2);
    public static final Duration MINUTES_5 = Duration.ofMinutes(5);
    public static final Duration MINUTES_10 = Duration.ofMinutes(10);

    private static final List<String> SECURITY_PATHS = Lists.newArrayList("./*", "/", "/bin", "/boot", "/dev", "/etc", "/lib", "/lib64", "/proc",
            "/sbin", "/usr", "/var", "C:\\Users", ":");

    /**
     * 检测是否root用户
     */
    public static boolean checkRootUser(JSchExecutor executor) {
        if ("root".equals(executor.getConfig().getUser())) {
            return true;
        }

        ExecConfig config = ExecConfig.builder().streamMerge(false).build();
        ExecResult<String> execResult = executor.exec().execute("id -u", SECONDS_10, value -> value, config);
        if (!execResult.isSuccess()) {
            throw new TestCaseException("执行命令 \"id -u\" 检查是否为Root用户出错, ErrOut: " + execResult.getErrOut(), execResult.getError());
        }
        return "0".equals(execResult.getResult());
    }

    /**
     * 检测java是否安装
     */
    public static boolean checkJava(JSchExecutor executor) {
        String command = "java -version";
        ExecConfig config = ExecConfig.builder().streamMerge(false).build();
        ExecResult<Boolean> execResult = executor.exec().execute(command, Duration.ofSeconds(30), Boolean::valueOf, config);
        if (!execResult.isSuccess()) {
            throw new TestCaseException("执行命令 \"" + command + "\" 失败,java 未安装, ErrOut: " + execResult.getErrOut(), execResult.getError());
        }
        return execResult.getResult();
    }

    /**
     * linux执行命令
     */
    public static List<Command> buildCommand(String cmds) {
        List<Command> commands = new ArrayList<>();
        //默认30s,由于部署平台安装需要时间较久，改成了10分钟，后续可调整优化
        ExecCmd cmd = new ExecCmd("linux执行命令", Ignore, fmt("%s", cmds), MINUTES_10);
        commands.add(cmd);
        return commands;
    }

    /**
     * linux执行脚本
     */
    public static List<Command> buildStartServiceCommand(String Script) {
        List<Command> commands = new ArrayList<>();
        //默认30s,由于部署平台安装需要时间较久，改成了10分钟，后续可调整优化
        ExecCmd cmd = new ExecCmd("linux执行脚本", Ignore, fmt("sh %s", Script), MINUTES_10);
        commands.add(cmd);
        return commands;
    }

    /**
     * windows执行脚本
     */
    public static List<Command> buildStartServiceCommandWindows(String Script) {
        List<Command> commands = new ArrayList<>();
        ExecCmd cmd = new ExecCmd("windows执行脚本", Ignore, fmt("Start-Process " +
                        "-LoadUserProfile %s"
                , Script),
                MINUTES_10);
        commands.add(cmd);
        return commands;
    }

    /**
     * 目录检查命令
     */
    @SuppressWarnings("SameParameterValue")
    public static SftpCmd buildDirectoryCheckCommand(String name, String directory, boolean expect, FailPolicy failPolicy) {
        return new SftpCmd(name + "检查", failPolicy, "Check if the directory \"" + directory + "\" exists", (ctx, sftp) -> {
            SftpResult<SftpATTRS> result = sftp.stat(directory);
            if (!result.isSuccess()) {
                return result;
            }
            boolean exists = result.getResult() != null && result.getResult().isDir();
            if (expect && !exists) {
                throw new TestCaseException(name + "(" + directory + ")不存在");
            } else if (!expect && exists) {
                throw new TestCaseException(name + "(" + directory + ")已存在");
            }
            return SftpResult.ok();
        }, null);
    }

    /**
     * 文件检查命令
     */
    @SuppressWarnings("SameParameterValue")
    public static SftpCmd buildFileCheckCommand(String name, String file, boolean expect, FailPolicy failPolicy) {
        return new SftpCmd(name + "检查", failPolicy, "Check if the file \"" + file + "\" exists", (ctx, sftp) -> {
            SftpResult<SftpATTRS> result = sftp.stat(file);
            if (!result.isSuccess()) {
                return result;
            }
            boolean exists = result.getResult() != null && !result.getResult().isDir();
            if (expect && !exists) {
                throw new TestCaseException(name + "(" + file + ")不存在");
            } else if (!expect && exists) {
                throw new TestCaseException(name + "(" + file + ")已存在");
            }
            return SftpResult.ok();
        }, null);
    }

    /**
     * 上传文件到指定目录
     */
    public static Command buildFileUploadCommand(String name, String srcFile, String destDir, FailPolicy failPolicy, boolean failRollback) {
        String descr = fmt("Upload file \"%s\" to \"%s\"", srcFile, destDir);
        SftpCmd sftpCmd = new SftpCmd(name, failPolicy, descr, (ctx, sftp) -> sftp.put(srcFile, destDir, new SftpFileTransferProgressMonitor()));
        if (failRollback) {
            sftpCmd.setRollback(rollbackFile(srcFile));
        }
        return sftpCmd;
    }

    /**
     * 解压文件到指定目录windows
     */
    public static List<Command> buildArchiveFileDecompressionCommandsWindows(File file, String destDir) {
        List<Command> commands = new ArrayList<>();

        //String uploadDir = "/tmp/installationTools-upload-" + UUID.randomUUID();
        //commands.add(new ExecCmd("创建文件上传临时目录", Interrupt, fmt("mkdir -p %s", destDir), SECONDS_30, rollbackFile(destDir)));

        //String remoteFile = new File(uploadDir, file.getName()).getAbsolutePath();
        String filePath = destDir + file.getName();
        String ext = ArchiverUtils.getArchiveExt(filePath);
        switch (ext) {
            case "tar":
                commands.add(new ExecCmd("解压文件", Interrupt, fmt("tar -xf %s -C %s \n", filePath, destDir),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            case "tar.gz":
                commands.add(new ExecCmd("解压文件", Interrupt, fmt("tar -zxf %s -C %s \n", filePath, destDir),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            case "zip":
                commands.add(new ExecCmd("解压文件", Interrupt, fmt("unzip -o %s -d %s \n", filePath, destDir),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            default:
                throw new TestCaseException("文件格式(" + ext + ")不支持");
        }

        //commands.add(new ExecCmd("删除文件上传临时目录", Ignore, rmCmd(uploadDir), SECONDS_60));
        return commands;
    }

    /**
     * 解压文件到指定目录Solaris
     * aix不支持tar指定目录，所以先进入到目录解压
     */
    public static List<Command> buildArchiveFileDecompressionCommandsSolaris(File file, String destDir) {
        List<Command> commands = new ArrayList<>();

        //String uploadDir = "/tmp/installationTools-upload-" + UUID.randomUUID();
        commands.add(new ExecCmd("创建文件上传目录", Interrupt, fmt("mkdir -p %s", destDir), SECONDS_30, rollbackFile(destDir)));
        String filePath = destDir + file.getName();
        String ext = ArchiverUtils.getArchiveExt(filePath);
        switch (ext) {
            case "tar":
                commands.add(new ExecCmd("进入到部署目录并解压文件", Interrupt, fmt("cd %s;tar -xf %s", destDir, filePath),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            case "tar.gz":
                commands.add(new ExecCmd("进入到部署目录并解压文件", Interrupt, fmt("cd %s;tar -zxf %s", destDir, filePath),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            case "zip":
                commands.add(new ExecCmd("进入到部署目录并解压文件", Interrupt, fmt("cd %s;unzip -o %s", destDir, filePath),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            default:
                throw new TestCaseException("文件格式(" + ext + ")不支持");
        }

        //commands.add(new ExecCmd("删除文件上传临时目录", Ignore, rmCmd(uploadDir), SECONDS_60));
        return commands;
    }

    /**
     * 解压文件到指定目录Aix
     * aix不支持tar指定目录，所以先进入到目录解压
     */
    public static List<Command> buildArchiveFileDecompressionCommandsAix(File file, String destDir) {
        List<Command> commands = new ArrayList<>();

        //String uploadDir = "/tmp/installationTools-upload-" + UUID.randomUUID();
        commands.add(new ExecCmd("创建文件上传目录", Interrupt, fmt("mkdir -p %s", destDir), SECONDS_30, rollbackFile(destDir)));
        String filePath = destDir + file.getName();
        String ext = ArchiverUtils.getArchiveExt(filePath);
        switch (ext) {
            //case "tar":
            //    commands.add(new ExecCmd("进入到部署目录并解压文件", Interrupt, fmt("cd %s;tar -xf %s", destDir, filePath),
            //            MINUTES_5));// rollbackFile(destDir)
            //    break;
            case "tar.gz":
                commands.add(new ExecCmd("进入到部署目录并解压文件", Interrupt, fmt("gunzip < %s | tar xvf - -C %s",
                        filePath, destDir),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            //case "zip":
            //    commands.add(new ExecCmd("进入到部署目录并解压文件", Interrupt, fmt("cd %s;unzip -o %s", destDir, filePath),
            //            MINUTES_5));// rollbackFile(destDir)
            //    break;
            default:
                throw new TestCaseException("文件格式(" + ext + ")不支持");
        }

        //commands.add(new ExecCmd("删除文件上传临时目录", Ignore, rmCmd(uploadDir), SECONDS_60));
        return commands;
    }

    /**
     * 解压文件到指定目录linux
     */
    public static List<Command> buildArchiveFileDecompressionCommands(File file, String destDir) {
        List<Command> commands = new ArrayList<>();

        //String uploadDir = "/tmp/installationTools-upload-" + UUID.randomUUID();
        commands.add(new ExecCmd("创建文件上传临时目录", Interrupt, fmt("mkdir -p %s", destDir), SECONDS_30, rollbackFile(destDir)));

        String filePath = destDir + file.getName();
        String ext = ArchiverUtils.getArchiveExt(filePath);
        switch (ext) {
            case "tar":
                commands.add(new ExecCmd("解压文件", Interrupt, fmt("tar -xf %s -C %s", filePath, destDir),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            case "tar.gz":
                commands.add(new ExecCmd("解压文件", Interrupt, fmt("tar -zxf %s -C %s", filePath, destDir),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            case "zip":
                commands.add(new ExecCmd("解压文件", Interrupt, fmt("unzip -o %s -d %s", filePath, destDir),
                        MINUTES_5));// rollbackFile(destDir)
                break;
            default:
                throw new TestCaseException("文件格式(" + ext + ")不支持");
        }

        //commands.add(new ExecCmd("删除文件上传临时目录", Ignore, rmCmd(uploadDir), SECONDS_60));
        return commands;
    }

    /**
     * 上传文件到远程指定目录
     */
    public static List<Command> buildFileRemoteUploadCommands(File file, String deistDir) {
        List<Command> commands = new ArrayList<>();
        //
        ////String uploadDir = "/tmp/installationTools-upload-" + UUID.randomUUID();
        //commands.add(new ExecCmd("创建文件上传目录", Interrupt, fmt("mkdir -p %s", deistDir), SECONDS_30, rollbackFile(deistDir)));

        String descr = fmt("Upload file \"%s\" to \"%s\"", file, deistDir);
        commands.add(new SftpCmd("上传文件", Interrupt, descr,
                (ctx, sftp) -> sftp.put(file.getAbsolutePath(), deistDir, new SftpFileTransferProgressMonitor())));
        return commands;
    }

    /**
     * linux远程wget下载文件并到指定目录
     */
    public static List<Command> wgetArchiveFileUploadCommands(String packageUrl, String destDir, Boolean isDecompress) {
        List<Command> commands = new ArrayList<>();
        //commands.add(new ExecCmd("创建文件上传临时目录", Interrupt, fmt("mkdir -p %s", destDir), SECONDS_30, rollbackFile(destDir)));
        //wget：-b后台执行，-q关闭日志打印，-N只有目标文件比本地新才下载，-P指定目录
        destDir = StringUtils.removeEnd(destDir, "/") + "/";
        String descr = fmt("wget  -q -N -P %s %s", destDir, packageUrl);
        commands.add(new ExecCmd("下载文件到指定目录", Interrupt, descr, MINUTES_10));
        if (isDecompress) {
            String filePath = destDir + packageUrl.substring(packageUrl.lastIndexOf("/") + 1);
            String ext = ArchiverUtils.getArchiveExt(filePath);
            switch (ext) {
                case "tar":
                    commands.add(new ExecCmd("解压文件", Interrupt, fmt("cd %s;tar -xf %s", destDir, filePath),
                            SECONDS_60));
                    break;
                case "tar.gz":
                    commands.add(new ExecCmd("解压文件", Interrupt, fmt("cd %s;tar -zxf %s", destDir, filePath),
                            SECONDS_60));
                    break;
                case "zip":
                    commands.add(new ExecCmd("解压文件", Interrupt, fmt("cd %s;unzip -o %s", destDir, filePath),
                            SECONDS_60));
                    break;
                default:
                    throw new TestCaseException("文件格式(" + ext + ")不支持");
            }
        }
        return commands;
    }

    /**
     * Aix远程wget下载文件并到指定目录
     */
    public static List<Command> wgetArchiveFileUploadCommandsAix(String packageUrl, String destDir,
                                                               Boolean isDecompress) {
        List<Command> commands = new ArrayList<>();
        //commands.add(new ExecCmd("创建文件上传临时目录", Interrupt, fmt("mkdir -p %s", destDir), SECONDS_30, rollbackFile(destDir)));
        //wget：-b后台执行，-q关闭日志打印，-N只有目标文件比本地新才下载，-P指定目录
        destDir = StringUtils.removeEnd(destDir, "/") + "/";
        String descr = fmt("wget  -q -N -P %s %s", destDir, packageUrl);
        commands.add(new ExecCmd("下载文件到指定目录", Interrupt, descr, MINUTES_10));
        if (isDecompress) {
            String filePath = destDir + packageUrl.substring(packageUrl.lastIndexOf("/") + 1);
            String ext = ArchiverUtils.getArchiveExt(filePath);
            switch (ext) {
                //case "tar":
                //    commands.add(new ExecCmd("解压文件", Interrupt, fmt("gunzip < %s | tar xvf - -C %s",
                //            filePath, destDir),
                //            SECONDS_60));
                //    break;
                case "tar.gz":
                    commands.add(new ExecCmd("解压文件", Interrupt, fmt("gunzip < %s | tar xvf - -C %s",
                            filePath, destDir),
                            SECONDS_60));
                    break;
                //case "zip":
                //    commands.add(new ExecCmd("解压文件", Interrupt, fmt("gunzip < %s | tar xvf - -C %s",
                //            filePath, destDir),
                //            SECONDS_60));
                //    break;
                default:
                    throw new TestCaseException("文件格式(" + ext + ")不支持");
            }
        }
        return commands;
    }

    /**
     * 替换指定目录下的文件内容
     * 直接替换fmt("sed -i s#.*%s.*#%s#g %s ", key,key + "=" + KeyValue.get(key), filePath)
     * 存在则替换，不存在则新增：
     */

    public static List<Command> buildReplaceFileContentCommand(List<Map<String, String>> replaceKeyValue,
                                                               String filePath
    ) {

        if (Utils.isStringEmpty(filePath) || replaceKeyValue.isEmpty()) {
            throw new TestCaseException("filePath or replaceValue is empty");
        }
        List<Command> commands = new ArrayList<>();
        //示例：PERFMA_APP_CODE 整行替换为PERFMA_APP_CODE=Init
        for (Map<String, String> KeyValue : replaceKeyValue) {
            for (String key : KeyValue.keySet()) {
                //兼容替换参数中有是路径包含"/"的情况：原命令：sed -i s/.*%s.*$/%s/g
                //替换值格式：test=test1  ,按照test替换后会变成 ddd=ttttt,ddd和ttttt都要自己用;
                //String descr = fmt("sed -i s#.*%s.*#%s#g %s ", key,
                //        key + "=" + KeyValue.get(key), filePath);
                //TODO 已验证通过，注意sed命令拼接空格，不然容易出错,如果value中有&时需要转义，已兼容 '/'、'&'
                //新增支持需要替换的指定字符不存在就新增
                //示例：grep -rl spring.datasource.url /data/service/xcenterClient/conf/application.properties
                // | xargs sed -i 's#.*spring.datasource.url.*#spring.datasource.url=jdbc:mysql://10.10.200.112:3306/baili_mock_xcenter?useUnicode=true\&characterEncoding=utf-8\&useSSL=false\&rewriteBatchedStatements=true\&allowPublicKeyRetrieval=true#g'
                // || sed -i '$a\spring.datasource.url=jdbc:mysql://10.10.200.112:3306/baili_mock_xcenter?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true' /data/service/xcenterClient/conf/application.properties
                String descr =
                        "grep -rl " + key + " "+ filePath + " | xargs sed -i " + "'s#.*" + key + ".*#" + key + "=" + StringUtils.replace(KeyValue.get(key),"&","\\&") +
                                "#g' || sed -i '$a\\" + key + "=" + KeyValue.get(key) + "' " + filePath;
                //只适配linux不适配windows，所以考虑脚本适配
                //String descr = fmt("grep -nm1 %s %s | awk -F : '{print$1}' | xargs -I {} sed -i {}s/.*%s.*$/%s/g %s",
                //                key,filePath,key,
                //        key + "=" + KeyValue.get(key), filePath);
                commands.add(new ExecCmd("替换指定文件下内的值", Ignore, descr, SECONDS_30));
            }
        }

        return commands;
    }

    /**
     * 替换指定docker-compose模板文件内容，指定内容包括{}
     */

    public static List<Command> buildReplaceDockerCopmposeContentCommand(List<Map<String, String>> replaceKeyValue,
                                                               String filePath
    ) {

        if (Utils.isStringEmpty(filePath) || replaceKeyValue.isEmpty()) {
            throw new TestCaseException("filePath or replaceValue is empty");
        }
        List<Command> commands = new ArrayList<>();
        //示例：PERFMA_APP_CODE 整行替换为PERFMA_APP_CODE=Init
        for (Map<String, String> KeyValue : replaceKeyValue) {
            for (String key : KeyValue.keySet()) {
                //兼容替换参数中有是路径包含"/"的情况：原命令：sed -i s/.*%s.*$/%s/g
                //替换值格式：test={test}  ,按照替换值为1234替换后会变成 test=1234;
                String descr =
                        "grep -rl {" + key +"} " + filePath + " | xargs sed -i " + "s#{" + key + "}#" + KeyValue.get(key) +
                                "#g";
                //只适配linux不适配windows，所以考虑脚本适配
                //String descr = fmt("grep -nm1 %s %s | awk -F : '{print$1}' | xargs -I {} sed -i {}s/.*%s.*$/%s/g %s",
                //                key,filePath,key,
                //        key + "=" + KeyValue.get(key), filePath);
                commands.add(new ExecCmd("替换指定文件下{}内的值", Ignore, descr, SECONDS_30));
            }
        }

        return commands;
    }
    /**
     * 替换指定docker-compose模板文件内容
     */

    public static List<Command> buildReplaceSpecifiedContentCommand(List<Map<String, String>> replaceKeyValue,
                                                                         String filePath
    ) {

        if (Utils.isStringEmpty(filePath) || replaceKeyValue.isEmpty()) {
            throw new TestCaseException("filePath or replaceValue is empty");
        }
        List<Command> commands = new ArrayList<>();
        //示例：PERFMA_APP_CODE 整行替换为PERFMA_APP_CODE=Init
        for (Map<String, String> KeyValue : replaceKeyValue) {
            for (String key : KeyValue.keySet()) {
                //兼容替换参数中有是路径包含"/"的情况：原命令：sed -i s/.*%s.*$/%s/g
                //替换值格式：test=:test  ,按照替换:test值为:cat替换后会变成 test=:cat;
                String descr =
                        "grep -rl "+ key + " " + filePath + " | xargs sed -i " + "s#" + key + "#" + KeyValue.get(key) +
                                "#g";
                commands.add(new ExecCmd("替换指定文件下匹配到的指定的值", Ignore, descr, SECONDS_30));
            }
        }

        return commands;
    }

    /**
     * 如果某个文件内不存在某个字符串则根据在该文件下的指定字符串内容的下一行添加指定内容
     */

    public static List<Command> buildAddSpecifiedContentCommand(List<Map<String, String>> replaceKeyValue,
                                                                    String filePath,List<String> replaceBasis
    ) {

        if (Utils.isStringEmpty(filePath) || replaceKeyValue.isEmpty()) {
            throw new TestCaseException("filePath or replaceValue is empty");
        }
        List<Command> commands = new ArrayList<>();
        int replaceBasisCount = 0;
        for (Map<String, String> KeyValue : replaceKeyValue) {
            for (String key : KeyValue.keySet()) {
                //grep -rL "volumes:" docker-compose.yml.bak | xargs sed -i '/volumes/a\            -
                // ./conf/baili-java-agent:/baili-java-agent'
                //grep -rL 是反向匹配，这里可以解决重复添加的问题
                //注意替换变量中的特殊字符，所以这里添加引号
                //String descr =
                //        "grep -rL \""+ KeyValue.get(key) + "\" " + filePath + " | xargs sed -i " + "'/" + key + "/a" +
                //                "\\" + KeyValue.get(key) + "'";

                String descr =
                        "grep -rL '"+ replaceBasis.get(replaceBasisCount) + "' " + filePath + " | xargs sed -i " + "'/" + key + "/a" +
                                "\\" + KeyValue.get(key) + "'";
                commands.add(new ExecCmd("替换指定文件下匹配到的行的下一行添加指定的值", Ignore, descr, SECONDS_30));
                replaceBasisCount++;
            }
        }

        return commands;
    }

    /**
     * 删除远程指定目录命令
     */
    public static List<Command> buildDeleteDirectoryCommand(String deistDir) {
        List<Command> commands = new ArrayList<>();

        commands.add(new ExecCmd("删除安装目录", Ignore, rmCmd(deistDir), SECONDS_60));
        return commands;
    }

    /**
     * 删除远程指定目录命令windows
     */
    public static List<Command> buildDeleteDirectoryCommandWindows(String deistDir) {
        List<Command> commands = new ArrayList<>();

        commands.add(new ExecCmd("删除安装目录", Ignore, rmCmdWindows(deistDir), SECONDS_60));
        return commands;
    }

    /**
     * 文件删除回滚
     *
     * @param path 路径
     */
    public static Rollback rollbackFile(String path) {
        return context -> context.getExecutor().exec().execute(rmCmd(path), SECONDS_60);
    }

    /**
     * 构建linux文件删除命令
     *
     * @param path 路径
     */
    public static String rmCmd(String path) {
        if (StringUtils.isBlank(path) || SECURITY_PATHS.contains(StringUtils.trim(path))) {
            throw new TestCaseException("远程删除路径[" + path + "]非法，禁止执行");
        }
        return fmt("rm -rf %s", path);
    }

    /**
     * 构建windows文件删除命令
     *
     * @param path 路径
     */
    public static String rmCmdWindows(String path) {
        if (StringUtils.isBlank(path) || SECURITY_PATHS.contains(StringUtils.trim(path))) {
            throw new TestCaseException("远程删除路径[" + path + "]非法，禁止执行");
        }
        return fmt("Remove-Item %s -recurse", path);
        //windows使用rm可能会出现进程卡死，后面解决
        //return fmt("rm %s -recurse", path);
    }

    /**
     * 构建目录创建命令
     *
     * @param path 路径
     */
    public static String mkdirCmd(String path) {
        return fmt("mkdir -p %s", path);
    }

    /**
     * 构建目录权限
     *
     * @param directoryInfo 路径
     */
    public static String chownCmd(DirectoryInfo directoryInfo) {
        return fmt("chown -R %s %s", directoryInfo.getGuid(), directoryInfo.getPath());
    }

    public static String fmt(String format, Object... args) {
        return String.format(format, args);
    }

}


