package com.ecommerce.project.jasperreports.salesreport.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.jasperreports.salesreport.entity.Report;
import com.ecommerce.project.service.OrderService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    public byte[] exportReportHelper(String format, List<Order> orders, Double grandTotal, Long quantity, Long totalOrders) throws JRException {
        List<Report> reportList = new ArrayList<>();

        for (Order order : orders) {
            Report report = new Report();
            report.setOrderId(order.getId());
            report.setOrderDate(order.getDate().toLocalDate());
            report.setCustomer(order.getUser().getEmail());
            report.setTotalAmount(order.getTotalPrice());
            report.setPaymentMethod(order.getPaymentMethod().getMethod());
            List<OrderItem> orderItemList = order.getOrderItems();

            for (int i=0; i<orderItemList.size();i++) {
                if (i==0)
                    report.setProducts(orderItemList.get(i).getProduct().getName() + " - " + orderItemList.get(i).getQuantity() + " nos.");
                else
                    report.setProducts(report.getProducts() + ", " + orderItemList.get(i).getProduct().getName() + " - " + orderItemList.get(i).getQuantity() + " nos.");
            }
            report.setOrderStatus(order.getOrderStatus().getStatus());
            reportList.add(report);
        }

        //Load jrxml file and compile it
        S3Object s3Object = s3Client.getObject(bucketName, "jasperReports/salesreport1.jrxml");
        InputStream jrxmlInputStream = s3Object.getObjectContent();
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);

        //Map resource to file
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportList);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("grandTotal", grandTotal);
        parameters.put("quantity", quantity);
        parameters.put("totalOrders", totalOrders);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        //Export to pdf
        if (format.equalsIgnoreCase("pdf")) {
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            return pdfBytes;
        }

        //Export to xls
        if (format.equalsIgnoreCase("xlsx")) {

            JRBeanCollectionDataSource dataSource2 = new JRBeanCollectionDataSource(reportList);
            parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);
            JasperPrint jasperPrint2 = JasperFillManager.fillReport(jasperReport, parameters, dataSource2);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint2);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, outputStream);
            exporter.exportReport();
            return outputStream.toByteArray();
        }

        return null;
    }

    public byte[] exportReport(String format) throws JRException {

        List<Order> orders = orderService.getAllNonCancelledOrders();

        Double grandTotal = Math.round(orderService.getSalesOfAllNonCancelledOrders()*100.0)/100.0;
        Long quantity = orderService.getTotalQuantityOfProductsSold();
        Long totalOrders = orderService.getCountOfAllNonCancelledOrders();

        return exportReportHelper(format, orders, grandTotal, quantity, totalOrders);

    }

    public byte[] exportReportForDateRange(LocalDate startDate,
                                           LocalDate endDate,
                                           String format) throws JRException {

        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);

        List<Order> orders = orderService.getOrdersByOrderDateBetween(startDateTime, endDateTime);

        Double grandTotal = Math.round(orderService.getTotalPriceSumBetweenDates(startDateTime, endDateTime)*100.0)/100.0;
        Long quantity = orderService.getTotalQuantityOfProductsSoldOrderDateBetween(startDateTime, endDateTime);
        Long totalOrders = orderService.getCountOfOrdersBetweenDates(startDateTime, endDateTime);

        return exportReportHelper(format, orders, grandTotal, quantity, totalOrders);

    }

}
















