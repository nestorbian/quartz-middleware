package com.unionpay.quartz.task.service;

import com.unionpay.quartz.task.entity.ResultWithPagination;

public interface TaskExecuteRecordService {
	ResultWithPagination getTaskRecordList(String taskNo, int pageNumber, String taskStatus);
	
	void deleteTaskRecordById(String id);
}
