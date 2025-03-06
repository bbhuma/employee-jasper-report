package com.example.demo.service;

import java.io.InputStream;  // ✅ Correct
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
//import com.itextpdf.commons.utils.Base64.InputStream;
import com.itextpdf.io.exceptions.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class JasperReportService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    public String exportReport(String format, LocalDate startDate, LocalDate endDate) throws JRException, IOException {
//        LocalDate startDate = LocalDate.now().minusYears(2);
//        LocalDate endDate = LocalDate.now();

        List<Employee> employees = employeeRepository.findEmployeesBetweenDates(startDate, endDate);

        // Load and compile the Jasper report template
        InputStream reportStream = getClass().getResourceAsStream("/employee_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // Convert Employee list into Jasper Data Source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);

        // Set parameters (if any)
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "Spring Boot Jasper");

        // Fill the Jasper report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Export to HTML or PDF
        String filePath = "C:/reports/employee_report." + format;
        if (format.equalsIgnoreCase("html")) {
            JasperExportManager.exportReportToHtmlFile(jasperPrint, filePath);
        } else if (format.equalsIgnoreCase("pdf")) {
            JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
        }

        return "Report generated: " + filePath;
    }
}
