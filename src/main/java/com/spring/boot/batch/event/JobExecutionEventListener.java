package com.spring.boot.batch.event;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobExecutionEventListener implements JobExecutionListener {
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("Called JobExecutionEventListener before");
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		log.info("Called JobExecutionEventListener after");
	}
	
}
