package com.spring.boot.batch.job.writer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;
import org.springframework.classify.ClassifierSupport;
import org.springframework.util.Assert;

import com.spring.boot.batch.model.CompositeModel;
import com.spring.boot.batch.model.CustomerExtendModel;

public class CustomCompositeItemWriter implements ItemWriter<CustomerExtendModel> {
	
	private Classifier<CustomerExtendModel, CompositeModel> classifier = new ClassifierSupport<>(null);

	public void setClassifier(Classifier<CustomerExtendModel, CompositeModel> classifier) {
		Assert.notNull(classifier, "A classifier is required.");
		this.classifier = classifier;
	}

	
	@Override
	public void write(List<? extends CustomerExtendModel> items) throws Exception {
		// TODO Auto-generated method stub
		Map<String, CompositeModel> map = new LinkedHashMap<>();

		for (CustomerExtendModel item : items) {
			CompositeModel model = classifier.classify(item);
			
			String yearMonth = model.getYearMonth();
			if (!map.containsKey(yearMonth)) {
				map.put(yearMonth, model);
			}
			map.get(yearMonth).getItems().add(item);
		}

		for (String yearMonth : map.keySet()) {
			MultiFlatFileCustomWriter<CustomerExtendModel> itemWriter = map.get(yearMonth).getWriter();
			List<CustomerExtendModel> list = map.get(yearMonth).getItems();
			
			itemWriter.open(new ExecutionContext());
			itemWriter.write(list);
			itemWriter.close();
		}
	}

}
