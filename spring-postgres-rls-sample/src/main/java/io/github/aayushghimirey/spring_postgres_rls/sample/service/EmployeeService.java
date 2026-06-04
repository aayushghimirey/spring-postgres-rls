package io.github.aayushghimirey.spring_postgres_rls.sample.service;

import io.github.aayushghimirey.spring_postgres_rls.annotations.UseRls;
import io.github.aayushghimirey.spring_postgres_rls.sample.entity.Employee;
import io.github.aayushghimirey.spring_postgres_rls.sample.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    @UseRls
    public List<Employee> getAllEmployeesForTenant() {
        // Because of @UseRls, PostgreSQL will only return employees belonging to the tenant injected in RlsContextHolder
        return employeeRepository.findAll();
    }
}
