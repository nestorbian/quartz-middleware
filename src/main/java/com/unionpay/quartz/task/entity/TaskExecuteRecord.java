package com.unionpay.quartz.task.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.unionpay.quartz.task.enums.StatusEnum;

import lombok.Data;

@Document(collection = "taskexecuterecords")
@Data
public class TaskExecuteRecord {
	@Id
	private String id;
	private String taskId;
	private LocalDateTime executeTime;
	private StatusEnum taskStatus;
	private String failReason;
	private LocalDateTime createTime;
}
