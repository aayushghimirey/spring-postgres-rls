package io.github.aayushghimirey.spring_postgres_rls.sample.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    private Long id;

    private Long tenantId;

    private String name;

    public Employee() {}

    public Employee(Long id, Long tenantId, String name) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
