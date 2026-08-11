package com.prorenta.financeservice.service.impl.unit_tests;

import com.prorenta.financeservice.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CategoryServiceImpl.class
})
public class CurrencyServiceImplUnitTest {

}
