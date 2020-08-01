package com.unionpay.quartz.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.unionpay.quartz.task.Constants;
import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.entity.TaskInformation;
import com.unionpay.quartz.task.service.TaskInformationService;
import com.unionpay.quartz.task.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TaskController {
	@Autowired
	private TaskInformationService service;
	
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
    	ResultWithPagination page = service.getTaskInfoListByPage(1);
    	model.addAttribute("page", page);
    	
        return "list";
    }
    
    @RequestMapping(value = "/task-list/{pageNumber}", method = RequestMethod.GET)
    public String getTaskList(@PathVariable int pageNumber, Model model) {
    	ResultWithPagination page = service.getTaskInfoListByPage(pageNumber);
    	model.addAttribute("page", page);
    	
        return "list";
    }
    
    // 重启指定任务
    @RequestMapping(value = "/resumeTask/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String resumeTask(@PathVariable String id) {
    	try {
    		service.resumeTask(id);
    	} catch (Exception e) {
    		log.error("重启异常：", e);
    		return e.getMessage();
		}
    	
        return "重启成功";
    }
    
    // 启动指定任务
    @RequestMapping(value = "/runTask/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String runTask(@PathVariable String id) {
    	try {
    		service.runTask(id);
    	} catch (Exception e) {
    		log.error("启动异常：", e);
    		return e.getMessage();
		}
    	
        return "任务启动成功";
    }
    
    // 暂停指定任务
    @RequestMapping(value = "/pauseTask/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String pauseTask(@PathVariable String id) {
    	try {
    		service.pauseTask(id);
    	} catch (Exception e) {
    		log.error("暂停异常：", e);
    		return e.getMessage();
		}
    	
        return "暂停任务成功";
    }
    
    // 重启所有运行的任务
    @RequestMapping(value = "/resumeAllTask", method = RequestMethod.GET)
    @ResponseBody
    public String resumeAllTask() {
    	try {
    		service.resumeAllTask();
    	} catch (Exception e) {
    		log.error("重启异常：", e);
    		return e.getMessage();
		}
    	
        return "重启成功";
    }
    
    // 任务详情
	@RequestMapping(value = "/task-detail/{id}", method = RequestMethod.GET)
	public String getTaskDetail(@PathVariable String id, Model model) {
		TaskInformation taskInformation = service.getTaskInfoById(id);
		model.addAttribute("taskInfo", taskInformation);
		
   		return "taskDetail";
	}
	
    // 添加编辑任务页面
	@RequestMapping(value = "/task-edit-page", method = RequestMethod.GET)
	public String taskEditPage(@RequestParam(value = "id", required = false) String id, Model model) {
		if (!StringUtil.isBlank(id)) {
			TaskInformation taskInformation = service.getTaskInfoById(id);
			model.addAttribute("taskInfo", taskInformation);
			model.addAttribute("title","编辑定时任务");
		} else {
			model.addAttribute("taskInfo", new TaskInformation());
			model.addAttribute("title","新建定时任务");
		}
   		
   		return "taskEdit";
	}
	
    // 添加或编辑任务
	@RequestMapping(value = "/editTaskInfo", method = RequestMethod.POST)
	@ResponseBody
	public String editTaskInfo(TaskInformation taskInformation) {
		try {
			service.editTaskInfo(taskInformation);
		} catch (Exception e) {
			log.error("添加或修改失败：" + e.toString());
			return e.getMessage();
		}
		return Constants.SUCCESS;
	}
	
	// 验证任务编号
	@RequestMapping(value = "/checkTaskNo/{taskNo}", method = RequestMethod.GET)
	@ResponseBody
	public String checkTaskNo(@PathVariable String taskNo) {
		TaskInformation entity = service.getTaskInfoByTaskNo(taskNo);
		if(null != entity){
			return Constants.FAILED;
		}else{
			return Constants.SUCCESS;
		}
	}
	
	// 执行一次当前任务
	@RequestMapping(value = "/executeOnce/{id}", method = RequestMethod.GET)
	@ResponseBody
	public String executeOnce(@PathVariable String id) {
		try {
			service.executeOnce(id);
		} catch (Exception e) {
			log.error("执行异常：", e);
			return e.getMessage();
		}
		return "任务执行成功";
	}
	
	/**
	 * 删除任务
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/deleteTask/{id}", method = RequestMethod.GET)
	@ResponseBody
	public String deleteTask(@PathVariable String id) {
		try {
			service.deleteTask(id);
		} catch (Exception e) {
			log.error("删除异常：", e);
			return e.getMessage();
		}
		return "删除成功";
	}

}
