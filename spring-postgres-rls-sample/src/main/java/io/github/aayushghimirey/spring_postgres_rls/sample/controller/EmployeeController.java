package io.github.aayushghimirey.spring_postgres_rls.sample.controller;

import io.github.aayushghimirey.spring_postgres_rls.core.RlsContextHolder;
import io.github.aayushghimirey.spring_postgres_rls.sample.entity.Employee;
import io.github.aayushghimirey.spring_postgres_rls.sample.service.EmployeeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> getEmployees(@RequestHeader("X-Tenant-ID") String tenantId) {
        // Set tenant context for RLS and always clear it afterwards.
        RlsContextHolder.setTenantId(tenantId);
        try {
            return employeeService.getAllEmployeesForTenant();
        } finally {
            RlsContextHolder.clear();
        }
    }
}
