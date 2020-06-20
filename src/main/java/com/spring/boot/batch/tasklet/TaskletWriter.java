package com.spring.boot.batch.tasklet;

import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.util.ExecutionContextUtil;

public class TaskletWriter implements Tasklet {

	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		List<CustomerExtendModel> list = (List<CustomerExtendModel>) ExecutionContextUtil.get(chunkContext, "data");
		
		for(CustomerExtendModel e : list) {
			System.out.println(e.getFirstName());
		}
		return RepeatStatus.FINISHED;
	}

}
