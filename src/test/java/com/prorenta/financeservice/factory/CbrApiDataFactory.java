package com.prorenta.financeservice.factory;

public class CbrApiDataFactory {

    public static final String cbrFeignClientResponse = """
                <?xml version="1.0" encoding="windows-1251"?>
                <ValCurs Date="01.09.2026" name="Foreign Currency Market">
                    <Valute ID="R01235">
                        <NumCode>840</NumCode>
                        <CharCode>USD</CharCode>
                        <Nominal>1</Nominal>
                        <Name>Доллар США</Name>
                        <Value>89,5012</Value>
                        <VunitRate>89,5012</VunitRate>
                    </Valute>
                </ValCurs>
                """;
}
