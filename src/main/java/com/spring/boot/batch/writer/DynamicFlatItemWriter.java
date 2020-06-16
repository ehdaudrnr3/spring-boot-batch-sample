package com.spring.boot.batch.writer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.Resource;

public class DynamicFlatItemWriter<T> implements ItemStream, ItemWriter<T> {
	
	private Map<String, FlatFileItemWriter<T>> writers = new HashMap<>();
	
	private LineAggregator<T> lineAggregator;
	private Resource resource;
    private ExecutionContext executionContext;
    
    public void setLineAggregator(LineAggregator<T> lineAggregator) {
		this.lineAggregator = lineAggregator;
	}
    
	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T item : items) {
            FlatFileItemWriter<T> ffiw = getFlatFileItemWriter(item);
            ffiw.write(Arrays.asList(item));
        }		
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		 this.executionContext = executionContext;
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws ItemStreamException {
		// TODO Auto-generated method stub
		
	}
	
	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public FlatFileItemWriter<T> getFlatFileItemWriter(T item) {
        String fileName = getResource().getFilename();
        FlatFileItemWriter<T> rr = writers.get(fileName);
        if(rr == null){
            rr = new FlatFileItemWriter<>();
            rr.setLineAggregator(lineAggregator);
            try {
                rr.setResource(getResource());
                rr.open(executionContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            writers.put(fileName, rr);
        }
        return rr;
    }

}
