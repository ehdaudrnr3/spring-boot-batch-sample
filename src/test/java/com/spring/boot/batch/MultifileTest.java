package com.spring.boot.batch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.boot.batch.job.multifile.CustomMultiFileJobConfiguration;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = {CustomMultiFileJobConfiguration.class, TestBatchConfig.class})
public class MultifileTest {
	
	@Autowired
	private JobLauncherTestUtils launcherTestUtils;
	
	@Autowired
	private JobRepositoryTestUtils repositoryTestUtils;
	
	@Before
	public void clearMetadata() {
		repositoryTestUtils.removeJobExecutions();
	}
	
	@Test
	public void testJob() throws Exception {
		JobParameters jobParameters = launcherTestUtils.getUniqueJobParameters();
		
		JobExecution jobExecution = launcherTestUtils.launchJob(jobParameters);
		
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
		
	}
}
