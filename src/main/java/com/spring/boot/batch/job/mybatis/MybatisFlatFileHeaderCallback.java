package com.spring.boot.batch.job.mybatis;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.batch.item.file.FlatFileHeaderCallback;

import com.spring.boot.batch.model.CustomerExtendModel;

public class MybatisFlatFileHeaderCallback implements FlatFileHeaderCallback {

	@Override
	public void writeHeader(Writer writer) throws IOException {
		Field[] fields = CustomerExtendModel.class.getDeclaredFields();
		String fillNames = Arrays.stream(fields)
				.map(Field::getName)
				.collect(Collectors.joining("|"));
		
		writer.write(fillNames);
	}

}
