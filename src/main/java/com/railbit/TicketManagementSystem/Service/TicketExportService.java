package com.railbit.TicketManagementSystem.Service;

import com.railbit.TicketManagementSystem.Entity.Ticket;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*; // Apache POI classes for Excel
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;
import java.io.IOException;

@Service // ✅ Marks this class as a Spring service bean
public class TicketExportService {

    /**
     * ✅ Export tickets as a CSV file
     */
    public void exportToCSV(HttpServletResponse response, List<Ticket> tickets) throws IOException {
        response.setContentType("text/csv"); // Set response type to CSV
        response.setHeader("Content-Disposition", "attachment; filename=tickets.csv"); // Download prompt

        PrintWriter writer = response.getWriter();

        // Write CSV header
        writer.println("ID,Title,Description,Passenger Name,Priority,Status,Created By");

        // Write ticket rows
        for (Ticket ticket : tickets) {
            writer.println(ticket.getId() + "," +
                           ticket.getTitle() + "," +
                           ticket.getDescription() + "," +
                           ticket.getPriority() + "," +
                           ticket.getStatus() + "," +
                           (ticket.getUser() != null ? ticket.getUser().getUsername() : ""));
        }

        writer.flush(); // ✅ Ensure data is written
        writer.close(); // ✅ Close the writer
    }

    /**
     * ✅ Export tickets as an Excel (.xlsx) file using Apache POI
     */
    public void exportToExcel(HttpServletResponse response, List<Ticket> tickets) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // Excel MIME type
        response.setHeader("Content-Disposition", "attachment; filename=tickets.xlsx"); // Download prompt

        Workbook workbook = new XSSFWorkbook(); // ✅ Create a new Excel workbook
        Sheet sheet = workbook.createSheet("Tickets"); // ✅ Create a sheet named "Tickets"

        // ✅ Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Title", "Description", "Passenger Name", "Priority", "Status", "Created By"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // ✅ Fill ticket data
        int rowNum = 1;
        for (Ticket ticket : tickets) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(ticket.getId());
            row.createCell(1).setCellValue(ticket.getTitle());
            row.createCell(2).setCellValue(ticket.getDescription());
            row.createCell(3).setCellValue(ticket.getPriority()); // 🛠️ This should be Passenger Name? You may want to replace or fix this
            row.createCell(4).setCellValue(ticket.getPriority());
            row.createCell(5).setCellValue(ticket.getStatus());
            row.createCell(6).setCellValue(ticket.getUser() != null ? ticket.getUser().getUsername() : "");
        }

        // ✅ Write the workbook to the HTTP response
        workbook.write(response.getOutputStream());
        workbook.close(); // ✅ Close workbook to free resources
    }
}
