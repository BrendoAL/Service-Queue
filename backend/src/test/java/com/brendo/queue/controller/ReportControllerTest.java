package com.brendo.queue.controller;

import com.brendo.queue.dto.response.CounterReportResponse;
import com.brendo.queue.dto.response.DailyTicketReportResponse;
import com.brendo.queue.dto.response.ReportSummaryResponse;
import com.brendo.queue.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerTest {

    private final ReportService reportService = mock(ReportService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new ReportController(reportService))
        .build();

    @Test
    void summaryReturnsReportSummary() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 12);
        when(reportService.summary(date, date))
            .thenReturn(new ReportSummaryResponse(date, date, 10, 2, 1, 1, 5, 1, 120, 600));

        mockMvc.perform(get("/api/reports/summary")
                .param("from", "2026-07-12")
                .param("to", "2026-07-12"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTickets").value(10))
            .andExpect(jsonPath("$.completedTickets").value(5))
            .andExpect(jsonPath("$.averageWaitSeconds").value(120));
    }

    @Test
    void dailyReturnsDailyReport() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 12);
        when(reportService.daily(date, date))
            .thenReturn(List.of(new DailyTicketReportResponse(date, 4, 3, 1)));

        mockMvc.perform(get("/api/reports/daily")
                .param("from", "2026-07-12")
                .param("to", "2026-07-12"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].totalTickets").value(4))
            .andExpect(jsonPath("$[0].completedTickets").value(3));
    }

    @Test
    void countersReturnsCounterReport() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 12);
        when(reportService.counters(date, date))
            .thenReturn(List.of(new CounterReportResponse(1L, "Guiche 1", 5, 4, 300)));

        mockMvc.perform(get("/api/reports/counters")
                .param("from", "2026-07-12")
                .param("to", "2026-07-12"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].counterId").value(1))
            .andExpect(jsonPath("$[0].counterName").value("Guiche 1"))
            .andExpect(jsonPath("$[0].completedTickets").value(4));
    }
}
