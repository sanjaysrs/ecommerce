package com.ecommerce.project.controller.admin;

import com.ecommerce.project.entity.Order;
import com.ecommerce.project.service.AdminDashboardService;
import com.ecommerce.project.service.ChartService;
import com.ecommerce.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminDashboardController {

    @Autowired
    AdminDashboardService adminDashboardService;

    @Autowired
    ChartService chartService;

    @Autowired
    OrderService orderService;

    @GetMapping("/admin")
    public String adminHome(Model model) {

        //All Time Orders
        model.addAttribute("allTimeOrders", orderService.getCountOfAllNonCancelledOrders());
        model.addAttribute("allTimeSales", orderService.getSalesOfAllNonCancelledOrders());

        //Stat Update
        model.addAttribute("totalOrdersIncl", orderService.getCountOfAllOrdersIncludingCancelled());
        model.addAttribute("totalOrders", orderService.getCountOfAllNonCancelledOrders());
        model.addAttribute("ordersPlaced", orderService.getCountOfAllPlacedOrders());
        model.addAttribute("ordersPacked", orderService.getCountOfAllPackedOrders());
        model.addAttribute("ordersShipped", orderService.getCountOfAllShippedOrders());
        model.addAttribute("ordersInTransit", orderService.getCountOfAllInTransitOrders());
        model.addAttribute("ordersDelivered", orderService.getCountOfAllDeliveredOrders());
        model.addAttribute("ordersCancelled", orderService.getCountOfAllCancelledOrders());

        //Orders today
        List<Order> dailyOrders = adminDashboardService.getDailyOrders();
        model.addAttribute("dailyOrders", dailyOrders.size());
        model.addAttribute("dailySales", Math.round(dailyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //Orders this week
        List<Order> weeklyOrders = adminDashboardService.getWeeklyOrders();
        model.addAttribute("weeklyOrders", weeklyOrders.size());
        model.addAttribute("weeklySales", Math.round(weeklyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //Orders this month
        List<Order> monthlyOrders = adminDashboardService.getMonthlyOrders();
        model.addAttribute("monthlyOrders", monthlyOrders.size());
        model.addAttribute("monthlySales", Math.round(monthlyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //Orders this year
        List<Order> yearlyOrders = adminDashboardService.getYearlyOrders();
        model.addAttribute("yearlyOrders", yearlyOrders.size());
        model.addAttribute("yearlySales", Math.round(yearlyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //Chart Weekly (Last 7 days)
        model.addAttribute("subtitleWeeklyOrders", "Orders weekly (last 7 days)");
        model.addAttribute("subtitleWeeklyRevenue", "Revenue weekly (last 7 days)");
        model.addAttribute("chartDataWeeklyOrders", chartService.getChartDataWeeklyOrders());
        model.addAttribute("chartDataWeeklyRevenue", chartService.getChartDataWeeklyRevenue());

        //Chart Monthly
        model.addAttribute("chartData", chartService.getChartDataMonthlyOrders().get(0));
        model.addAttribute("chartData2", chartService.getChartDataMonthlyOrders().get(1));
        model.addAttribute("barColors", chartService.getBarColors());
        model.addAttribute("chartData3", chartService.getChartDataMonthlyRevenue().get(0));
        model.addAttribute("chartData4", chartService.getChartDataMonthlyRevenue().get(1));

        //Chart yearly
        model.addAttribute("graphData1", chartService.getChartDataYearlyOrders().get(0));
        model.addAttribute("graphData2", chartService.getChartDataYearlyOrders().get(1));
        model.addAttribute("graphData3", chartService.getChartDataYearlyRevenue().get(0));
        model.addAttribute("graphData4", chartService.getChartDataYearlyRevenue().get(1));

        //Chart last 24 hours
        model.addAttribute("dailyList", chartService.getChartDataDailyOrders());
        model.addAttribute("dailyList2", chartService.getChartDataDailyRevenue());

        //Chart last 5 years
        model.addAttribute("labelFive", chartService.getLastFiveYears().get(0));
        model.addAttribute("dataFive", chartService.getLastFiveYears().get(1));
        model.addAttribute("labelFive2", chartService.getLastFiveYears2().get(0));
        model.addAttribute("dataFive2", chartService.getLastFiveYears2().get(1));

        //line chart 1
        model.addAttribute("lh1", chartService.getLineChart().get(0));
        model.addAttribute("gh1", chartService.getLineChart().get(1));
        model.addAttribute("lh2", chartService.getLineChart2().get(0));
        model.addAttribute("gh2", chartService.getLineChart2().get(1));

        //line chart 2
        model.addAttribute("hal1", chartService.getLineChartDaily1().get(0));
        model.addAttribute("hal2", chartService.getLineChartDaily1().get(1));
        model.addAttribute("gal1", chartService.getLineChartDaily2().get(0));
        model.addAttribute("gal2", chartService.getLineChartDaily2().get(1));

        //pie chart 1
        model.addAttribute("pie1", chartService.getPieChart());
        model.addAttribute("pie2", chartService.getPieChart2());

        return "adminHome";
    }

}
