package com.unionpay.quartz.task.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.unionpay.quartz.task.Constants;
import com.unionpay.quartz.task.core.QuartzJob;
import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.entity.TaskInformation;
import com.unionpay.quartz.task.enums.TaskStatusEnum;
import com.unionpay.quartz.task.exception.BizException;
import com.unionpay.quartz.task.repository.TaskExecuteRecordRepository;
import com.unionpay.quartz.task.repository.TaskInformationRepository;
import com.unionpay.quartz.task.service.NoticeService;
import com.unionpay.quartz.task.service.TaskInformationService;
import com.unionpay.quartz.task.util.StringUtil;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskInformationServiceImpl implements TaskInformationService {
	
	@Autowired
	private TaskInformationRepository repository;
	
	@Autowired
	private TaskExecuteRecordRepository taskExecuteRecordRepository;
	
	@Autowired
	private SchedulerFactoryBean schedulerBean;
	
	@Autowired
	private NoticeService noticeService;
	
	@Autowired
	private Channel channel;
	
	/**
	 * 启动指定任务
	 * @param id
	 * @return
	 */
	@SneakyThrows
	public void runTask(String id){
		// 验证参数
		StringUtil.validateEmpty(id, "id不能为空");
		
		TaskInformation entity = repository.findById(id).get();
		if(null != entity){
			Scheduler scheduler = schedulerBean.getScheduler();
			scheduler.deleteJob(new JobKey(entity.getTaskNo()));
			this.addTaskToScheduler(entity, scheduler);
			this.saveUnfrozenStatus(entity);
		}else{
			throw new BizException("该任务不存在");
		}
	}
	
	/**
	 * 暂停定时任务
	 * @param id
	 * @return
	 */
	@SneakyThrows
	public void pauseTask(String id){
		// 验证参数
		StringUtil.validateEmpty(id, "id不能为空");
		
		TaskInformation entity = repository.findById(id).get();
		if(null != entity && TaskStatusEnum.UNFROZEN == entity.getFrozenStatus()){
			Scheduler scheduler = schedulerBean.getScheduler();
			scheduler.deleteJob(new JobKey(entity.getTaskNo()));
			this.saveFrozenStatus(entity);
		} else {
			throw new BizException("该任务不存在或已暂停");
		}
	}
	
	/**
	 * 重启任务
	 */
	public void resumeTask(String id) {
		// 验证参数
		StringUtil.validateEmpty(id, "id不能为空");
		
		TaskInformation entity = repository.findById(id).get();
		if(null != entity) {
			Scheduler scheduler = schedulerBean.getScheduler();
			
			// 移除任务或添加任务失败改变任务的状态
			try {
				scheduler.deleteJob(new JobKey(entity.getTaskNo()));
				this.addTaskToScheduler(entity, scheduler);
			} catch (Exception e) {
				log.error("重启任务失败：", e);
				this.saveFrozenStatus(entity);
				throw new BizException("重启任务失败");
			}
		} else {
			throw new BizException("该任务不存在");
		}
	}
	
	/**
	 * 重启所有运行的任务
	 * @return
	 */
	@SneakyThrows
	public void resumeAllTask(){
		List<TaskInformation> taskList = repository.getByFrozenStatus(TaskStatusEnum.UNFROZEN.toString());
		Scheduler scheduler = schedulerBean.getScheduler();
		scheduler.clear();
		// 出错后，调度器里的任务与数据库保存的任务状态一致
		taskList.stream().forEach((item) -> {
			try {
				this.addTaskToScheduler(item, scheduler);
			} catch (Exception e) {
				log.error("重启任务" + item.getTaskNo() + "发生异常：",e);
				this.saveFrozenStatus(item);
			}
		});
	}
	
	/**
	 * 执行一次当前任务
	 */
	public void executeOnce(String id){
		// 验证参数
		StringUtil.validateEmpty(id, "id不能为空");
		
		TaskInformation taskInformation = repository.findById(id).get();
		if(null != taskInformation) {
			noticeService.sendMQmsgOrHttpRequest(taskInformation);
		} else {
			throw new BizException("当前任务不存在");
		}
	}

	/**
	 * 获取任务信息列表
	 */
	public ResultWithPagination getTaskInfoListByPage(int pageNumber) {
		long totalCounts = repository.count();
		ResultWithPagination pagination = new ResultWithPagination(pageNumber, Constants.PAGESIZE, totalCounts);
		Sort sort = Sort.by(Order.desc("createTime"));
		Pageable pageable = PageRequest.of(pagination.getPageNumber() - 1, Constants.PAGESIZE, sort);
		Page<TaskInformation> page = repository.findAll(pageable);
		pagination.setResults(page.getContent());
		
		return pagination;
	}
	
	/** 
	 * 添加或编辑任务信息
	 */
	@SneakyThrows
	public String editTaskInfo(TaskInformation entity){
		if(!StringUtil.isBlank(entity.getId())) {
			log.info("执行修改操作，id：{}", entity.getId());

			TaskInformation origin = repository.findById(entity.getId()).get();
			entity.setFrozenTime(origin.getFrozenTime());
			entity.setCreateTime(origin.getCreateTime());
			entity.setUnfrozenTime(origin.getUnfrozenTime());
			entity.setLastModifyTime(LocalDateTime.now());
			TaskInformation taskInformation = repository.save(entity);
			
			// 如果任务编号或者发送类型改变就删除之前的队列，发送类型为mq就创建新的队列以及绑定关系
			if (!origin.getTaskNo().equals(entity.getTaskNo()) || !origin.getSendType().equals(entity.getSendType())) {
				channel.queueDelete(origin.getTaskNo());
				if (Constants.RABBIT_MQ.equals(entity.getSendType())) {
					buildQueueAndBinding(channel, entity.getTaskNo());
				}
			}
			
			// 任务处于未冻结状态，就把任务重新装载到定时器
			if(taskInformation != null && TaskStatusEnum.UNFROZEN == entity.getFrozenStatus()) {
				Scheduler scheduler = schedulerBean.getScheduler();
				try {
					scheduler.deleteJob(new JobKey(origin.getTaskNo()));
					this.addTaskToScheduler(entity, scheduler);
				} catch (Exception e) {
					log.error("启动任务异常：",e);
					this.saveFrozenStatus(entity);
					throw new BizException(Constants.RELOAD);
				}
			}
			
			return entity.getId();
		} else {
			log.info("执行添加操作");
			// 增加
			entity.setId(null);
			entity.setCreateTime(LocalDateTime.now());
			entity.setFrozenStatus(TaskStatusEnum.INIT);
			TaskInformation taskInformation = repository.insert(entity);
			// 添加队列以及绑定关系
			if (Constants.RABBIT_MQ.equals(entity.getSendType())) {
				buildQueueAndBinding(channel, entity.getTaskNo());
			}
			
			return taskInformation.getId();
		}
	}
	
	/**
	 * 根据id获取任务信息
	 */
	public TaskInformation getTaskInfoById(String id){
		// 验证参数
		StringUtil.validateEmpty(id, "id不能为空");
		
		TaskInformation taskInformation = repository.findById(id).get();
		if (taskInformation == null) {
			throw new BizException("该任务不存在");
		}
		return taskInformation;
	}

	/**
	 * 根据任务编号获取任务信息
	 */
	@Override
	public TaskInformation getTaskInfoByTaskNo(String taskNo) {
		// 验证参数
		StringUtil.validateEmpty(taskNo, "任务编号不能为空");
		
		return repository.getByTaskNo(taskNo);
	}

	/**
	 * 删除任务
	 */
	@Override
	@SneakyThrows
	public void deleteTask(String id) {
		// 验证参数
		StringUtil.validateEmpty(id, "id不能为空");
		
		TaskInformation taskInformation = repository.findById(id).get();
		if (taskInformation == null) {
			throw new BizException("该任务已被删除");
		}
		schedulerBean.getScheduler().deleteJob(new JobKey(taskInformation.getTaskNo()));
		
		repository.deleteById(taskInformation.getId());
		// 删除对应的队列
		channel.queueDelete(taskInformation.getTaskNo());
		// 删除关于该任务的所有执行记录
		taskExecuteRecordRepository.deleteByTaskId(taskInformation.getId());
	}
	
	/**
	 * 新建队列并设置队列与交换机的绑定关系
	 * @param channel
	 * @param taskNo
	 */
	@SneakyThrows
	private void buildQueueAndBinding(Channel channel, String taskNo) {
		channel.queueDeclare(taskNo, true, false, false, null);
		channel.queueBind(taskNo, Constants.EXCAHNGE_NAME, Constants.BINDING_PREFIX + taskNo);
	}
	
	/**
	 * 将任务加载到调度器中
	 * @param task
	 * @param scheduler
	 */
	private void addTaskToScheduler(TaskInformation task, Scheduler scheduler){
		TriggerKey triggerKey = TriggerKey.triggerKey(task.getTaskNo(), Scheduler.DEFAULT_GROUP);
		JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withDescription(task.getTaskName()).withIdentity(task.getTaskNo(), Scheduler.DEFAULT_GROUP).build();
		jobDetail.getJobDataMap().put(Constants.ID, task.getId());
		jobDetail.getJobDataMap().put(Constants.TASKNO, task.getTaskNo());
		jobDetail.getJobDataMap().put(Constants.SENDTYPE, task.getSendType());
		jobDetail.getJobDataMap().put(Constants.URL, task.getUrl());
		jobDetail.getJobDataMap().put(Constants.EXECUTEPARAMETER, task.getExecuteParamter());
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(task.getSchedulerRule());
		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
		try {
			/* 	trigger与jobkey是一对一关系，不匹配会报错
				jobkey与trigger是一对多关系
			 */
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("任务 "+ task.getTaskNo() +" 定时规则 :" + task.getSchedulerRule() + " 加载成功");
		} catch (Exception e) {
			log.error("加载任务失败：", e);
			throw new BizException("加载任务失败");
		}
	}
	
	/**
	 * 保存为冻结状态
	 * @param taskInformation
	 */
	private void saveFrozenStatus(TaskInformation taskInformation) {
		taskInformation.setFrozenStatus(TaskStatusEnum.FROZEN);
		taskInformation.setFrozenTime(LocalDateTime.now());
		taskInformation.setLastModifyTime(LocalDateTime.now());
		repository.save(taskInformation);
	}
	
	private void saveUnfrozenStatus(TaskInformation taskInformation) {
		taskInformation.setFrozenStatus(TaskStatusEnum.UNFROZEN);
		taskInformation.setUnfrozenTime(LocalDateTime.now());
		taskInformation.setLastModifyTime(LocalDateTime.now());
		repository.save(taskInformation);
	}
	
}
