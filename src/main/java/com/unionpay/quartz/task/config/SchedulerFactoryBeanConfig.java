package com.unionpay.quartz.task.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class SchedulerFactoryBeanConfig {
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean bean = new SchedulerFactoryBean();
        Properties properties = new Properties();
        try {
        	properties.load(this.getClass().getClassLoader().getResourceAsStream("quartz.properties"));
        } catch (IOException e) {
           throw new Error(e);
        }
        bean.setQuartzProperties(properties);
        bean.setWaitForJobsToCompleteOnShutdown(true);
		return bean;
	}
}
