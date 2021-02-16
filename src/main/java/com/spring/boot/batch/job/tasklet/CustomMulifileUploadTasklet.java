package com.spring.boot.batch.job.tasklet;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class CustomMulifileUploadTasklet implements Tasklet {
	
	private SqlSessionTemplate sessionTemplate;
	
	public CustomMulifileUploadTasklet(SqlSessionFactory sqlSessionFactory) {
		this.sessionTemplate = new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		Path path = Paths.get("./output/classifilerMultifile");
		Map<String, Object> params = new HashMap<>();
		String jobId = chunkContext.getStepContext().getJobParameters().get("JobId").toString();
		for(File file : path.toFile().listFiles()) {
			if(!file.isDirectory()) {
				params.put("jobId", jobId);
				params.put("fileName", file.getName());
				sessionTemplate.insert("com.spring.boot.batch.Customer.insert", params);
			}
		}

		return RepeatStatus.FINISHED;
	}


}
