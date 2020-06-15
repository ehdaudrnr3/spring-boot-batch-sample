package com.spring.boot.batch;


import java.util.Date;

import javax.annotation.Resource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableBatchProcessing
@RequiredArgsConstructor
public class SpringBootBatchSampleApplication implements CommandLineRunner {
	
	private final JobLauncher jobLauncher;
	
	@Resource(name = "customerJob")
	private final Job customerJob;
	
	@Resource(name = "mybatisJob")
	private final Job mybatisJob;
	
	@Resource(name = "classifierMultiFileJob")
	private final Job classifierMultiFileJob;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootBatchSampleApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("JobId", String.valueOf(System.currentTimeMillis()))
                .addDate("date", new Date())
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();
		
		JobExecution execution =  jobLauncher.run(classifierMultiFileJob, jobParameters);
		System.out.println("STATUS : " + execution.getStatus());
		
	}

}
