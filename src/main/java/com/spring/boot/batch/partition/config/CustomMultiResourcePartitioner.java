package com.spring.boot.batch.partition.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import lombok.Setter;

@Setter
public class CustomMultiResourcePartitioner implements Partitioner {

	private static final String PARTITION_KEY = "partition";

	private Resource[] resources = new Resource[0];

	private String keyName = "fileName";

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<>();
		
		int k = 1;
		for (int j = 0; j < resources.length; j++) {
			Resource resource = resources[j];
			ExecutionContext context = new ExecutionContext();
			Assert.state(resource.exists(), "Resource does not exist: " + resource);
			context.putString(keyName, resource.getFilename());
			context.putString("opFileName", "output" + k++ + ".xml");
			
			map.put(PARTITION_KEY + j, context);
		}
		return map;
	}

}
