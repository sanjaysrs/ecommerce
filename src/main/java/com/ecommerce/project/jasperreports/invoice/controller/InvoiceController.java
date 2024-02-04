package com.ecommerce.project.jasperreports.invoice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.jasperreports.invoice.entity.InvoiceDataset;
import com.ecommerce.project.jasperreports.invoice.service.InvoiceService;
import com.ecommerce.project.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jdk.dynalink.linker.LinkerServices;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InvoiceController {

    @Autowired
    InvoiceService invoiceService;

    @GetMapping("/invoice/{id}")
    public void generateInvoice(@PathVariable Long id,
                                HttpServletResponse response) throws IOException, JRException {

        byte[] pdfBytes = invoiceService.generateInvoice(id);

        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
        response.setContentType("application/pdf");

        response.getOutputStream().write(pdfBytes);

    }

}


















