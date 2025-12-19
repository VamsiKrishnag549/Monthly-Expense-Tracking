package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonthlyExpenseTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonthlyExpenseTrackerApplication.class, args);
	}

}
