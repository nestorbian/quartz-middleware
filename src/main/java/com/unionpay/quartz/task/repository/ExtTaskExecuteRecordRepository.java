package com.unionpay.quartz.task.repository;

import java.util.List;

import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.entity.TaskExecuteRecord;

public interface ExtTaskExecuteRecordRepository {
	public long countByTaskNoAndTaskStatus(String taskNo, String taskStatus);
	
	public List<TaskExecuteRecord> getListByPage(String taskNo, String taskStatus,
												 ResultWithPagination resultWithPagination);
}
