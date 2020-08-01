package com.unionpay.quartz.task.vo;

import java.time.LocalDateTime;

import com.unionpay.quartz.task.enums.StatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRecord {
	private String id;
	private String taskNo;
	private String taskName;
	private LocalDateTime executeTime;
	private StatusEnum taskStatus;
	private String failReason;
	private LocalDateTime createTime;
}
