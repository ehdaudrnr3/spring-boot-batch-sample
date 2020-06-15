package com.spring.boot.batch.configuration;

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
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.spring.boot.batch.domain.Customer;
import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.flatfile.header.MybatisFlatFileHeaderCallback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MybatisJobConfiguration {
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final SqlSessionFactory sessionFactory;
	
	@Bean
	@StepScope
	public MyBatisPagingItemReader<Customer> myBatisPagingItemReader() {
		
		MyBatisPagingItemReader<Customer> itemReader = new MyBatisPagingItemReader<Customer>();
		itemReader.setSqlSessionFactory(sessionFactory);
		itemReader.setPageSize(200);
		itemReader.setQueryId("com.spring.boot.batch.Customer.list");
		return itemReader;
	}
	
	@Bean
	public ItemProcessor<Customer, CustomerExtendModel> mybatisProcessor() {
		return customer -> {
			return new CustomerExtendModel(customer);
		};
	}
	
	@Bean
	public FlatFileItemWriter<CustomerExtendModel> mybatisItemWriter() {
		String path = ".\\output\\csv\\customer.csv";
		log.info(">> MybatisJobConfiguration : csv Output Path = " + path);
		
		FlatFileItemWriter<CustomerExtendModel> itemWriter = new FlatFileItemWriter<CustomerExtendModel>();
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
		return itemWriter;
	}
	
	@Bean
	@JobScope
	public Step mybatisStep() {
		return stepBuilderFactory.get("mybatisStep")
				.<Customer, CustomerExtendModel>chunk(20)
				.reader(myBatisPagingItemReader())
				.processor(mybatisProcessor())
				.writer(mybatisItemWriter())
				.build();
	}
	
	@Bean
	public Job mybatisJob() {
		return jobBuilderFactory.get("mybatisJob")
				.incrementer(new RunIdIncrementer())
				.start(mybatisStep())
				.build();
	}
}
