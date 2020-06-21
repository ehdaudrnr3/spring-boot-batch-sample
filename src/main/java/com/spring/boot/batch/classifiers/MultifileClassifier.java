package com.spring.boot.batch.classifiers;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.classify.Classifier;
import org.springframework.core.io.FileSystemResource;

import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.flatfile.header.MybatisFlatFileHeaderCallback;
import com.spring.boot.batch.writer.MultiFlatFileCustomWriter;

public class MultifileClassifier implements Classifier<CustomerExtendModel, ItemWriter<? super CustomerExtendModel>> {
	
	private static final long serialVersionUID = 1L;
	

	@Override
	public MultiFlatFileCustomWriter<? super CustomerExtendModel> classify(CustomerExtendModel customer) {
		String path = ".\\output\\multifile\\Customer"+customer.getFileName("")+".csv";

		MultiFlatFileCustomWriter<CustomerExtendModel> itemWriter = new MultiFlatFileCustomWriter<CustomerExtendModel>();
		itemWriter.setAppendAllowed(true);
		itemWriter.setHeaderCallback(new MybatisFlatFileHeaderCallback());
		itemWriter.setLineAggregator(new DelimitedLineAggregator<CustomerExtendModel>() {
			{
				setDelimiter("|");
				setFieldExtractor(new BeanWrapperFieldExtractor<CustomerExtendModel>() {
					{
						setNames(new String[] { "firstName", "lastName", "year", "yearMonth", "time"});
					}
				});
			}
		});
		itemWriter.setResource(new FileSystemResource(path));
		itemWriter.open(new ExecutionContext());
		
		return itemWriter;
	}
}
