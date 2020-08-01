package com.unionpay.quartz.task.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unionpay.quartz.task.Constants;
import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.entity.TaskExecuteRecord;
import com.unionpay.quartz.task.entity.TaskInformation;
import com.unionpay.quartz.task.enums.StatusEnum;
import com.unionpay.quartz.task.repository.TaskExecuteRecordRepository;
import com.unionpay.quartz.task.repository.TaskInformationRepository;
import com.unionpay.quartz.task.service.TaskExecuteRecordService;
import com.unionpay.quartz.task.util.StringUtil;
import com.unionpay.quartz.task.vo.TaskRecord;

@Service
public class TaskExecuteRecordServiceImpl implements TaskExecuteRecordService {
	@Autowired
	private TaskExecuteRecordRepository repository;
	
	@Autowired
	private TaskInformationRepository taskInformationRepository;

	@Override
	public ResultWithPagination getTaskRecordList(String taskNo, int pageNumber, String taskStatus) {
		if (!StatusEnum.SUCCESS.toString().equals(taskStatus) &&
			!StatusEnum.FAILED.toString().equals(taskStatus)) {
			taskStatus = null;
		}
		
		long totalCounts = repository.countByTaskNoAndTaskStatus(taskNo, taskStatus);
		ResultWithPagination resultWithPagination = new ResultWithPagination(pageNumber, Constants.PAGESIZE, totalCounts);
		List<TaskExecuteRecord> taskExecuteRecords = repository.getListByPage(taskNo, taskStatus, resultWithPagination);
		// 转换成视图可用的任务记录
		List<TaskRecord> taskRecords = new ArrayList<TaskRecord>();
		taskExecuteRecords.stream().forEach((item) -> {
			TaskInformation taskInformation = taskInformationRepository.findById(item.getTaskId()).get();
			taskRecords.add(new TaskRecord(item.getId(), taskInformation.getTaskNo(), taskInformation.getTaskName(), 
					item.getExecuteTime(), item.getTaskStatus(), item.getFailReason(), item.getCreateTime()));
		});
		resultWithPagination.setResults(taskRecords);
		return resultWithPagination;
	}

	@Override
	public void deleteTaskRecordById(String id) {
		StringUtil.validateEmpty(id, "id不能为空");
		
		repository.deleteById(id);
	}

}
