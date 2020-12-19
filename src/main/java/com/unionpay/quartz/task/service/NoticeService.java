package com.unionpay.quartz.task.service;

import com.unionpay.quartz.task.entity.TaskInformation;

public interface NoticeService {
	void sendMQmsgOrHttpRequest(TaskInformation taskInformation);
}
