package com.ecommerce.project.service;

import com.ecommerce.project.entity.Category;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.*;
import java.util.*;

@Service
public class ChartService {

    @Autowired
    OrderService orderService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    OrderRepository orderRepository;

    public List<List<Object>> getChartDataLastSevenDaysOrders() {
        return orderRepository.countOrdersLastSevenDays();
    }

    public List<List<Object>> getChartDataLastSevenDaysSales() {
        return orderRepository.sumTotalPriceLastSevenDays();
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

    public List<List<Object>> getLastFiveYears() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate fiveYearsAgo = today.minusYears(4);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(fiveYearsAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }


        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<Integer, Integer> mapOrdersFive = new HashMap<>();

        for (int i=0;i<5;i++) {
            mapOrdersFive.put(today.minusYears(i).getYear(), 0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersFive.put(localDate.getYear(), mapOrdersFive.get(localDate.getYear()) + 1);
        }

        for (int i=0;i<5;i++) {
            rowsInGraph.add(today.minusYears(i).getYear());
            rowsInGraph2.add(mapOrdersFive.get(today.minusYears(i).getYear()));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getLastFiveYears2() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate fiveYearsAgo = today.minusYears(4);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(fiveYearsAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }


        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<Integer, Double> mapOrdersFive = new HashMap<>();

        for (int i=0;i<5;i++) {
            mapOrdersFive.put(today.minusYears(i).getYear(), 0.0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersFive.put(localDate.getYear(), mapOrdersFive.get(localDate.getYear()) + order.getTotalPrice());
        }

        for (int i=0;i<5;i++) {
            rowsInGraph.add(today.minusYears(i).getYear());
            rowsInGraph2.add(mapOrdersFive.get(today.minusYears(i).getYear()));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getPieChart() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<Category> categoryList = categoryService.getAllCategories();

        Map<Category, Integer> categoryIntegerMap = new HashMap<>();

        for (Category category : categoryList) {
            categoryIntegerMap.put(category, 0);
        }

        for (Order order : validOrders) {
            List<OrderItem> orderItemList = order.getOrderItems();
            for (OrderItem orderItem : orderItemList) {
                Category category = orderItem.getProduct().getCategory();
                categoryIntegerMap.put(category, categoryIntegerMap.get(category) + orderItem.getQuantity());
            }
        }

        List<List<Object>> rowsInGraph = new ArrayList<>();

        for (Category category : categoryList) {
            rowsInGraph.add(Arrays.asList(category.getName(),categoryIntegerMap.get(category)));
        }

        return rowsInGraph;
    }

    public List<List<Object>> getPieChart2() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<Category> categoryList = categoryService.getAllCategories();

        Map<Category, Double> categoryDoubleMap = new HashMap<>();

        for (Category category : categoryList) {
            categoryDoubleMap.put(category, 0.0);
        }

        for (Order order : validOrders) {
            List<OrderItem> orderItemList = order.getOrderItems();
            for (OrderItem orderItem : orderItemList) {
                Category category = orderItem.getProduct().getCategory();
                categoryDoubleMap.put(category, categoryDoubleMap.get(category) + orderItem.getSubtotal());
            }
        }

        List<List<Object>> rowsInGraph = new ArrayList<>();

        for (Category category : categoryList) {
            rowsInGraph.add(Arrays.asList(category.getName(),categoryDoubleMap.get(category)));
        }

        return rowsInGraph;
    }

    public List<String> getBarColors() {
        return Arrays.asList("black","maroon","red","purple","fuchsia","green","lime","olive","yellow","navy","blue","teal","aqua",
                "blueviolet","brown","burlywood","cadetblue","chartreuse","chocolate","cornflowerblue","crimson","cyan","darkblue","darkcyan",
                "darkgoldenrod","darkgreen","darkmagenta","darkorange","darkslategrey","firebrick");
    }
}
