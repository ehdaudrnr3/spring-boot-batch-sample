package com.spring.boot.batch.event;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.spring.boot.batch.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StepExecutionMultifileListener implements StepExecutionListener {

	@Override
	public void beforeStep(StepExecution stepExecution) {
		Path from = Paths.get("./output/multifile");
		Path to = Paths.get("./output/multifile/backup");
		
		if(!to.toFile().isDirectory()) {
			to.toFile().mkdir();
		}
		
		FileUtil.moveDirectory(from, to);

		log.info("Called StepExecutionEventListener before");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("Called StepExecutionEventListener after");
		return ExitStatus.COMPLETED;
	}

}
