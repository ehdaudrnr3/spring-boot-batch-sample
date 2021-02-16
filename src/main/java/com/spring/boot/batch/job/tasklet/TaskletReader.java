package com.spring.boot.batch.job.tasklet;

import java.util.List;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.spring.boot.batch.model.Customer;
import com.spring.boot.batch.util.ExecutionContextUtil;

public class TaskletReader implements Tasklet {
	
	private SqlSessionTemplate template;
	private final String queryId = "com.spring.boot.batch.Customer.list2";
	
	public TaskletReader(SqlSessionFactory sqlSessionFactory) {
		template = new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		List<Customer> list = template.selectList(queryId, chunkContext.getStepContext().getJobParameters());
		chunkContext.setAttribute("list", list);
		ExecutionContextUtil.put(chunkContext, "data", list);
		return RepeatStatus.FINISHED;
	}
}
