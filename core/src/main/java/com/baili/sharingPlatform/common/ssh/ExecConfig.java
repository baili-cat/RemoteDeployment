/*
 * Created by baili on 2022/04/13.
 */
package com.baili.sharingPlatform.common.ssh;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author baili
 * @date 2022/04/13.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class ExecConfig {

	/**
	 * 环境变量
	 */
	private Map<String, String> envs;
	/**
	 * 合并sdterr和stdout输出流
	 */
	private boolean             streamMerge = true;
	/**
	 * 打开伪终端模式
	 */
	private boolean             pty         = false;
	/**
	 * 输入数据，pty=true才生效
	 */
	private String              inputData;
	/**
	 * 需要识别为正常的退出码，默认为：0
	 */
	private List<Integer>       successExit = new ArrayList<>();

}
