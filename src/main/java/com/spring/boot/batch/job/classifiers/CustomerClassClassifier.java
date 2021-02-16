package com.spring.boot.batch.job.classifiers;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import com.spring.boot.batch.model.Customer;

public class CustomerClassClassifier implements Classifier<Customer, ItemWriter<? super Customer>> {

	private static final long serialVersionUID = 1L;

	private ItemWriter<Customer> evenItemWriter;
	private ItemWriter<Customer> oddItemWriter;

	public CustomerClassClassifier(ItemWriter<Customer> evenItemWriter, ItemWriter<Customer> oddItemWriter) {
		this.evenItemWriter = evenItemWriter;
		this.oddItemWriter = oddItemWriter;
	}

	@Override
	public ItemWriter<? super Customer> classify(Customer customer) {
		return customer.getId() % 2 == 0 ? evenItemWriter : oddItemWriter;
	}

}
