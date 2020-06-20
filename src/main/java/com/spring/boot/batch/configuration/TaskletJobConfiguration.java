package com.spring.boot.batch.configuration;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spring.boot.batch.tasklet.TaskletProcessor;
import com.spring.boot.batch.tasklet.TaskletReader;
import com.spring.boot.batch.tasklet.TaskletWriter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class TaskletJobConfiguration {
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final SqlSessionFactory sqlsessionFactory;
	
	@Bean
	public Job taskletJob() {
		return jobBuilderFactory.get("taskletJob")
				.start(taskletReader())
				.next(taskletProcessor())
				.next(taskletWriter())
				.build();
	}
	
	@Bean
	public Step taskletReader() {
		return stepBuilderFactory.get("taskletReader")
				.tasklet(new TaskletReader(sqlsessionFactory))
				.build();
	}
	
	@Bean
	public Step taskletProcessor() {
		return stepBuilderFactory.get("taskletProcessor")
				.tasklet(new TaskletProcessor())
				.build();
	}
	
	@Bean
	public Step taskletWriter() {
		return stepBuilderFactory.get("taskletWriter")
				.tasklet(new TaskletWriter())
				.build();
	}

}
