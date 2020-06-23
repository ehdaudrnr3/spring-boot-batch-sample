package com.spring.boot.batch.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class UploadListener implements StepExecutionListener {
	
	private SqlSessionTemplate sessionTemplate;
	
	public UploadListener(SqlSessionFactory sqlSessionFactory) {
		this.sessionTemplate = new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		Map<String, Object> params = new HashMap<>();
		params.put("jobId", stepExecution.getJobParameters().getParameters().get("JobId").toString());
		sessionTemplate.delete("com.spring.boot.batch.Customer.delete", params);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
