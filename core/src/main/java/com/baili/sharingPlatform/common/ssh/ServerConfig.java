package com.baili.sharingPlatform.common.ssh;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

/**
 * @author baili
 * @date 2020/10/29.
 */
public class ServerConfig {
    @ApiModelProperty(value = "操作系统类型:", example = "linux、windows")
    private String osType;
    @NotEmpty(message = "目标ip不能为空")
    private String host;
    @ApiModelProperty(value = "内部使用不需要配置", example = "22")
    @NotEmpty(message = "端口号不能为空")
    private int port;
    @NotEmpty(message = "用户名不能为空")
    private String user;
    @NotEmpty(message = "密码不能为空")
    private String password;
    @ApiModelProperty(value = "私钥", hidden = true)
    private String privateKeyData;
    @ApiModelProperty(value = "公钥", hidden = true)
    private String publicKeyData;
    @ApiModelProperty(value = "公钥文件", hidden = true)
    private String publicKeyFile;
    @ApiModelProperty(value = "私钥文件", hidden = true)
    private String privateKeyFile;
    @ApiModelProperty(value = "没啥用暂时保留", hidden = true)
    private String passphrase;
    private int connectTimeout = 5000;// 默认连接超时时间5秒

    public String  getOsType() { return  osType;}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrivateKeyData() {
        return privateKeyData;
    }

    public void setPrivateKeyData(String privateKeyData) {
        this.privateKeyData = privateKeyData;
    }

    public String getPublicKeyData() {
        return publicKeyData;
    }

    public void setPublicKeyData(String publicKeyData) {
        this.publicKeyData = publicKeyData;
    }

    public String getPublicKeyFile() {
        return publicKeyFile;
    }

    public void setPublicKeyFile(String publicKeyFile) {
        this.publicKeyFile = publicKeyFile;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
