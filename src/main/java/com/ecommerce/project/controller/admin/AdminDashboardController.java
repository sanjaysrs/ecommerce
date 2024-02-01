package com.ecommerce.project.controller.admin;

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

        //Chart Weekly (Last 7 days)
        model.addAttribute("subtitleWeeklyOrders", "Orders over the last 7 days");
        model.addAttribute("subtitleWeeklyRevenue", "Revenue over the last 7 days");
        model.addAttribute("chartDataWeeklyOrders", chartService.getChartDataLastSevenDaysOrders());
        model.addAttribute("chartDataWeeklyRevenue", chartService.getChartDataLastSevenDaysSales());

        //Chart Monthly
        model.addAttribute("chartData", chartService.getChartDataLastThirtyDaysOrders().get(0));
        model.addAttribute("chartData2", chartService.getChartDataLastThirtyDaysOrders().get(1));
        model.addAttribute("barColors", chartService.getBarColors());
        model.addAttribute("chartData3", chartService.getChartDataLastThirtyDaysSales().get(0));
        model.addAttribute("chartData4", chartService.getChartDataLastThirtyDaysSales().get(1));

        //Chart yearly
        model.addAttribute("graphData1", chartService.getChartDataLastTwelveMonthsOrders().get(0));
        model.addAttribute("graphData2", chartService.getChartDataLastTwelveMonthsOrders().get(1));
        model.addAttribute("graphData3", chartService.getChartDataLastTwelveMonthsSales().get(0));
        model.addAttribute("graphData4", chartService.getChartDataLastTwelveMonthsSales().get(1));

        //Chart last 24 hours
        model.addAttribute("dailyList", chartService.getChartDataDailyOrders());
        model.addAttribute("dailyList2", chartService.getChartDataDailyRevenue());

        //Chart last 5 years
        model.addAttribute("labelFive", chartService.getLastFiveYears().get(0));
        model.addAttribute("dataFive", chartService.getLastFiveYears().get(1));
        model.addAttribute("labelFive2", chartService.getLastFiveYears2().get(0));
        model.addAttribute("dataFive2", chartService.getLastFiveYears2().get(1));

        //pie chart 1
        model.addAttribute("pie1", chartService.getPieChart());
        model.addAttribute("pie2", chartService.getPieChart2());

        return "adminHome";
    }

}
