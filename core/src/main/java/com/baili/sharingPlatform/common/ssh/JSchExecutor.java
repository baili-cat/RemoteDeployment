package com.baili.sharingPlatform.common.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;

/**
 * @author baili
 * @date 2020/10/29.
 */
public class JSchExecutor implements Closeable {

    private Session      session;
    private ExecExecutor execExecutor;
    private SftpExecutor sftpExecutor;
    private ServerConfig config;

    public JSchExecutor(ServerConfig config) throws JSchException {
        this.config = config;
        // 连接超时时间
        int connectTimeout = config.getConnectTimeout();
        String debugConnectTimeout = System.getProperty("jsch.debug.session.connectTimeout");
        if (StringUtils.isNotBlank(debugConnectTimeout)) {
            connectTimeout = Integer.parseInt(debugConnectTimeout);
        }

        session = openSession(config);
        execExecutor = new ExecExecutor(session, connectTimeout);
        sftpExecutor = new SftpExecutor(session, connectTimeout);
    }

    @Override
    public void close() {
        if (sftpExecutor != null) {
            sftpExecutor.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    private Session openSession(ServerConfig config) throws JSchException {
        JSch jsch = createJSchInstance(config);
        Session session = jsch.getSession(config.getUser(), config.getHost(), config.getPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey,password");
        session.setPassword(StringUtils.trimToNull(config.getPassword()));
        // TODO jsch session 连接超时时间设置
        // session.setTimeout((int)TimeUnit.MINUTES.toMillis(5));
        session.connect(config.getConnectTimeout());
        return session;
    }

    private JSch createJSchInstance(ServerConfig config) throws JSchException {
        JSch jsch = new JSch();
        byte[] passphrase = null;
        if (StringUtils.isNotBlank(config.getPassphrase())) {
            passphrase = config.getPassphrase().getBytes(StandardCharsets.UTF_8);
        }

        if (StringUtils.isNotBlank(config.getPrivateKeyData())) {
            if (StringUtils.isBlank(config.getPublicKeyData())) {
                throw new IllegalStateException("'publicKey' is required");
            }

            byte[] privateKey = config.getPrivateKeyData().getBytes(StandardCharsets.UTF_8);
            byte[] publicKey = config.getPublicKeyData().getBytes(StandardCharsets.UTF_8);
            jsch.addIdentity("privateKeyBytes", privateKey, publicKey, passphrase);
        } else if (StringUtils.isNotBlank(config.getPrivateKeyFile())) {
            if (StringUtils.isBlank(config.getPublicKeyFile())) {
                throw new IllegalStateException("'publicKeyFile' is required");
            }

            jsch.addIdentity(config.getPrivateKeyFile(), config.getPublicKeyFile(), passphrase);
        }
        return jsch;
    }

    public ServerConfig getConfig() {
        return config;
    }

    public ExecExecutor exec() {
        return execExecutor;
    }

    public SftpExecutor sftp() {
        return sftpExecutor;
    }
}

