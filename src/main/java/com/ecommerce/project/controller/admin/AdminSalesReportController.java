package com.ecommerce.project.controller.admin;

import com.ecommerce.project.entity.Order;
import com.ecommerce.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminSalesReportController {

    @Autowired
    OrderService orderService;

    @GetMapping("/admin/salesReport")
    public String salesReport(Model model) {

        List<Order> orders = orderService.getAllNonCancelledOrders();

        if (!model.containsAttribute("DAILY") && !model.containsAttribute("WEEKLY") && !model.containsAttribute("MONTHLY") && !model.containsAttribute("YEARLY"))
            model.addAttribute("ALL", true);
        if (!model.containsAttribute("orderFilter"))
            model.addAttribute("orderFilter", "All orders");
        if (!model.containsAttribute("userOrders"))
            model.addAttribute("userOrders", orders);
        if (!model.containsAttribute("totalSales"))
            model.addAttribute("totalSales", orderService.getSalesOfAllNonCancelledOrders());
        return "salesReport";

    }

    @PostMapping("/admin/salesReport")
    public String filterSalesReport(@ModelAttribute("dateFilter") String dateFilter, RedirectAttributes redirectAttributes) {

        switch (dateFilter) {
            case "ALL" -> {
                return "redirect:/admin/salesReport";
            }
            case "DAILY" -> {

                redirectAttributes.addFlashAttribute("DAILY", true);
                redirectAttributes.addFlashAttribute("orderFilter", "Daily orders");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeToday());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeToday());
                return "redirect:/admin/salesReport";
            }
            case "WEEKLY" -> {

                redirectAttributes.addFlashAttribute("WEEKLY", true);
                redirectAttributes.addFlashAttribute("orderFilter", "Weekly orders");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeThisWeek());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeThisWeek());
                return "redirect:/admin/salesReport";

            }
            case "MONTHLY" -> {

                redirectAttributes.addFlashAttribute("MONTHLY", true);
                redirectAttributes.addFlashAttribute("orderFilter", "Monthly orders");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeThisMonth());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeThisMonth());
                return "redirect:/admin/salesReport";
            }
            case "YEARLY" -> {

                redirectAttributes.addFlashAttribute("YEARLY", true);
                redirectAttributes.addFlashAttribute("orderFilter", "Yearly orders");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeThisYear());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeThisYear());
                return "redirect:/admin/salesReport";

            }
        }

        return "redirect:/admin/salesReport";
    }
}
