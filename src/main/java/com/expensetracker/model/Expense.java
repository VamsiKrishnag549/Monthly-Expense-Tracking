package com.expensetracker.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate date;
	private String category;
	private double amount;
	private String description;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
}
