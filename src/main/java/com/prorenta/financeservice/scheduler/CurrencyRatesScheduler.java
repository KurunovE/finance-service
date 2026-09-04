package com.prorenta.financeservice.scheduler;

import com.prorenta.financeservice.service.CurrencyRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyRatesScheduler {

    private final CurrencyRateService currencyRateService;

    @SchedulerLock(
            name = "currency-rates",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    @Scheduled(
            cron = "${scheduler.cdr-scheduler.cron}",
            zone = "${scheduler.cdr-scheduler.zone}"
    )
    public void scheduleCurrencySync() {
        log.info("Захвачена блокировка ShedLock. Синхронизация курсов валют");
        try {
            currencyRateService.aggregateRates();
        } catch (Exception e) {
            log.error("Критическая ошибка при синхронизации курсов валют", e);
        }
    }
}
