package com.prorenta.financeservice.service.impl.integration_tests.util;

import org.springframework.boot.test.context.TestComponent;
import java.util.UUID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TestComponent
public class UserClientHelper {

    public void mockUserInfo(UUID userId) {
        stubFor(
                get(urlMatching("/api/v1/users/" + userId))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {
                                                    "id": "%s",
                                                    "name": "Test User"
                                                }
                                                """.formatted(userId.toString()))
                        )
        );
    }
}