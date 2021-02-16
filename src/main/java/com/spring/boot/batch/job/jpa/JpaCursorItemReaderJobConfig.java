package com.spring.boot.batch.job.jpa;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spring.boot.batch.entity.Pay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaCursorItemReaderJobConfig {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;

	private final int chunkSize = 5;

	@Bean
	public Job jpaCursorItemReaderJob() {
		return jobBuilderFactory.get("jpaCursorItemReaderJob")
				.start(jpaCursorItemReaderStep())
				.build();
	}
	
	@Bean
	public Step jpaCursorItemReaderStep() {
		return stepBuilderFactory.get("jpaCursorItemReaderStep")
				.<Pay, Pay>chunk(chunkSize)
				.reader(jpaCursorItemReader())
				.writer(jpaCursorItemReaderWriter())
				.build();
	}

	@Bean
	public JpaCursorItemReader<Pay> jpaCursorItemReader() {
		return new JpaCursorItemReaderBuilder<Pay>()
				.name("jpaCursorItemReader")
				.entityManagerFactory(entityManagerFactory)
				.queryString("SELECT p FROM Pay p")
				.maxItemCount(5)
				.currentItemCount(2)
				.saveState(true)
				.build();
	}
	
	@Bean
	public ItemWriter<Pay> jpaCursorItemReaderWriter() {
		return list -> {
			for(Pay pay : list) {
				log.info("Current Pay = {}", pay);
			}
		};
	}
}
