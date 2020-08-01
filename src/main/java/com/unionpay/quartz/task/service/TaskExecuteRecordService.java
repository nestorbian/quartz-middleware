package com.unionpay.quartz.task.service;

import com.unionpay.quartz.task.entity.ResultWithPagination;

public interface TaskExecuteRecordService {
	public ResultWithPagination getTaskRecordList(String taskNo, int pageNumber, String taskStatus);
	
	public void deleteTaskRecordById(String id);
}
