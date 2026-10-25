
package com.jobscheduler;
import org.springframework.http.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SchedulerApiApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;


	@Test
	void createJobShouldReturnSavedJob() throws Exception {
		String requestBody = """
        {
            "jobType": "INTEGRATION_TEST",
            "payload": "{\\"task\\":\\"test-job\\"}",
            "priority": 3,
            "maxAttempts": 3
        }
        """;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/jobs",
				request,
				String.class
		);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());

		JsonNode job = objectMapper.readTree(response.getBody());

		assertTrue(job.hasNonNull("id"));
		assertEquals("INTEGRATION_TEST", job.get("jobType").asText());
		assertEquals("SCHEDULED", job.get("status").asText());
		assertEquals(3, job.get("priority").asInt());
		assertEquals(3, job.get("maxAttempts").asInt());
		assertEquals(0, job.get("attempts").asInt());
	}


	@Test
	void getJobByIdShouldReturnSavedJob() throws Exception {
		String requestBody = """
        {
            "jobType": "GET_TEST",
            "payload": "{\\"task\\":\\"retrieve-job\\"}",
            "priority": 4,
            "maxAttempts": 3
        }
        """;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<String> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/jobs",
				request,
				String.class
		);

		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
		assertNotNull(createResponse.getBody());

		JsonNode createdJob = objectMapper.readTree(createResponse.getBody());
		String jobId = createdJob.get("id").asText();

		ResponseEntity<String> getResponse = restTemplate.getForEntity(
				"http://localhost:" + port + "/api/jobs/" + jobId,
				String.class
		);

		assertEquals(HttpStatus.OK, getResponse.getStatusCode());
		assertNotNull(getResponse.getBody());

		JsonNode retrievedJob = objectMapper.readTree(getResponse.getBody());
		assertEquals(jobId, retrievedJob.get("id").asText());
		assertEquals("GET_TEST", retrievedJob.get("jobType").asText());
	}


}
