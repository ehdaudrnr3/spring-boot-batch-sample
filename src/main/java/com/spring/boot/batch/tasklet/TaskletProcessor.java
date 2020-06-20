package com.spring.boot.batch.tasklet;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.spring.boot.batch.domain.Customer;
import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.util.ExecutionContextUtil;

public class TaskletProcessor implements Tasklet {
	
	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		List<Customer> list = (List<Customer>) ExecutionContextUtil.get(chunkContext, "data");
		List<CustomerExtendModel> convertList = list.stream().map(CustomerExtendModel::new).collect(Collectors.toList());
		ExecutionContextUtil.put(chunkContext, "data", convertList);
		
		return RepeatStatus.FINISHED;
	}
}
