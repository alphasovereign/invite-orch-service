package com.alphabytes.invite.integration;

import com.alphabytes.invite.dto.CreateEventRequest;
import com.alphabytes.invite.dto.EventResponse;
import com.alphabytes.invite.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EventControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldCreateAndFetchEventForOwner() {
        CreateEventRequest request = new CreateEventRequest(
                "owner1",
                "Team Dinner",
                "Small get together",
                "NYC",
                LocalDateTime.of(2026, 4, 10, 19, 30)
        );

        ResponseEntity<EventResponse> createResponse = testRestTemplate.postForEntity(
                "/api/events",
                request,
                EventResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().id()).isNotNull();
        assertThat(createResponse.getBody().createdAt()).isNotNull();
        assertThat(createResponse.getBody().ownerId()).isEqualTo("owner1");

        ResponseEntity<List<EventResponse>> fetchResponse = testRestTemplate.exchange(
                "/api/events/owner/owner1",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(fetchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetchResponse.getBody()).hasSize(1);
        assertThat(fetchResponse.getBody().get(0).title()).isEqualTo("Team Dinner");
    }
}
