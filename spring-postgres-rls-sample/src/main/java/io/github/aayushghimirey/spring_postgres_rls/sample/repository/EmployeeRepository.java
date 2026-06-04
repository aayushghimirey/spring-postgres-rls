package io.github.aayushghimirey.spring_postgres_rls.sample.repository;

import io.github.aayushghimirey.spring_postgres_rls.sample.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
