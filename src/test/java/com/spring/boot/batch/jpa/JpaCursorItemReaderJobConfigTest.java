package com.spring.boot.batch.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.boot.batch.TestBatchConfig;
import com.spring.boot.batch.entity.Pay;
import com.spring.boot.batch.job.jpa.JpaCursorItemReaderJobConfig;
import com.spring.boot.batch.repository.PayRepository;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = {JpaCursorItemReaderJobConfig.class, TestBatchConfig.class})
public class JpaCursorItemReaderJobConfigTest {
	
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private PayRepository payRepository;
	
	@AfterEach
	public void tearDown() {
		payRepository.deleteAllInBatch();
	}
	
	@Test
	public void JPA_CURSOR_조회() throws Exception {
		for (int i = 1; i <= 10; i++) {
			payRepository.save(new Pay(i * 1000L, String.valueOf(i), LocalDateTime.now()));
		}
		
		JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
			.addString("version", "1")
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
	}

}
