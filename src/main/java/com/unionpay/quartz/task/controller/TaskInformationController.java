package com.unionpay.quartz.task.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.unionpay.quartz.task.entity.Result;
import com.unionpay.quartz.task.entity.TaskInformation;
import com.unionpay.quartz.task.service.TaskInformationService;

@RestController
public class TaskInformationController {
	@Autowired
	private TaskInformationService service;
	
	@PostMapping(path = "/task-informations")
	public Result<?> addTaskToScheduler(@RequestBody TaskInformation taskInformation) {
		String id = "";
		try {
			id = service.editTaskInfo(taskInformation);
		} catch (Exception e) {
			return new Result<>(e);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", id);
		
		return new Result<>(map);
	}
}
