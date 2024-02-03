package com.ecommerce.project.controller.admin;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.OrderStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AdminOrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderStatusService orderStatusService;

    @Autowired
    StorageService storageService;

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        List<Order> userOrders = orderService.getAllOrders();
        model.addAttribute("userOrders", userOrders);
        return "ordersAdmin";
    }

    @GetMapping("/admin/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/admin/orders";
        }

        model.addAttribute("statuses", orderStatusService.findAll());
        model.addAttribute("order", order);
        if (order.isCouponApplied())
            model.addAttribute("couponApplied", "Coupon Applied!");
        model.addAttribute("urlList", storageService.getUrlListForOrder(order));

        return "orderDetailsAdmin";
    }

    @PostMapping("/admin/orders/{orderId}")
    public String orderStatus(@PathVariable("orderId") Long orderId,
                              @ModelAttribute("status") int orderStatusId) {

        orderService.updateOrderStatus(orderId, orderStatusId);
        return "redirect:/admin/orders/" + orderId;
    }
}
