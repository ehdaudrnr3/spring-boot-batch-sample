package com.spring.boot.batch.model;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@XmlRootElement(name = "transactionRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class Transaction {
	private String username;
	private int userId;
	private LocalDateTime transactionDate;
	private double amount;
	
	
	public Transaction(String username, int userId, LocalDateTime transactionDate, double amount) {
		this.username = username;
		this.userId = userId;
		this.transactionDate = transactionDate;
		this.amount = amount;	
	}
	
	private Transaction() {
		
	}
	
}
