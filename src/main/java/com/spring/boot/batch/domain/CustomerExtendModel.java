package com.spring.boot.batch.domain;

import java.text.SimpleDateFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerExtendModel {
	
	private String firstName;
	private String lastName;
	private String year;
	private String yearMonth;
	private String time;
	
	public CustomerExtendModel(Customer customer) {
		this.firstName = customer.getFirstName();
		this.lastName = customer.getLastName();
		this.year = customer.getBirthDate().substring(0, 4);
		this.yearMonth = customer.getBirthDate().substring(0, 6);
		this.time = new SimpleDateFormat("HH:mm:ss").format(customer.getInsertDate());
	}
	
	public String getFileName(String writeDate) {
    	return "Customer_" +yearMonth+"_"+writeDate+".csv";
    }
}
