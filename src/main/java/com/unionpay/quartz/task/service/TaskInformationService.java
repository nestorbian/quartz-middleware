package com.unionpay.quartz.task.service;

import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.entity.TaskInformation;

public interface TaskInformationService {
	
	public void resumeTask(String id);
	
	public void resumeAllTask();
	
	public void runTask(String id);
	
	public void pauseTask(String id);
	
	public ResultWithPagination getTaskInfoListByPage(int pageNumber);
	
	public String editTaskInfo(TaskInformation entity);
	
	public TaskInformation getTaskInfoById(String id);
	
	public TaskInformation getTaskInfoByTaskNo(String taskNo);
	
	public void executeOnce(String id);
	
	public void deleteTask(String id);
}
