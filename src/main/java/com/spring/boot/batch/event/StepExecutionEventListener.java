package com.spring.boot.batch.event;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StepExecutionEventListener implements StepExecutionListener {

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("Called StepExecutionEventListener before");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("Called StepExecutionEventListener after");
		return ExitStatus.COMPLETED;
	}
	

}
