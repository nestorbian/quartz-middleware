package com.unionpay.quartz.task.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.unionpay.quartz.task.enums.TaskStatusEnum;

import lombok.Data;

@Document(collection = "taskinformations")
@Data
public class TaskInformation {
	@Id
	private String id;
//	private Integer version;
	private String taskNo;
	private String taskName;
	private String schedulerRule;
	private TaskStatusEnum frozenStatus;
//	private String executorNo;
	private String sendType;
	private String url;
	private String executeParamter;
//	private String queueName;
	private LocalDateTime frozenTime;
	private LocalDateTime unfrozenTime;
	private LocalDateTime createTime;
	private LocalDateTime lastModifyTime;
}
