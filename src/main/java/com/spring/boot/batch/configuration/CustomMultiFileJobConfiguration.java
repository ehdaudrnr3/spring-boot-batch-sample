package com.spring.boot.batch.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.spring.boot.batch.domain.CompositeModel;
import com.spring.boot.batch.domain.Customer;
import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.event.ItemReaderEventListener;
import com.spring.boot.batch.event.ItemWriterEventListener;
import com.spring.boot.batch.event.StepExecutionEventListener;
import com.spring.boot.batch.event.UploadListener;
import com.spring.boot.batch.flatfile.header.MybatisFlatFileHeaderCallback;
import com.spring.boot.batch.jobparameters.CustomJobParameters;
import com.spring.boot.batch.tasklet.CustomMulifileUploadTasklet;
import com.spring.boot.batch.tasklet.FailTasklet;
import com.spring.boot.batch.writer.CustomCompositeItemWriter;
import com.spring.boot.batch.writer.MultiFlatFileCustomWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomMultiFileJobConfiguration {
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final SqlSessionFactory sessionFactory;
	
	@Autowired
	private CustomJobParameters jobParameters;
	
	private final int chunkSize = 20;
	private String executionTime;
	
	@Bean
	@StepScope
	public MyBatisPagingItemReader<Customer> classifierMultiFilePagingItemReader(
			@Value("#{jobParameters}") Map<String, Object> jobParam) {
		
		log.info("JobParam : {}", jobParam.get("JobId"));
		log.info("CustomJobParameters : {}", jobParameters.getJobid());
		MyBatisPagingItemReader<Customer> itemReader = new MyBatisPagingItemReader<Customer>();
		itemReader.setSqlSessionFactory(sessionFactory);
		itemReader.setPageSize(chunkSize);
		itemReader.setQueryId("com.spring.boot.batch.Customer.list");
		return itemReader;
	}
	
	@Bean
	@StepScope
	public ItemProcessor<Customer, CustomerExtendModel> classifierMultiFileProcessor() {
		return customer -> {
			return new CustomerExtendModel(customer);
		};
	}
	
	@Bean
	public Classifier<CustomerExtendModel, CompositeModel> classifierMultifileItemClassfier() {
		return model -> {
			String path = ".\\output\\classifilerMultifile\\"+model.getFileName(executionTime);
			try {
				return new CompositeModel(model.getYearMonth(), classifierMultiFileItemWriter(path));
			} catch (ItemStreamException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		};
	}
	
	@Bean
	public CustomCompositeItemWriter classifierMultifileCompositeItemWriter() throws IOException, Exception {
		CustomCompositeItemWriter c = new CustomCompositeItemWriter();
		c.setClassifier(classifierMultifileItemClassfier());
		
		return c;
	}
	
	@StepScope
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
	public Step classifierMultiFileStep() throws IOException, Exception {
		LocalDateTime localDateTime = LocalDateTime.now();
		executionTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		
		return stepBuilderFactory.get("classifierMultiFileStep")
				.<Customer, CustomerExtendModel>chunk(chunkSize)
				.reader(classifierMultiFilePagingItemReader(null))
				.processor(classifierMultiFileProcessor())
				.writer(classifierMultifileCompositeItemWriter())
				.listener(new ItemReaderEventListener())
				.listener(new ItemWriterEventListener())
				.listener(new StepExecutionEventListener())
				.build();
	}
	
	@Bean
	public Step customMulifileUploadStep() {
		return stepBuilderFactory.get("customMulifileUploadStep")
				.tasklet(customUploadTasklet())
				.listener(new UploadListener(sessionFactory))
				.build();
	}
	
	@Bean
	public Tasklet customUploadTasklet() {
		return new CustomMulifileUploadTasklet(sessionFactory);
	}
	
	@Bean
	public Step failStep() {
		return stepBuilderFactory.get("failStep")
				.tasklet(failTasklet())
				.build();
	}
	
	@Bean
	public Tasklet failTasklet() {
		return new FailTasklet();
	}
	
	@Bean
	@JobScope
	public CustomJobParameters customJobParameters() {
		return new CustomJobParameters();
	}
	
	@Bean
	public Job classifierMultiFileJob(Step classifierMultiFileStep, Step customMulifileUploadStep, Step failStep) throws IOException, Exception {
		return jobBuilderFactory.get("classifierMultiFileJob")
				.incrementer(new RunIdIncrementer())
				.start(classifierMultiFileStep)
					.on("COMPLETED")
					.to(customMulifileUploadStep)
					.on("*")
					.end()
				.from(classifierMultiFileStep)
					.on("FAILED")
					.to(failStep)
					.end()
				.build();
	}
}
