package com.prorenta.financeservice.model.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;

import java.util.List;

@Builder
@JacksonXmlRootElement(localName = "ValCurs")
public record ValCursResponseDto (

        @JacksonXmlProperty(localName = "Valute")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<Valute> currencies
) {
    @Builder
    public record Valute (

            @JacksonXmlProperty(localName = "CharCode")
            String charCode,

            @JacksonXmlProperty(localName = "Value")
            String value
    ) {
    }
}
