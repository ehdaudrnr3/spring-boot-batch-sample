package com.spring.boot.batch.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@ToString
@NoArgsConstructor
public class Customer {
	
    private Long id;
    private String firstName;
    private String lastName;
    private String birthDate;
    private Date insertDate;
    
    public String getFileName() {
    	return "Customer_" + getBirthDate().substring(0, 6)+".csv";
    }
}