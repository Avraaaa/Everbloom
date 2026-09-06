package com.everbloom.service;

import com.everbloom.model.PopularItem;
import com.everbloom.model.SalesReportRow;
import com.everbloom.repository.ReportRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<SalesReportRow> findSalesByDate(LocalDate startDate, LocalDate endDate) throws SQLException {
        validateDates(startDate, endDate);
        return reportRepository.findSalesByDate(startDate.toString(), endDate.toString());
    }

    public List<PopularItem> findPopularFlowers(LocalDate startDate, LocalDate endDate) throws SQLException {
        validateDates(startDate, endDate);
        return reportRepository.findPopularFlowers(startDate.toString(), endDate.toString());
    }

    public List<PopularItem> findPopularExtras(LocalDate startDate, LocalDate endDate) throws SQLException {
        validateDates(startDate, endDate);
        return reportRepository.findPopularExtras(startDate.toString(), endDate.toString());
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Select both report dates.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after the start date.");
        }
    }
}
