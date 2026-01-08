package com.rentify.rentify_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-secret")
class RentifyApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
