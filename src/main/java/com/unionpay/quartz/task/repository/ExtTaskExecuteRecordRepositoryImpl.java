package com.unionpay.quartz.task.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.unionpay.quartz.task.Constants;
import com.unionpay.quartz.task.entity.ResultWithPagination;
import com.unionpay.quartz.task.entity.TaskExecuteRecord;
import com.unionpay.quartz.task.entity.TaskInformation;
import com.unionpay.quartz.task.util.StringUtil;

public class ExtTaskExecuteRecordRepositoryImpl implements ExtTaskExecuteRecordRepository {
	@Autowired
	private MongoTemplate template;

	@Override
	public long countByTaskNoAndTaskStatus(String taskNo, String taskStatus) {	
		return template.count(getQuery(taskNo, taskStatus), TaskExecuteRecord.class);
	}

	@Override
	public List<TaskExecuteRecord> getListByPage(String taskNo, String taskStatus,
			ResultWithPagination resultWithPagination) {
		Pageable pageable = PageRequest.of(resultWithPagination.getPageNumber() - 1, 
				resultWithPagination.getPageSize(), Sort.by(Order.desc(Constants.CREATETIME)));
		return template.find(getQuery(taskNo, taskStatus).with(pageable), TaskExecuteRecord.class);
	}
	
	private Query getQuery(String taskNo, String taskStatus) {
		Criteria criteria = new Criteria();
		if (!StringUtil.isBlank(taskNo)) {
			// 模糊查询得到TaskId集合
			Pattern pattern = Pattern.compile("^.*" + taskNo + ".*$", Pattern.CASE_INSENSITIVE);
			Query fuzzyQuery = new Query(Criteria.where(Constants.TASKNO).regex(pattern));
			List<TaskInformation> taskInformations = template.find(fuzzyQuery, TaskInformation.class);
			// 根据TaskId集合查询
			List<String> ids = new ArrayList<String>();
			taskInformations.stream().forEach((item) -> {
				ids.add(item.getId());
			});
			criteria.and(Constants.TASKID).in(ids);
		}
		if (!StringUtil.isBlank(taskStatus)) {
			criteria.and(Constants.TASKSTATUS).is(taskStatus);
		}
		Query query = new Query(criteria);
		return query;
	}

}
