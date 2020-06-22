package com.spring.boot.batch.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemWriter;

import com.spring.boot.batch.writer.MultiFlatFileCustomWriter;

public class CompositeModel {
	private String yearMonth;
	private MultiFlatFileCustomWriter<CustomerExtendModel> writer;
	private List<CustomerExtendModel> items;

	public CompositeModel(String yearMonth, MultiFlatFileCustomWriter<CustomerExtendModel> writer) {
		this.yearMonth = yearMonth;
		this.writer = writer;
		this.items = new ArrayList<>();
	}

	public String getYearMonth() {
		return yearMonth;
	}

	public void setYearMonth(String yearMonth) {
		this.yearMonth = yearMonth;
	}

	public List<CustomerExtendModel> getItems() {
		return items;
	}

	public void setItems(List<CustomerExtendModel> items) {
		this.items = items;
	}

	public MultiFlatFileCustomWriter<CustomerExtendModel> getWriter() {
		return writer;
	}

	public void setWriter(MultiFlatFileCustomWriter<CustomerExtendModel> writer) {
		this.writer = writer;
	}
}

