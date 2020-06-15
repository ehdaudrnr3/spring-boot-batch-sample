package com.spring.boot.batch.event;

import org.springframework.batch.core.ItemReadListener;

import com.spring.boot.batch.domain.Customer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemReaderEventListener implements ItemReadListener<Customer>{
	
	@Override
	public void beforeRead() {
		log.info("Called ItemReaderEventListener before");
	}

	@Override
	public void afterRead(Customer item) {
		log.info("Called ItemReaderEventListener afer : "+item.toString());
	}

	@Override
	public void onReadError(Exception ex) {
		log.error("Called ItemReaderEventListener Read Error");
	}
	

}
