package com.ecommerce.project.controller.admin;

import com.ecommerce.project.repository.OrderItemRepository;
import com.ecommerce.project.repository.OrderRepository;
import com.ecommerce.project.service.ChartService;
import com.ecommerce.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    @Autowired
    ChartService chartService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @GetMapping("/admin")
    public String adminHome(Model model) {

        //Stat Update
        model.addAttribute("totalSales", orderService.getSalesOfAllNonCancelledOrders());
        model.addAttribute("totalOrdersIncludingCancelled", orderService.getCountOfAllOrdersIncludingCancelled());
        model.addAttribute("totalOrdersExcludingCancelled", orderService.getCountOfAllNonCancelledOrders());
        model.addAttribute("ordersPlaced", orderService.getCountOfAllPlacedOrders());
        model.addAttribute("ordersPacked", orderService.getCountOfAllPackedOrders());
        model.addAttribute("ordersShipped", orderService.getCountOfAllShippedOrders());
        model.addAttribute("ordersInTransit", orderService.getCountOfAllInTransitOrders());
        model.addAttribute("ordersDelivered", orderService.getCountOfAllDeliveredOrders());
        model.addAttribute("ordersCancelled", orderService.getCountOfAllCancelledOrders());

        //Orders today
        model.addAttribute("ordersToday", orderService.getCountOfOrdersMadeToday());
        model.addAttribute("salesToday", orderService.getSalesMadeToday());

        //Orders this week
        model.addAttribute("ordersThisWeek", orderService.getCountOfOrdersMadeThisWeek());
        model.addAttribute("salesThisWeek", orderService.getSalesMadeThisWeek());

        //Orders this month
        model.addAttribute("ordersThisMonth", orderService.getCountOfOrdersMadeThisMonth());
        model.addAttribute("salesThisMonth", orderService.getSalesMadeThisMonth());

        //Orders this year
        model.addAttribute("ordersThisYear", orderService.getCountOfOrdersMadeThisYear());
        model.addAttribute("salesThisYear", orderService.getSalesMadeThisYear());


        // Chart.js chart data
        model.addAttribute("barColors", chartService.getBarColors());

        model.addAttribute("lastSevenDaysOrders", chartService.getChartDataLastSevenDaysOrders());
        model.addAttribute("lastSevenDaysOrdersTitle", "Orders over the last 7 days");
        model.addAttribute("lastSevenDaysSales", chartService.getChartDataLastSevenDaysSales());
        model.addAttribute("lastSevenDaysSalesTitle", "Revenue over the last 7 days");

        model.addAttribute("lastThirtyDaysOrders", chartService.getChartDataLastThirtyDaysOrders());
        model.addAttribute("lastThirtyDaysOrdersTitle", "Orders over the last 30 days");
        model.addAttribute("lastThirtyDaysSales", chartService.getChartDataLastThirtyDaysSales());
        model.addAttribute("lastThirtyDaysSalesTitle", "Revenue over the last 30 days");

        model.addAttribute("lastTwelveMonthsOrders", chartService.getChartDataLastTwelveMonthsOrders());
        model.addAttribute("lastTwelveMonthsOrdersTitle", "Orders over the last 12 months");
        model.addAttribute("lastTwelveMonthsSales", chartService.getChartDataLastTwelveMonthsSales());
        model.addAttribute("lastTwelveMonthsSalesTitle", "Revenue over the last 12 months");

        model.addAttribute("todayHourlyOrders", chartService.getChartDataTodayByHourOrders());
        model.addAttribute("todayHourlyOrdersTitle", "Orders today (hourly)");
        model.addAttribute("todayHourlySales", chartService.getChartDataTodayByHourSales());
        model.addAttribute("todayHourlySalesTitle", "Revenue today (hourly)");

        model.addAttribute("lastFiveYearsOrders", chartService.getChartDataLastFiveYearsOrders());
        model.addAttribute("lastFiveYearsOrdersTitle", "Orders today (hourly)");
        model.addAttribute("lastFiveYearsSales", chartService.getChartDataLastFiveYearsSales());
        model.addAttribute("lastFiveYearsSalesTitle", "Revenue today (hourly)");

        //Google charts chart data
        System.out.println(orderItemRepository.productsSoldByCategory());
        System.out.println(orderItemRepository.revenueByCategory());
        model.addAttribute("productsSoldByCategory", chartService.getChartDataProductsSoldByCategory());
        model.addAttribute("salesByCategory", chartService.getChartDataSalesByCategory());

        return "adminHome";
    }

}
