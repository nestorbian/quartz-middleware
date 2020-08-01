package com.unionpay.quartz.task.core;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import com.unionpay.quartz.task.entity.TaskInformation;
import com.unionpay.quartz.task.service.NoticeService;
import com.unionpay.quartz.task.util.SpringContext;

import lombok.extern.slf4j.Slf4j;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Slf4j
public class QuartzJob implements Job {
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String id = context.getMergedJobDataMap().getString("id");
		String taskNo = context.getMergedJobDataMap().getString("taskNo");
		String sendType = context.getMergedJobDataMap().getString("sendType");
		String executeParamter = context.getMergedJobDataMap().getString("executeParamter");
		String url = context.getMergedJobDataMap().getString("url");
		log.info("id:" + id + "|taskNo:" + taskNo + "|sendType:"+ sendType
						+ "|executeParamter:"+ executeParamter+ "|url:"+ url);
		NoticeService noticeService = (NoticeService) SpringContext.getBean("noticeService");
		TaskInformation taskInformation = new TaskInformation();
		taskInformation.setId(id);
		taskInformation.setSendType(sendType);
		taskInformation.setTaskNo(taskNo);
		taskInformation.setUrl(url);
		taskInformation.setExecuteParamter(executeParamter);
		try {
			noticeService.sendMQmsgOrHttpRequest(taskInformation);
		} catch (Exception e) {
			log.error("异常：", e);
		}
	}
	
}
