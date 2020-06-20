package com.spring.boot.batch.util;


import org.springframework.batch.core.scope.context.ChunkContext;

public class ExecutionContextUtil {
	
	public static Object get(ChunkContext chunkContext, String key) {
		return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(key);
	}
	
	public static void put(ChunkContext chunkContext, String key, Object data) {
		chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(key, data);
	}
}
