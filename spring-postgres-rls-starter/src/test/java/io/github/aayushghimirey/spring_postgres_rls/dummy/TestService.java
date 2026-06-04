package io.github.aayushghimirey.spring_postgres_rls.dummy;

import io.github.aayushghimirey.spring_postgres_rls.annotations.UseRls;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService {

    @UseRls
    @Transactional
    public void testInjection() {

    }
}
