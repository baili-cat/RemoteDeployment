/*
 * Created by baili on 2021/09/15.
 */
package com.baili.sharingPlatform.service.ServiceXxlJob.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baili
 * @date 2021/09/15.
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class JobInfo {

	/**
	 * 调度状态：0-停止，1-运行
	 */
	private Integer triggerStatus;

	// ******************************基础配置******************************
	/**
	 * 任务Id
	 */
	private Long   id;
	/**
	 * 任务key (仅支持xxl-job-admin:2.3.0.2之后版本)
	 */
	private String jobKey;
	/**
	 * 执行器Id
	 */
	private Long   jobGroup;
	/**
	 * 任务描述
	 */
	private String jobDesc;
	/**
	 * 负责人
	 */
	private String author;
	/**
	 * 报警邮箱
	 */
	private String alarmEmail;

	// ******************************调度配置******************************
	/**
	 * 调度类型: NONE, CRON(默认), FIX_RATE
	 */
	private ScheduleType    scheduleType;
	/**
	 * 调度配置，值含义取决于调度类型
	 */
	private String          scheduleConf;
	/**
	 * 调度过期策略
	 */
	private MisfireStrategy misfireStrategy;

	// ******************************任务配置******************************
	/**
	 * 运行模式
	 */
	private GlueType      glueType;
	/**
	 * 任务Handler名称
	 */
	private String        executorHandler;
	/**
	 * 任务参数
	 */
	private String        executorParam;
	/**
	 * 路由策略
	 */
	private RouteStrategy executorRouteStrategy;
	/**
	 * 阻塞处理策略
	 */
	private BlockStrategy executorBlockStrategy;
	/**
	 * 任务超时时间(单位秒)
	 */
	private Integer       executorTimeout;
	/**
	 * 失败重试次数
	 */
	private Integer       executorFailRetryCount;

	// private Date addTime;
	// private Date updateTime;
	//
	// private String glueSource;        // GLUE源代码
	// private String glueRemark;        // GLUE备注
	// private Date   glueUpdatetime;    // GLUE更新时间
	//
	// private String childJobId;        // 子任务ID，多个逗号分隔
	//
	// private long triggerLastTime;    // 上次调度时间
	// private long triggerNextTime;    // 下次调度时间

	/**
	 * 调度类型
	 */
	public enum ScheduleType {
		NONE,
		/**
		 * Cron
		 */
		CRON,
		/**
		 * 固定速度
		 */
		FIX_RATE
	}

	/**
	 * 运行模式
	 */
	public enum GlueType {
		BEAN
	}

	/**
	 * 路由策略
	 */
	public enum RouteStrategy {

		/**
		 * 第一个
		 */
		FIRST,
		/**
		 * 最后一个
		 */
		LAST,
		/**
		 * 轮询
		 */
		ROUND,
		/**
		 * 随机
		 */
		RANDOM,
		/**
		 * 一致性HASH
		 */
		CONSISTENT_HASH,
		/**
		 * 最不经常使用
		 */
		LEAST_FREQUENTLY_USED,
		/**
		 * 最近最久未使用
		 */
		LEAST_RECENTLY_USED,
		/**
		 * 故障转移
		 */
		FAILOVER,
		/**
		 * 忙碌转移
		 */
		BUSYOVER,
		/**
		 * 分片广播
		 */
		SHARDING_BROADCAST;

	}

	/**
	 * 调度过期策略
	 */
	public enum MisfireStrategy {

		/**
		 * 忽略
		 */
		DO_NOTHING,
		/**
		 * 立即执行一次
		 */
		FIRE_ONCE_NOW;

	}

	/**
	 * 阻塞处理策略
	 */
	public enum BlockStrategy {

		/**
		 * 单机串行
		 */
		SERIAL_EXECUTION,
		/**
		 * 丢弃后续调度
		 */
		DISCARD_LATER,
		/**
		 * 覆盖之前调度
		 */
		COVER_EARLY;

	}

}
