package com.spring.boot.batch.classifiers;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;
import org.springframework.core.io.FileSystemResource;

import com.spring.boot.batch.domain.CustomerExtendModel;
import com.spring.boot.batch.writer.MultiFlatFileCustomWriter;

public class MutilFileClassifier implements Classifier<CustomerExtendModel, ItemWriter<? super CustomerExtendModel>> {

	private static final long serialVersionUID = 1L;

	private MultiFlatFileCustomWriter<CustomerExtendModel> itemWriter;

	public MutilFileClassifier(MultiFlatFileCustomWriter<CustomerExtendModel> itemWriter) {
		this.itemWriter = itemWriter;
	}

	@Override
	public ItemWriter<? super CustomerExtendModel> classify(CustomerExtendModel customer) {
		String path = ".\\output\\classifilerMultifile\\Customer"+customer.getYearMonth()+".csv";
		itemWriter.setResource(new FileSystemResource(path));
		return itemWriter;
	}

}