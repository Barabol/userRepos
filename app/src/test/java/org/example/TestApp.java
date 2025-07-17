package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestApp {

	@Autowired
	private TestRestTemplate template;

	@LocalServerPort
	private int port;
	private final String testUser = "Barabol";

	@Test
	public void testHappyPath() {
		final String URL = "http://127.0.0.1:" + port + "/";
		System.out.println("Request: "+ URL + testUser);

		ResponseEntity<String> res = template.getForEntity(URL + testUser, String.class);
		int responseCode = res.getStatusCodeValue();

		System.out.println("Status code: " + responseCode);
		assertTrue(responseCode == 200);

		String rawJson = res.getBody();
		assertNotNull(rawJson);
		System.out.println("JSON: " + rawJson);
	}
}
