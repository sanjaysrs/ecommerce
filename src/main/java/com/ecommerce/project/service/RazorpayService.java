package com.ecommerce.project.service;

import com.ecommerce.project.entity.TransactionDetails;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    private static final String KEY = "rzp_test_amAJ6g1mhBlQKL";

    private static final String KEY_SECRET = "xW9gfY6xByn88aKq8GixUNZ0";

    private static final String CURRENCY = "INR";

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

    private TransactionDetails prepareTransactionDetails(Order order) {
        String orderId = order.get("id");
        String currency = order.get("currency");
        Integer amount = order.get("amount");

        TransactionDetails transactionDetails = new TransactionDetails(orderId, currency, amount);
        return transactionDetails;
    }

}
