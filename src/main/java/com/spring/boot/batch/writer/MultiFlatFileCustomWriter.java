package com.spring.boot.batch.writer;

import java.util.List;

import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class MultiFlatFileCustomWriter<T> extends AbstractCustomerFileItemWriter<T> {
	
	private LineAggregator<T> lineAggregator;
	
	public MultiFlatFileCustomWriter() {
		this.setExecutionContextName(ClassUtils.getShortName(FlatFileItemWriter.class));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(lineAggregator, "A LineAggregator must be provided.");
		if (append) {
			shouldDeleteIfExists = false;
		}
	}

	public void setLineAggregator(LineAggregator<T> lineAggregator) {
		this.lineAggregator = lineAggregator;
	}

	@Override
	public String doWrite(List<? extends T> items) {
		StringBuilder lines = new StringBuilder();
		for (T item : items) {
			lines.append(this.lineAggregator.aggregate(item)).append(this.lineSeparator);
		}
		return lines.toString();
	}

}
