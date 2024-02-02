package com.ecommerce.project.controller.admin;

import com.ecommerce.project.entity.Order;
import com.ecommerce.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class AdminSalesReportController {

    @Autowired
    OrderService orderService;

    @GetMapping("/admin/salesReport")
    public String salesReport(Model model) {

        List<Order> orders = orderService.getAllNonCancelledOrders();

        model.addAttribute("ALL", true);
        model.addAttribute("orderFilter", "All orders");
        model.addAttribute("userOrders", orders);
        model.addAttribute("totalSales", orderService.getSalesOfAllNonCancelledOrders());
        return "salesReport";

    }

    @PostMapping("/admin/salesReport")
    public String filterSalesReport(@ModelAttribute("dateFilter") String dateFilter, Model model) {

        List<Order> userOrders = orderService.getAllOrders();
        List<Order> filteredUserOrders = new ArrayList<>(userOrders.stream().filter(order -> order.getOrderStatus().getId() != 6).toList());
        Collections.reverse(filteredUserOrders);

        List<LocalDate> localDateList = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : filteredUserOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList.add(localDate);
        }

        LocalDate today = LocalDate.now();

        switch (dateFilter) {
            case "ALL" -> {
                return "redirect:/admin/salesReport";
            }
            case "DAILY" -> {
                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isEqual(today)) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("DAILY", "DAILY");

                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Daily orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";
            }
            case "WEEKLY" -> {

                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isAfter(startOfWeek.minusDays(1)) && localDate.isBefore(endOfWeek.plusDays(1))) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("WEEKLY", "WEEKLY");


                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Weekly orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";

            }
            case "MONTHLY" -> {

                LocalDate startOfMonth = today.withDayOfMonth(1);
                LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isAfter(startOfMonth.minusDays(1)) && localDate.isBefore(endOfMonth.plusDays(1))) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("MONTHLY", "MONTHLY");

                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Monthly orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";
            }
            case "YEARLY" -> {

                LocalDate startOfYear = today.withDayOfYear(1);
                LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());

                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isAfter(startOfYear.minusDays(1)) && localDate.isBefore(endOfYear.plusDays(1))) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("YEARLY", "YEARLY");

                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Yearly orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";

            }
        }

        return "redirect:/admin/salesReport";
    }
}
