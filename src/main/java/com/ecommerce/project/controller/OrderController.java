package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.entity.OrderStatus;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.OrderStatusService;
import com.ecommerce.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private CartService cartService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(authentication.getName());
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {

        List<Order> orders = orderService.getOrdersByUser(getCurrentUser());
        Collections.reverse(orders);
        model.addAttribute("userOrders", orders);
        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));

        return "orders";
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId,
                                   Model model) {

        Order order = orderService.getOrderById(orderId);
        if (order == null)
            return "redirect:/orders";

        model.addAttribute("order", order);
        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        model.addAttribute("urlList", storageService.getUrlListForSingleOrder(order));

        return "orderDetails";
    }

    @GetMapping("/cancelOrder/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes redirectAttributes) {

        orderService.cancelOrder(orderId);
        double refund = orderService.refundIfWallet(orderId, getCurrentUser());
        if (refund!=0)
            redirectAttributes.addFlashAttribute("moneyCredited", "Invoice amount of ₹" + refund + " has been credited back to your wallet");

        return "redirect:/orders/" + orderId;
    }

}
