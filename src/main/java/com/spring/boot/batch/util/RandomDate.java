package com.spring.boot.batch.util;

import java.util.Random;

public class RandomDate {
	
	public static String generate() {
		Random rand = new Random();
		int year = rand.nextInt(2020 - 2000 + 1) + 2000;
		int month = rand.nextInt(12 - 1 + 1) + 1;
		String monthStr = month < 10 ? "0"+month : String.valueOf(month);
		
		return String.valueOf(year) + monthStr +"01";
	}
}
