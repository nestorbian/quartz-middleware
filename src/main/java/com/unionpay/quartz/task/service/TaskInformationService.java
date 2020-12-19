package com.unionpay.quartz.task.service;

import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.entity.TaskInformation;

public interface TaskInformationService {
	
	void resumeTask(String id);
	
	void resumeAllTask();
	
	void runTask(String id);
	
	void pauseTask(String id);
	
	ResultWithPagination getTaskInfoListByPage(int pageNumber);
	
	String editTaskInfo(TaskInformation entity);
	
	TaskInformation getTaskInfoById(String id);
	
	TaskInformation getTaskInfoByTaskNo(String taskNo);
	
	void executeOnce(String id);
	
	void deleteTask(String id);
}
