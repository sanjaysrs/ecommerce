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

        if (!model.containsAttribute("orderFilter"))
            model.addAttribute("orderFilter", "All orders");
        if (!model.containsAttribute("userOrders"))
            model.addAttribute("userOrders", orders);
        if (!model.containsAttribute("totalSales"))
            model.addAttribute("totalSales", orderService.getSalesOfAllNonCancelledOrders());
        if (!model.containsAttribute("totalOrders"))
            model.addAttribute("totalOrders", orderService.getCountOfAllNonCancelledOrders());
        return "salesReport";

    }

    @PostMapping("/admin/salesReport")
    public String filterSalesReport(@ModelAttribute("dateFilter") String dateFilter, RedirectAttributes redirectAttributes) {

        switch (dateFilter) {
            case "ALL" -> {
                return "redirect:/admin/salesReport";
            }
            case "TODAY" -> {

                redirectAttributes.addFlashAttribute("orderFilter", "Orders today");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeToday());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeToday());
                redirectAttributes.addFlashAttribute("totalOrders", orderService.getCountOfOrdersMadeToday());
                return "redirect:/admin/salesReport";
            }
            case "THIS WEEK" -> {

                redirectAttributes.addFlashAttribute("orderFilter", "Orders this week");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeThisWeek());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeThisWeek());
                redirectAttributes.addFlashAttribute("totalOrders", orderService.getCountOfOrdersMadeThisWeek());
                return "redirect:/admin/salesReport";

            }
            case "THIS MONTH" -> {

                redirectAttributes.addFlashAttribute("orderFilter", "Orders this month");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeThisMonth());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeThisMonth());
                redirectAttributes.addFlashAttribute("totalOrders", orderService.getCountOfOrdersMadeThisMonth());
                return "redirect:/admin/salesReport";
            }
            case "THIS YEAR" -> {

                redirectAttributes.addFlashAttribute("orderFilter", "Orders this year");
                redirectAttributes.addFlashAttribute("userOrders", orderService.getOrdersMadeThisYear());
                redirectAttributes.addFlashAttribute("totalSales", orderService.getSalesMadeThisYear());
                redirectAttributes.addFlashAttribute("totalOrders", orderService.getCountOfOrdersMadeThisYear());
                return "redirect:/admin/salesReport";

            }
        }

        return "redirect:/admin/salesReport";
    }
}
