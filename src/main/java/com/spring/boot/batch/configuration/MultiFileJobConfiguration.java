package com.spring.boot.batch.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spring.boot.batch.classifiers.MultifileClassifier;
import com.spring.boot.batch.domain.Customer;
import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.event.StepExecutionMultifileListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MultiFileJobConfiguration {
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final SqlSessionFactory sessionFactory;
	
	private String executionTime;
	private final int chunkSize = 20;
	
	@Bean
	@StepScope
	public MyBatisPagingItemReader<Customer> multiFilePagingItemReader() {
		
		MyBatisPagingItemReader<Customer> itemReader = new MyBatisPagingItemReader<Customer>();
		itemReader.setSqlSessionFactory(sessionFactory);
		itemReader.setPageSize(chunkSize);
		itemReader.setQueryId("com.spring.boot.batch.Customer.list");
		return itemReader;
	}
	
	@Bean
	public ItemProcessor<Customer, CustomerExtendModel> multiFileProcessor() {
		return customer -> {
			return new CustomerExtendModel(customer);
		};
	}
	
	@Bean
	public ClassifierCompositeItemWriter<CustomerExtendModel> multifileCompositeItemWriter() throws IOException, Exception {
		return new ClassifierCompositeItemWriterBuilder<CustomerExtendModel>()
				.classifier(new MultifileClassifier())
				.build();
	}
	
	@Bean
	@JobScope
	public Step multiFileStep() throws IOException, Exception {
		LocalDateTime localDateTime = LocalDateTime.now();
		executionTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		
		return stepBuilderFactory.get("multiFileStep")
				.<Customer, CustomerExtendModel>chunk(chunkSize)
				.reader(multiFilePagingItemReader())
				.processor(multiFileProcessor())
				.writer(multifileCompositeItemWriter())
				.listener(new StepExecutionMultifileListener())
				.build();
	}
	
	@Bean
	public Job multiFileJob() throws IOException, Exception {
		return jobBuilderFactory.get("multiFileJob")
				.incrementer(new RunIdIncrementer())
				.start(multiFileStep())
				.build();
	}
}
