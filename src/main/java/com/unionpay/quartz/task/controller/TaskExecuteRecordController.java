package com.unionpay.quartz.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.service.TaskExecuteRecordService;

@Controller
public class TaskExecuteRecordController {
	@Autowired
	private TaskExecuteRecordService service;
	
	/**
	 * 定时任务记录列表页面展示
	 */
	@RequestMapping(value = "/task-record-list", method = RequestMethod.GET)
	public String getTaskRecordList(@RequestParam(value = "taskNo", defaultValue = "") String taskNo,
									@RequestParam(value = "pageNumber") int pageNumber,
									@RequestParam(value = "taskStatus", defaultValue = "") String taskStatus,
									Model model) {
		ResultWithPagination page = service.getTaskRecordList(taskNo, pageNumber, taskStatus);
		model.addAttribute("page", page);
		model.addAttribute("taskNo", taskNo);
		model.addAttribute("taskStatus", taskStatus);
		
		return "taskRecordList";
	}
	
	/**
	 * 删除记录
	 */
	@RequestMapping(value = "/deleteTaskRecord/{id}", method = RequestMethod.GET)
	public String deleteTaskRecordById(@PathVariable String id,
									   @RequestParam(value = "pageNumber") int pageNumber, 
									   @RequestParam(value = "taskNo") String taskNo, 
									   @RequestParam(value = "taskStatus") String taskStatus, 
									   RedirectAttributes redirectAttributes) {
		service.deleteTaskRecordById(id);
		redirectAttributes.addAttribute("taskNo", taskNo);
		redirectAttributes.addAttribute("pageNumber", pageNumber);
		redirectAttributes.addAttribute("taskStatus", taskStatus);
		
		return "redirect:/task-record-list";
	}
}
