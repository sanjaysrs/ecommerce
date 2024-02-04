package com.ecommerce.project.jasperreports.invoice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.jasperreports.invoice.entity.InvoiceDataset;
import com.ecommerce.project.service.OrderService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceService {

    @Autowired
    OrderService orderService;

    @Autowired
    private AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    public byte[] generateInvoice(Long id) throws JRException {

        Order order = orderService.getOrderById(id);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("customerName", order.getUser().getFirstName() + " " + order.getUser().getLastName());
        parameters.put("customerEmail", order.getUser().getEmail());
        parameters.put("customerAddress", order.getAddress().getFullAddress());
        parameters.put("orderId", order.getId());
        parameters.put("orderDate", order.getDate().toLocalDate());
        parameters.put("orderStatus", order.getOrderStatus().getStatus());
        parameters.put("paymentMethod", order.getPaymentMethod().getMethod());
        parameters.put("total", order.getTotalPrice());
        if (order.isCouponApplied())
            parameters.put("couponApplied", "YES");
        else
            parameters.put("couponApplied", "NO");

        List<InvoiceDataset> invoiceDatasetList = new ArrayList<>();

        List<OrderItem> orderItemList = order.getOrderItems();

        for (OrderItem orderItem : orderItemList) {
            InvoiceDataset invoiceDataset = new InvoiceDataset();
            invoiceDataset.setProductName(orderItem.getProduct().getName());
            invoiceDataset.setQuantity(orderItem.getQuantity());
            invoiceDataset.setUnitPrice(orderItem.getProduct().getPrice());
            invoiceDataset.setStotal(orderItem.getSubtotal());

            invoiceDatasetList.add(invoiceDataset);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(invoiceDatasetList);

        parameters.put("invoiceDataset", dataSource);

        //Load jrxml file and compile it
        S3Object s3Object = s3Client.getObject(bucketName, "jasperReports/invoice.jrxml");
        InputStream jrxmlInputStream = s3Object.getObjectContent();
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
        return pdfBytes;
    }

}
