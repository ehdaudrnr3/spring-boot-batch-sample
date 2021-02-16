package com.spring.boot.batch.job.jdbc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import com.spring.boot.batch.event.ItemReaderEventListener;
import com.spring.boot.batch.event.ItemWriterEventListener;
import com.spring.boot.batch.event.JobExecutionEventListener;
import com.spring.boot.batch.job.aggregator.CustomLineAggregator;
import com.spring.boot.batch.job.classifiers.CustomerClassClassifier;
import com.spring.boot.batch.job.rowmapper.CustomerMapper;
import com.spring.boot.batch.model.Customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JobClassifierConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;

	@Bean
	@StepScope
	public JdbcPagingItemReader<Customer> customerItemReader(@Value("#{jobParameters[JobId]}") String JobId) {
		Map<String, Order> sortkeys = new HashMap<>();
		sortkeys.put("id", Order.ASCENDING);

		H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
		queryProvider.setSelectClause("id, firstName, lastName, birthDate");
		queryProvider.setFromClause("from customer");
		queryProvider.setSortKeys(sortkeys);
		
		JdbcPagingItemReader<Customer> itemReader = new JdbcPagingItemReader<Customer>();
		itemReader.setDataSource(dataSource);
		itemReader.setFetchSize(100);
		itemReader.setRowMapper(new CustomerMapper());
		itemReader.setQueryProvider(queryProvider);

		return itemReader;
	}

	@Bean
	public FlatFileItemWriter<Customer> jsonItemWriter() throws Exception {
		String path = ".\\output\\classifier\\customer.txt";
		log.info(">> JobClassifierConfiguration : >Json Output Path = " + path);
		
		FlatFileItemWriter<Customer> itemWriter = new FlatFileItemWriter<>();
		itemWriter.setLineAggregator(new CustomLineAggregator());
		itemWriter.setResource(new FileSystemResource(path));
		return itemWriter;
	}

	@Bean
	public StaxEventItemWriter<Customer> xmlItemWriter() throws IOException{
		String path = ".\\output\\classifier\\customer.xml";
		log.info(">> JobClassifierConfiguration : Xml Output Path = " + path);
		
		Map<String, Class<?>> aliases = new HashMap<>();
		aliases.put("customer", Customer.class);
		XStreamMarshaller marshaller = new XStreamMarshaller();
		marshaller.setAliases(aliases);

		StaxEventItemWriter<Customer> itemWriter = new StaxEventItemWriter<Customer>();
		itemWriter.setRootTagName("customers");
		itemWriter.setMarshaller(marshaller);
		itemWriter.setResource(new FileSystemResource(path));
		
		return itemWriter;
	}
	
	@Bean
	public ClassifierCompositeItemWriter<Customer> compositeItemWriter() throws IOException, Exception {
		return new ClassifierCompositeItemWriterBuilder<Customer>()
				.classifier(new CustomerClassClassifier(jsonItemWriter(), xmlItemWriter()))
				.build();
	}
	
	@Bean 
	@JobScope
	public Step customerStep() throws IOException, Exception {
		return stepBuilderFactory.get("customerStep")
				.<Customer, Customer>chunk(10)
				.reader(customerItemReader(null))
				.writer(compositeItemWriter())
				.stream(jsonItemWriter())
				.stream(xmlItemWriter())
				//.listener(new StepExecutionEventListener())
				.listener(new ItemReaderEventListener())
				.listener(new ItemWriterEventListener())
				.build();
	}
	
	@Bean
	public Job customerJob() throws Exception {
		return jobBuilderFactory.get("customerJob")
				.start(customerStep())
				.listener(new JobExecutionEventListener())
				.build();
	}
}