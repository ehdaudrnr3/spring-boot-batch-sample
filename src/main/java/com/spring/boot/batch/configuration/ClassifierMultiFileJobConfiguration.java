package com.spring.boot.batch.configuration;

import java.io.IOException;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.spring.boot.batch.domain.Customer;
import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.event.ItemReaderEventListener;
import com.spring.boot.batch.flatfile.header.MybatisFlatFileHeaderCallback;
import com.spring.boot.batch.writer.MultiFlatFileCustomWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClassifierMultiFileJobConfiguration {
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final SqlSessionFactory sessionFactory;
	
	private final int chunkSize = 20;
	
	@Bean
	@StepScope
	public MyBatisPagingItemReader<Customer> classifierMultiFilePagingItemReader() {
		
		MyBatisPagingItemReader<Customer> itemReader = new MyBatisPagingItemReader<Customer>();
		itemReader.setSqlSessionFactory(sessionFactory);
		itemReader.setPageSize(chunkSize);
		itemReader.setQueryId("com.spring.boot.batch.Customer.list");
		return itemReader;
	}
	
	@Bean
	public ItemProcessor<Customer, CustomerExtendModel> classifierMultiFileProcessor() {
		return customer -> {
			return new CustomerExtendModel(customer);
		};
	}
	
	
	public MultiFlatFileCustomWriter<CustomerExtendModel> classifierMultiFileItemWriter(String resourcePath) throws ItemStreamException, IOException {
		log.info(">> ClassifierMultiFileJobConfiguration : multifile Output Path = " + resourcePath);
		
		Resource resource = new FileSystemResource(resourcePath);
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
		itemWriter.setResource(resource);
		itemWriter.open(new ExecutionContext());
		
		return itemWriter;
	}
	
	
	@Bean
	public Classifier<CustomerExtendModel, ItemWriter<? super CustomerExtendModel>> classifierMultifileItemClassfier() {
		return model -> {
			String path = ".\\output\\classifilerMultifile\\"+model.getFileName();
			try {
				return classifierMultiFileItemWriter(path);
			} catch (ItemStreamException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		};
	}
	
	@Bean
	public ClassifierCompositeItemWriter<CustomerExtendModel> classifierMultifileCompositeItemWriter() throws IOException, Exception {
		return new ClassifierCompositeItemWriterBuilder<CustomerExtendModel>()
				.classifier(classifierMultifileItemClassfier())
				.build();
	}
	@Bean
	@JobScope
	public Step classifierMultiFileStep() throws IOException, Exception {
		return stepBuilderFactory.get("classifierMultiFileStep")
				.<Customer, CustomerExtendModel>chunk(chunkSize)
				.reader(classifierMultiFilePagingItemReader())
				.processor(classifierMultiFileProcessor())
				.writer(classifierMultifileCompositeItemWriter())
				.listener(new ItemReaderEventListener())
				.build();
	}
	
	@Bean
	public Job classifierMultiFileJob() throws IOException, Exception {
		return jobBuilderFactory.get("classifierMultiFileJob")
				.incrementer(new RunIdIncrementer())
				.start(classifierMultiFileStep())
				.build();
	}
}
