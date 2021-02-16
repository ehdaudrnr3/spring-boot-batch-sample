package com.spring.boot.batch.partition.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.spring.boot.batch.model.Transaction;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class PartitionConfig {

	private final ResourcePatternResolver resourcePatternResolver;
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean(name = "partitionerJob")
	public Job partitionerJob() throws Exception {
		return jobBuilderFactory.get("partitionerJob")
				.start(partitionStep())
				.build();
	}

	@Bean(name = "partitionStep")
	public Step partitionStep() {
		return stepBuilderFactory.get("partitionStep")
				.partitioner("slaveStep", partitioner())
				.step(slaveStep())
				.taskExecutor(taskExecutor())
				.build();
	}

	@Bean
	public Partitioner partitioner() {
		CustomMultiResourcePartitioner partitioner = new CustomMultiResourcePartitioner();
		Resource[] resources;
		try {
			resources = resourcePatternResolver.getResources("file:src/main/resources/partitioner/input/*.csv");
		} catch (Exception e) {
			throw new RuntimeException("I/O problem when resolving the inpout file pattern.", e);
		}
		partitioner.setResources(resources);
		return partitioner;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(5);
		taskExecutor.setCorePoolSize(5);
		taskExecutor.setQueueCapacity(5);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}
	
	@Bean
	public Step slaveStep() {
		return stepBuilderFactory.get("slaveStep")
				.<Transaction, Transaction>chunk(1)
				.reader(reader(null))
				.writer(writer(null))
				.build();
	}
	
	@Bean
	@StepScope
	public FlatFileItemReader<Transaction> reader(
			@Value("#{stepExecutionContext[fileName]}") String fileName) {
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames("username", "userId", "transactionDate", "amount");
		
		DefaultLineMapper<Transaction> lineMapper = new DefaultLineMapper<>();
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(new RecordFieldSetMapper<Transaction>(Transaction.class, conversionLocalDateService()));
		
		FlatFileItemReader<Transaction> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("partitioner/input/" + fileName));
		reader.setLinesToSkip(1);
		reader.setLineMapper(lineMapper);
		
		return reader;
	}
	
	@Bean
    public ConversionService conversionLocalDateService() {
        DefaultConversionService testConversionService = new DefaultConversionService();
        DefaultConversionService.addDefaultConverters(testConversionService);
        testConversionService.addConverter(new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String text) {
            	DateTimeFormatter format = DateTimeFormatter.ofPattern("d/MM/yyyy");

        	    DateTimeFormatter dateTimeFormat = new DateTimeFormatterBuilder().append(format)
        	    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
        	    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
        	    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        	    .toFormatter();

                return LocalDateTime.parse(text.trim(), dateTimeFormat);
            }
        });

        return testConversionService;
    }


	@Bean(destroyMethod = "")
	@StepScope
	public StaxEventItemWriter<Transaction> writer( 
			@Value("#{stepExecutionContext[opFileName]}") String fileName) {
		StaxEventItemWriter<Transaction> itemWriter = new StaxEventItemWriter<>();
        itemWriter.setMarshaller(marshaller());
        itemWriter.setRootTagName("transactionRecord");
        itemWriter.setResource(new FileSystemResource("src/main/resources/partitioner/output/" + fileName));
		return itemWriter; 
	}

	@Bean
	public Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(Transaction.class);
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setMarshallerProperties(map);
		return marshaller;
	}
	
   private JobRepository getJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource());
        factory.setTransactionManager(getTransactionManager());
        // JobRepositoryFactoryBean's methods Throws Generic Exception,
        // it would have been better to have a specific one
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder.setType(EmbeddedDatabaseType.HSQL)
          .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
          .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
          .build();
    }

    private PlatformTransactionManager getTransactionManager() {
        return new ResourcelessTransactionManager();
    }

    public JobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        // SimpleJobLauncher's methods Throws Generic Exception,
        // it would have been better to have a specific one
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}
