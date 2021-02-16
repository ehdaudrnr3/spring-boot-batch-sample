package com.spring.boot.batch.job.aggregator;

import org.springframework.batch.item.file.transform.LineAggregator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.boot.batch.model.Customer;

public class CustomLineAggregator implements LineAggregator<Customer> {

	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public String aggregate(Customer item) {
		try {
			return mapper.writeValueAsString(item);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to serialize Customer", e);
		}
	}

}
