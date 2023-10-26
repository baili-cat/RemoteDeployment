package com.baili.sharingPlatform.common.ssh;


import com.jcraft.jsch.*;
import com.baili.sharingPlatform.common.utils.Sleeper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author baili
 * @date 2022年04月02日3:02 下午
 */
public class ExecExecutor {

    private static final int STATUS_OK = 0;
    private static final ScheduledExecutorService CHANNEL_MONITOR_SCHEDULED = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("ChannelTimeoutMonitor-"));

    private Session session;
    private int connectTimeout;
    private Duration commandTimeout = Duration.ofMinutes(5);
    private Map<String, String> envs = new HashMap<>();

    public ExecExecutor(Session session, int connectTimeout) {
        this.session = session;
        this.connectTimeout = connectTimeout;
    }

    public Duration getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(Duration commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, String> envs) {
        this.envs = envs;
    }

    public ChannelExec openChannel() throws JSchException {
        return (ChannelExec) session.openChannel("exec");
    }

    /**
     * 执行shell命令
     *
     * @param command 命令内容
     */
    public ExecResult<Void> execute(String command) {
        return execute(command, null, null, null);
    }


    public ExecResult<Void> execute(List<String> command) {
        return execute(command, null);
    }

    /**
     * 执行shell命令
     *
     * @param command 命令内容
     * @param timeout 命令执行超时时间
     */
    public ExecResult<Void> execute(String command, Duration timeout) {
        return execute(command, timeout, null, null);
    }

    /**
     * 执行shell命令
     *
     * @param command   命令内容
     * @param converter output输出结果转换器
     */
    public <T> ExecResult<T> execute(String command, ResultConverter<T> converter) {
        return execute(command, null, converter, null);
    }

    /**
     * 执行shell命令
     *
     * @param command   命令内容
     * @param converter output输出结果转换器
     */
    public <T> ExecResult<T> execute(String command, Duration timeout, ResultConverter<T> converter) {
        return execute(command, timeout, converter, null);
    }

    /**
     * 切换java版本，支持交互式输入
     *TODO 优化while循环为监听器

     */
    //public <T> ExecResult<T> execute(List<String> command) {
    public String changeJavaVersion(String javaVersion) {
        ChannelShell channel = null;
        PrintStream commander = null;
        BufferedReader br = null;
        try {
            channel = (ChannelShell) session.openChannel("shell");
            InputStream in = channel.getInputStream();
            channel.setPty(true);
            channel.connect();
            OutputStream out = channel.getOutputStream();
            PrintWriter printWriter = new PrintWriter(out, true);
            printWriter.println("alternatives --config java");
            Thread.sleep(1000);
            printWriter.println(javaVersion);
            printWriter.println("exit");

            byte[] tmp = new byte[1024];
            while(true){

                while(in.available() > 0){
                    int i = in.read(tmp, 0, 1024);
                    if(i < 0) {break;}
                    String s = new String(tmp, 0, i);
                    if(s.indexOf("exit") >= 0){
                        out.write((" ").getBytes());
                        out.flush();
                    }
                    System.out.println(s);
                }
                if(channel.isClosed()){
                    System.out.println("exit-status:"+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception e){}

            }

            out.close();
            in.close();
            channel.disconnect();
            session.disconnect();

            return "success";

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Done_Stop";
    }



        /**
     * 执行shell命令
     *
     * @param command   命令内容
     * @param converter output输出结果转换器
     * @param timeout   命令执行超时时间
     */
    public <T> ExecResult<T> execute(String command, Duration timeout, ResultConverter<T> converter, ExecConfig config) {
        if (config == null) {
            config = new ExecConfig();
        }
        config.setSuccessExit(Optional.ofNullable(config.getSuccessExit()).orElse(new ArrayList<>()));
        config.getSuccessExit().add(STATUS_OK);

        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setInputStream(null);
            channel.setCommand(command);
            channel.setPty(config.isPty());
            // 环境变量设置
            Map<String, String> envs = new HashMap<>();
            if (this.getEnvs() != null) {
                envs.putAll(this.getEnvs());
            }
            if (config.getEnvs() != null) {
                envs.putAll(config.getEnvs());
            }
            envs.forEach(channel::setEnv);

            InputStream stdIn;
            InputStream errIn;
            OutputStream out = channel.getOutputStream();
            // 合并sdterr和stdout输出流
            if (config.isStreamMerge()) {
                PipedInputStream pipeIn = new PipedInputStream();
                PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
                channel.setOutputStream(pipeOut);
                channel.setErrStream(pipeOut);
                stdIn = pipeIn;
                errIn = new ByteArrayInputStream(new byte[0]);
            } else {
                stdIn = channel.getInputStream();
                errIn = channel.getExtInputStream();
            }
            channel.connect(connectTimeout);
            if (config.isPty() && StringUtils.isNotEmpty(config.getInputData())) {
                waitChannelAvailableRead(channel, stdIn, Duration.ofSeconds(5));
                out.write(config.getInputData().getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

            if (timeout == null) {
                timeout = commandTimeout;
            }
            // 启动执行超时监听器
            ChannelTimeoutMonitor monitor = startChannelMonitor(channel, timeout);
            String stdOut = StringUtils.trimToNull(readChannelInputStream(channel, stdIn));
            String errOut = StringUtils.trimToNull(readChannelInputStream(channel, errIn));
            monitor.cancel();

            int exitCode = channel.getExitStatus();
            if (monitor.isTimedout()) {
                return ExecResult.fail(new SshException(command, "Command execution timeout(" + timeout.toMillis() + "ms)"), exitCode, stdOut,
                        errOut);
            }

            if (!config.getSuccessExit().contains(channel.getExitStatus())) {
                return ExecResult.fail(new SshException(command, "Exited (" + exitCode + ")"), exitCode, stdOut, errOut);
            }
            if (converter == null) {
                return ExecResult.ok(null, exitCode, stdOut, errOut);
            }

            try {
                T value = converter.convert(stdOut);
                return ExecResult.ok(value, exitCode, stdOut, errOut);
            } catch (Throwable e) {
                return ExecResult.fail(new SshException(command, e), exitCode, stdOut, errOut);
            }
        } catch (Throwable e) {
            return ExecResult.fail(new SshException(command, e), -1, null, null);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    /**
     * 执行shell命令
     *TODO 有问题后续在调试
     * @param commands   命令内容,多条
     * @param timeout   命令执行超时时间
     */
    public <T> ExecResult<T> execute(List<String> commands, Duration timeout) {

        ChannelShell channel = null;
        InputStream in = null;
        OutputStream out = null;
        PrintStream commander = null;
        BufferedReader br = null;
        try {
            channel = (ChannelShell) session.openChannel("shell");
            //out = channel.getOutputStream();
            commander = new PrintStream(out, true, "UTF-8");
            channel.connect();
            for (String command : commands) {
                commander.println(command);
                Thread.sleep(1000);
            }
            commander.println("exit");
            commander.close();
            channel.setPty(true);
            in = channel.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            return ExecResult.ok(null, 0, null, null);

        } catch (Throwable e) {
            e.printStackTrace();
            return ExecResult.fail(new SshException(commands.toString(), e), -1, null, null);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }


    /**
     * 持续读取output输出流，直至Channel被关闭
     */
    private String readChannelInputStream(Channel channel, InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        StringWriter writer = new StringWriter();
        while (true) {
            // 在网络不稳定的情况下这里可能出现阻塞卡死, 需要上游调用方额外监视当前方法执行超时后，通过调用channel.close()结束阻塞
            while (in.available() > 0) {
                if (IOUtils.copy(reader, writer) < 0) {
                    break;
                }
            }
            if (channel.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                return writer.toString();
            }
            Sleeper.sleep(Duration.ofMillis(100));
        }
    }

    /**
     * 等待Channel首次接收服务器返回可读数据
     */
    private void waitChannelAvailableRead(Channel channel, InputStream in, Duration timeout) throws IOException {
        // 在网络不稳定的情况下 in.available() 这里可能出现阻塞卡死, 所以需要额外监视当前方法执行超时后，通过调用channel.close()结束阻塞
        ChannelTimeoutMonitor monitor = startChannelMonitor(channel, timeout);
        while (!channel.isClosed() && in.available() <= 0) {
            Sleeper.sleep(Duration.ofMillis(100));
        }
        monitor.cancel();
    }

    /**
     * 启动Channel超时监控
     */
    private ChannelTimeoutMonitor startChannelMonitor(Channel channel, Duration timeout) {
        ChannelTimeoutMonitor monitor = new ChannelTimeoutMonitor(channel);
        long nanos = timeout.toNanos();
        if (channel.isClosed() || nanos <= 0) {
            return monitor;
        }
        CHANNEL_MONITOR_SCHEDULED.schedule(monitor, nanos, TimeUnit.NANOSECONDS);
        return monitor;
    }



    private static class ChannelTimeoutMonitor implements Runnable {

        private final Channel channel;
        private boolean timedout;
        private boolean cancelled;

        public ChannelTimeoutMonitor(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            if (cancelled || channel.isClosed()) {
                return;
            }
            timedout = true;
            channel.disconnect();
        }

        public void cancel() {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public boolean isTimedout() {
            return timedout;
        }

    }
}

