package com.unionpay.quartz.task.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.unionpay.quartz.task.entity.TaskInformation;

public interface TaskInformationRepository extends MongoRepository<TaskInformation, String> {
	public TaskInformation getByTaskNo(String taskNo);

	public List<TaskInformation> getByFrozenStatus(String frozenStatus);
}
