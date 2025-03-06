package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.JasperReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private JasperReportService reportService;

    @GetMapping("/{format}/{startDate}/{endDate}")
    public ResponseEntity<String> generateReport(@PathVariable String format, @PathVariable LocalDate startDate, @PathVariable LocalDate endDate) {
        try {
            String message = reportService.exportReport(format,startDate,endDate);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating report: " + e.getMessage());
        }
    }
}
