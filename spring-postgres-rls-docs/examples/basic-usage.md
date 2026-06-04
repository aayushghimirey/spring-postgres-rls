# Basic Usage

To use `@UseRls`, you must ensure that you have configured your database tables with Row Level Security.

1. Configure your data source in `application.yml` and enable the starter:
```yaml
spring:
  rls:
    enabled: true
    validation-mode: STRICT
    tables:
      - name: my_table
```

2. Wrap any service method that requires tenant isolation with `@UseRls` and `@Transactional`.
```java
@Service
public class MyService {

    @UseRls
    @Transactional
    public List<MyEntity> fetchIsolatedData() {
        // ...
    }
}
```

3. Ensure you inject the specific tenant or context key before calling the method:
```java
RlsContextHolder.setTenantId(123L);
myService.fetchIsolatedData();
// RlsContextHolder is automatically cleared by the interceptor after execution!
```
