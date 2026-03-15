package com.weatherbroadcast;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.main.lazy-initialization=true",
		"firebase.database-url=https://example.firebaseio.com"
})
class SpringWeatherBroadcastApplicationTests {

	@Test
	void contextLoads() {
	}

}
