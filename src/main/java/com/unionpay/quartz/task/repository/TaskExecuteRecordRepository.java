package com.unionpay.quartz.task.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.unionpay.quartz.task.entity.TaskExecuteRecord;

public interface TaskExecuteRecordRepository extends MongoRepository<TaskExecuteRecord, String>,
													 ExtTaskExecuteRecordRepository {
	public void deleteByTaskId(String taskId);
}
