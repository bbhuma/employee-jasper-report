package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Data
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String designation;
    private Double salary;
    @Column(name="JOINING_DATE")
    private LocalDate joiningDate;
    @Column(name="ENDING_DATE")
    private LocalDate endingDate;
//	public Long getId() {
//		return id;
//	}
//	public void setId(Long id) {
//		this.id = id;
//	}
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	public String getDesignation() {
//		return designation;
//	}
//	public void setDesignation(String designation) {
//		this.designation = designation;
//	}
//	public Double getSalary() {
//		return salary;
//	}
//	public void setSalary(Double salary) {
//		this.salary = salary;
//	}
//	public LocalDate getJoiningDate() {
//		return joiningDate;
//	}
//	public void setJoiningDate(LocalDate joiningDate) {
//		this.joiningDate = joiningDate;
//	}
//	public LocalDate getEndingDate() {
//		return endingDate;
//	}
//	public void setEndingDate(LocalDate endingDate) {
//		this.endingDate = endingDate;
//	}
//	public Employee() {
//		super();
//	}
//	public Employee(Long id, String name, String designation, Double salary, LocalDate joiningDate,
//			LocalDate endingDate) {
//		super();
//		this.id = id;
//		this.name = name;
//		this.designation = designation;
//		this.salary = salary;
//		this.joiningDate = joiningDate;
//		this.endingDate = endingDate;
//	}
    
}

