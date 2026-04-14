package com.inseong.dallyrun.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DallyrunBackendApplicationTests {

    @Test
    void applicationClassIsAnnotated() {
        assertNotNull(DallyrunBackendApplication.class.getAnnotation(SpringBootApplication.class));
    }
}
