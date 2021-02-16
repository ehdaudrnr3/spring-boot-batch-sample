package com.spring.boot.batch.event;

import java.io.IOException;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.spring.boot.batch.model.Customer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemReaderEventListener implements ItemReadListener<Customer>{
	
	@Override
	public void beforeRead() {
		log.info("Called ItemReaderEventListener before");
	}

	@Override
	public void afterRead(Customer customer) {
		String path = ".\\output\\classifilerMultifile\\"+customer.getFileName();
		Resource resource = new FileSystemResource(path);
		try {
			if(resource.getFile().exists()) {
				resource.getFile().delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		log.info("Called ItemReaderEventListener afer : "+customer.toString());
	}

	@Override
	public void onReadError(Exception ex) {
		log.error("Called ItemReaderEventListener Read Error");
	}
	

}
