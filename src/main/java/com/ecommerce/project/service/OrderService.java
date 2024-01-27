package com.ecommerce.project.service;

import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.OrderRepository;
import com.ecommerce.project.repository.OrderStatusRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class OrderService {

    private static final String KEY = "rzp_test_amAJ6g1mhBlQKL";

    private static final String KEY_SECRET = "xW9gfY6xByn88aKq8GixUNZ0";

    private static final String CURRENCY = "INR";

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderStatusRepository orderStatusRepository;

    @Autowired
    AddressService addressService;

    @Autowired
    PaymentMethodService paymentMethodService;

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

    public TransactionDetails createTransaction(Double amount) {

        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("amount", amount * 100);
            jsonObject.put("currency", CURRENCY);

            RazorpayClient razorpayClient = new RazorpayClient(KEY, KEY_SECRET);

            com.razorpay.Order order = razorpayClient.orders.create(jsonObject);
            return prepareTransactionDetails(order);

        } catch (RazorpayException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public TransactionDetails prepareTransactionDetails(com.razorpay.Order order) {
        String orderId = order.get("id");
        String currency = order.get("currency");
        Integer amount = order.get("amount");

        TransactionDetails transactionDetails = new TransactionDetails(orderId, currency, amount);
        return transactionDetails;
    }

    public void createOrderAndSave(User user, Long selectedAddressId, int paymentMethodId, double total) {

        Address address = addressService.getAddressById(selectedAddressId);
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(paymentMethodId);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);

        Date date = new Date();
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        date.setTime(date.getTime() + istTimeZone.getRawOffset());
        order.setOrderDate(date);

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

}


















