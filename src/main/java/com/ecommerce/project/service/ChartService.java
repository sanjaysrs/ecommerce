package com.ecommerce.project.service;

import com.ecommerce.project.repository.OrderItemRepository;
import com.ecommerce.project.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.*;
import java.util.*;

@Service
public class ChartService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    public List<List<Object>> getChartDataLastSevenDaysOrders() {
        return modifyLists(orderRepository.countOrdersLastSevenDays());
    }

    public List<List<Object>> getChartDataLastSevenDaysSales() {
        return modifyLists(orderRepository.sumTotalPriceLastSevenDays());
    }

    public List<List<Object>> getChartDataLastThirtyDaysOrders() {
        List<List<Object>> lists = orderRepository.countOrdersLastThirtyDays();
        modifyMonthLabels(lists);
        return modifyLists(lists);
    }

    public List<List<Object>> getChartDataLastThirtyDaysSales() {
        List<List<Object>> lists = orderRepository.sumTotalPriceLastThirtyDays();
        modifyMonthLabels(lists);
        return modifyLists(lists);
    }

    private void modifyMonthLabels(List<List<Object>> lists) {
        for (List<Object> list : lists) {
            Date sqlDate = (Date) list.get(0);
            LocalDate date = sqlDate.toLocalDate();
            list.add(0, date.getDayOfMonth() + " " + date.getMonth().name().substring(0, 3));
            list.remove(1);
        }
    }

    private List<List<Object>> modifyLists(List<List<Object>> lists) {
        List<Object> xData = new ArrayList<>();
        List<Object> yData = new ArrayList<>();
        for (List<Object> list : lists) {
            xData.add(list.get(0));
            yData.add(list.get(1));
        }
        return List.of(xData, yData);
    }

    public List<List<Object>> getChartDataLastTwelveMonthsOrders() {
        return getChartDataLastTwelveMonths(orderRepository.countOrdersLastTwelveMonths());
    }

    public List<List<Object>> getChartDataLastTwelveMonthsSales() {
        return getChartDataLastTwelveMonths(orderRepository.sumTotalPriceLastTwelveMonths());
    }

    public List<List<Object>> getChartDataLastTwelveMonths(List<List<Object>> lists) {
        List<List<Object>> modifiedList = modifyLists(lists);
        Month monthToday = LocalDate.now().getMonth();
        Map<Object, Object> map = new HashMap<>();
        for (Month month : Month.values()) {
            map.put(month.toString(), 0);
        }
        for (int i=0; i<modifiedList.get(0).size(); i++) {
            map.put(modifiedList.get(0).get(i).toString().toUpperCase(), modifiedList.get(1).get(i));
        }
        List<Object> xData = new ArrayList<>();
        List<Object> yData = new ArrayList<>();
        for (int i=0 ; i<12; i++) {
            xData.add(monthToday.minus(11-i).toString());
            yData.add(map.get(monthToday.minus(11-i).toString()));
        }
        return List.of(xData, yData);
    }

    public List<List<Object>> getChartDataTodayByHourOrders() {
        return modifyLists(orderRepository.countOrdersTodayByHour());
    }

    public List<List<Object>> getChartDataTodayByHourSales() {
        return modifyLists(orderRepository.sumTotalPriceTodayByHour());
    }

    public List<List<Object>> getChartDataLastFiveYearsOrders() {
        return modifyLists(orderRepository.countOrdersLastFiveYears());
    }

    public List<List<Object>> getChartDataLastFiveYearsSales() {
        return modifyLists(orderRepository.sumTotalPriceLastFiveYears());
    }

    public List<List<Object>> getChartDataProductsSoldByCategory() {
        return orderItemRepository.productsSoldByCategory();
    }

    public List<List<Object>> getChartDataSalesByCategory() {
        return orderItemRepository.revenueByCategory();
    }

    public List<String> getBarColors() {
        return Arrays.asList("black","maroon","red","purple","fuchsia","green","lime","olive","yellow","navy","blue","teal","aqua",
                "blueviolet","brown","burlywood","cadetblue","chartreuse","chocolate","cornflowerblue","crimson","cyan","darkblue","darkcyan",
                "darkgoldenrod","darkgreen","darkmagenta","darkorange","darkslategrey","firebrick");
    }
}
