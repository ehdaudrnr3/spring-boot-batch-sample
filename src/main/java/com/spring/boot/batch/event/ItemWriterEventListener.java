package com.spring.boot.batch.event;

import java.util.List;

import org.springframework.batch.core.ItemWriteListener;

import com.spring.boot.batch.model.Customer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemWriterEventListener implements ItemWriteListener<Customer> {
	
	@Override
	public void beforeWrite(List<? extends Customer> items) {
		log.info("Called ItemWriterEventListener before");
	}

	@Override
	public void afterWrite(List<? extends Customer> items) {
		log.info("Called ItemWriterEventListener after");
	}

	@Override
	public void onWriteError(Exception exception, List<? extends Customer> items) {
		log.error("Called ItemWriterEventListener Write Error");
	}

}
