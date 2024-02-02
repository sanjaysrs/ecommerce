package com.ecommerce.project.service;

import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.OrderRepository;
import com.ecommerce.project.repository.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderStatusRepository orderStatusRepository;

    @Autowired
    AddressService addressService;

    @Autowired
    PaymentMethodService paymentMethodService;

    @Autowired
    WalletService walletService;

    public List<Order> getAllNonCancelledOrders() {
        return orderRepository.findByOrderStatusIdNotOrderByDateDesc(6);
    }

    public long getCountOfAllNonCancelledOrders() {
        return orderRepository.countByOrderStatusIdNot(6);
    }

    public double getSalesOfAllNonCancelledOrders() {
        double sales = orderRepository.sumTotalPriceByOrderStatusIdNot(6);
        return Math.round(sales*100)/100.0;
    }

    public long getCountOfAllOrdersIncludingCancelled() {
        return orderRepository.count();
    }

    public long getCountOfAllPlacedOrders() {
        return orderRepository.countByOrderStatusId(1);
    }

    public long getCountOfAllPackedOrders() {
        return orderRepository.countByOrderStatusId(2);
    }

    public long getCountOfAllShippedOrders() {
        return orderRepository.countByOrderStatusId(3);
    }

    public long getCountOfAllInTransitOrders() {
        return orderRepository.countByOrderStatusId(4);
    }

    public long getCountOfAllDeliveredOrders() {
        return orderRepository.countByOrderStatusId(5);
    }

    public long getCountOfAllCancelledOrders() {
        return orderRepository.countByOrderStatusId(6);
    }

    public long getCountOfOrdersMadeToday() {
        return orderRepository.countByDate(LocalDate.now());
    }

    public List<Order> getOrdersMadeToday() {
        return orderRepository.findOrdersMadeToday();
    }

    public double getSalesMadeToday() {
        double sales = orderRepository.sumTotalPriceByDate(LocalDate.now());
        return Math.round(sales * 100)/100.0;
    }

    public long getCountOfOrdersMadeThisWeek() {
        return orderRepository.countOrdersForThisWeek();
    }

    public double getSalesMadeThisWeek() {
        double sales = orderRepository.sumTotalPriceForThisWeek();
        return Math.round(sales * 100)/100.0;
    }

    public List<Order> getOrdersMadeThisWeek() {
        return orderRepository.findOrdersMadeThisWeek();
    }

    public long getCountOfOrdersMadeThisMonth() {
        return orderRepository.countOrdersForThisMonth();
    }

    public double getSalesMadeThisMonth() {
        double sales = orderRepository.sumTotalPriceForThisMonth();
        return Math.round(sales * 100)/100.0;
    }

    public List<Order> getOrdersMadeThisMonth() {
        return orderRepository.findOrdersMadeThisMonth();
    }

    public long getCountOfOrdersMadeThisYear() {
        return orderRepository.countOrdersForThisYear();
    }

    public double getSalesMadeThisYear() {
        double sales = orderRepository.sumTotalPriceForThisYear();
        return Math.round(sales * 100)/100.0;
    }

    public List<Order> getOrdersMadeThisYear() {
        return orderRepository.findOrdersMadeThisYear();
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findAllByUser(user);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).get();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void createOrderAndSave(User user, Long selectedAddressId, int paymentMethodId, double total) {

        Address address = addressService.getAddressById(selectedAddressId);
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(paymentMethodId);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);

        order.setOrderDate(new Date());
        order.setDate(LocalDateTime.now());

        order.setTotalPrice(total);

        OrderStatus orderStatus = orderStatusRepository.findById(1).get();
        order.setOrderStatus(orderStatus);

        addOrderItems(user.getCart(), order);
        orderRepository.save(order);
    }

    private void addOrderItems(Cart cart, Order order) {

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

    }

    public void cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderStatus orderStatus = orderStatusRepository.findById(6).orElseThrow();
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
    }

    public double refundIfWallet(Long orderId, User user) {
        Order order = getOrderById(orderId);

        if (order.getPaymentMethod().getId()!=3)
            return 0;

        double refundAmount = order.getTotalPrice();
        Wallet wallet = user.getWallet();
        wallet.setAmount(wallet.getAmount() + refundAmount);
        walletService.save(wallet);
        return refundAmount;

    }
}


















