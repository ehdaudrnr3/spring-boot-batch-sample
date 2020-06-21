package com.spring.boot.batch.jobparameters;

import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomJobParameters {
	
	@Value("#{jobParameters[JobId]}")
	private String jobid;
	
}
