package com.spring.boot.batch.partition;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.spring.boot.batch.partition.config.PartitionConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringBatchPartitionApplication {
	
	public static void main(String[] args) {
		
		@SuppressWarnings("resource")
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(PartitionConfig.class);
		context.refresh();
		
		final JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
		final Job job = (Job) context.getBean("partitionerJob");
		log.info("Stating the batch job");
		try {
			final JobExecution execution = (JobExecution) jobLauncher.run(job, new JobParameters());
			log.info("Job Status : {}", execution.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("job failed {}", e.getMessage());
		}
	}

}
