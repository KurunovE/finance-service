package com.prorenta.financeservice.integration.integration_tests;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.prorenta.financeservice.factory.CbrApiDataFactory;
import com.prorenta.financeservice.integration.CbrFeignClient;
import com.prorenta.financeservice.model.dto.ValCursResponseDto;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class CbrFeignClientIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CbrFeignClient cbrFeignClient;

    @Test
    @DisplayName("Получение курсов валют: успешно")
    public void testGetDailyRatesParsesXmlSuccessfully() {
        String xmlResponse = CbrApiDataFactory.cbrFeignClientResponse;

        WireMock.stubFor(get(urlPathEqualTo("/XML_daily.asp"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=windows-1251")
                        .withBody(xmlResponse)));

        ValCursResponseDto response = cbrFeignClient.getDailyRates(null);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.currencies()).hasSize(1);
        Assertions.assertThat(response.currencies().getFirst().charCode()).isEqualTo("USD");
        Assertions.assertThat(response.currencies().getFirst().value()).isEqualTo("89,5012");
    }
}
