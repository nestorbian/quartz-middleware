package com.unionpay.quartz.task.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.unionpay.quartz.task.Constants;
import com.unionpay.quartz.task.entity.TaskExecuteRecord;
import com.unionpay.quartz.task.entity.TaskInformation;
import com.unionpay.quartz.task.enums.StatusEnum;
import com.unionpay.quartz.task.exception.BizException;
import com.unionpay.quartz.task.repository.TaskExecuteRecordRepository;
import com.unionpay.quartz.task.service.NoticeService;
import com.unionpay.quartz.task.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service("noticeService")
@Slf4j
public class NoticeServiceImpl implements NoticeService {
	@Autowired
	private TaskExecuteRecordRepository repository;
	
	@Autowired
	private AmqpTemplate rabbitTemplate;
	
	@Autowired
	private RestTemplate restTemplate;

	private void sendMQmsg(String taskNo, String msg) {
		rabbitTemplate.convertAndSend(Constants.EXCAHNGE_NAME, Constants.BINDING_PREFIX + taskNo, msg);
		System.out.println("mq消息发送成功，内容为: " + msg);
	}

	private void httpRequest(String url, String executeParamter) {
		StringUtil.validateEmpty(url, "url不能为空");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		
		Map<String, String> map = new HashMap<>();
		map.put("executeParamter", executeParamter);
		
		HttpEntity<Map<String, String>> request = new HttpEntity<Map<String, String>>(map, headers);
		ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);
		System.out.println("http请求发送成功，返回结果：" + response.getBody().toString());
	}

	@Override
	public void sendMQmsgOrHttpRequest(TaskInformation taskInformation) {
		LocalDateTime executeTime = LocalDateTime.now();
		TaskExecuteRecord taskExecuteRecord = new TaskExecuteRecord();
		try {
			if(Constants.RABBIT_MQ.equals(taskInformation.getSendType())) {
				String msg = taskInformation.getTaskNo() + "," + taskInformation.getExecuteParamter();
				sendMQmsg(taskInformation.getTaskNo(), msg);
			} else if (Constants.URL_REQUEST.equals(taskInformation.getSendType())) {
				httpRequest(taskInformation.getUrl(), taskInformation.getExecuteParamter());
			} else {
				throw new BizException("不支持当前类型");
			}
			taskExecuteRecord.setTaskId(taskInformation.getId());
			taskExecuteRecord.setExecuteTime(executeTime);
			taskExecuteRecord.setCreateTime(LocalDateTime.now());
			taskExecuteRecord.setTaskStatus(StatusEnum.SUCCESS);
			repository.insert(taskExecuteRecord);
		} catch (Exception e) {
			taskExecuteRecord.setTaskId(taskInformation.getId());
			taskExecuteRecord.setExecuteTime(executeTime);
			taskExecuteRecord.setCreateTime(LocalDateTime.now());
			taskExecuteRecord.setFailReason(e.getMessage());
			taskExecuteRecord.setTaskStatus(StatusEnum.FAILED);
			repository.insert(taskExecuteRecord);
			throw new BizException("执行任务失败");
		}
	}

}
