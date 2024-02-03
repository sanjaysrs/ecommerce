package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class AdminController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderStatusService orderStatusService;

    @Autowired
    StorageService storageService;

    @GetMapping("/admin/categories")
    public String getCategories(@ModelAttribute("exception") String exception,
                                @ModelAttribute("deleted") String deleted,
                                Model model) {

        if (!exception.isEmpty())
            model.addAttribute("exceptionCheck", "Exception Check");
        if (!deleted.isEmpty())
            model.addAttribute("deletedCheck", "Deleted Check");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @PostMapping("/admin/categories")
    public String searchCategories(@ModelAttribute("name") String name, Model model) {

        List<Category> categoryList = categoryService.getAllCategories();

        List<Category> searchResult = new ArrayList<>();

        for (Category category : categoryList) {
            if (category.getName().toLowerCase().contains(name.toLowerCase())) {
                searchResult.add(category);
            }
        }

        if (searchResult.isEmpty()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("searchError", "Category could not be found.");
            return "categories";
        }

        model.addAttribute("categories", searchResult);
        return "categories";

    }

    @GetMapping("/admin/categories/add")
    public String addCategories(Model model) {
        model.addAttribute("category", new Category());
        return "categoriesAdd";
    }

    @PostMapping("/admin/categories/add")
    public String postAddCategories(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model
            ) {

        if (bindingResult.hasErrors()){
            return "categoriesAdd";
        }

        Optional<Category> existing = categoryService.getCategoryByName(category.getName());

        if (existing.isPresent()) {
            model.addAttribute("category", new Category());
            model.addAttribute("categoryError", "Category already exists.");
            return "categoriesAdd";
        }

        categoryService.addCategory(category);
        return "redirect:/admin/categories";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategoryById(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("exception", "Cannot delete this category as products under this category have been ordered.");
            return "redirect:/admin/categories";
        }
        redirectAttributes.addFlashAttribute("deleted", "Category and the products under it have been deleted.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/update/{id}")
    public String updateCategory(@PathVariable int id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "categoriesAdd";
        }
        return "404";
    }

    @GetMapping("/admin/products")
    public String getProducts(@ModelAttribute("exception") String exception,
                              @ModelAttribute("deleted") String deleted,
                              @ModelAttribute("addOrUpdate") String addOrUpdate,
                              Model model) {

        if (!addOrUpdate.isEmpty())
            model.addAttribute("addOrUpdateCheck", "addOrUpdateCheck");
        if (!exception.isEmpty())
            model.addAttribute("exceptionCheck", "Exception Check");
        if (!deleted.isEmpty())
            model.addAttribute("deletedCheck", "Deleted Check");
        List<Product> products = productService.getAllProducts();
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }

    @GetMapping("/admin/products/filter/{id}")
    public String filterProductByCategory(@PathVariable("id") int id, Model model) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }

    @PostMapping("/admin/products")
    public String searchProducts(@ModelAttribute("name") String name, Model model) {

        List<Product> productList = productService.getAllProducts();

        List<Product> searchResult = new ArrayList<>();

        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(name.toLowerCase())) {
                searchResult.add(product);
            }
        }

        if (searchResult.isEmpty()) {
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("searchError", "Your search did not match any products.");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
            return "products";
        }

        model.addAttribute("urlList", storageService.getUrlList(searchResult));
        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "products";

    }

    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            productService.removeProductById(id);
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("exception", "Cannot delete a product that has already been ordered.");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("deleted", "The product was deleted.");
        return "redirect:/admin/products";
    }
    @GetMapping("/admin/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.findAll().stream().filter(User::isVerified).toList());
        return "users";
    }

    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/enable/{id}")
    public String enableUser(@PathVariable("id") Integer id) {
        User user = userService.findById(id);
        user.setEnabled(true);
        userService.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/disable/{id}")
    public String disableUser(@PathVariable("id") Integer id) {
        User user = userService.findById(id);
        user.setEnabled(false);
        userService.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        List<Order> userOrders = orderService.getAllOrders();
        Collections.reverse(userOrders);
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

        //13-10-2023 coupon check
        List<OrderItem> orderItems = order.getOrderItems();
        double actualTotal=0.0;
        for (OrderItem orderItem : orderItems) {
            actualTotal += orderItem.getProduct().getPrice() * orderItem.getQuantity();
        }
        if (order.getTotalPrice()!=actualTotal)
            model.addAttribute("couponApplied", "Coupon Applied!");

        model.addAttribute("urlList", storageService.getUrlListForOrder(order));

        return "orderDetailsAdmin";
    }

    @PostMapping("/admin/orders/{orderId}")
    public String orderStatus(
            @PathVariable("orderId") Long orderId,
            @ModelAttribute("status") int status,
            Model model) {

        OrderStatus orderStatus = orderStatusService.findById(status);

        Order order = orderService.getOrderById(orderId);
        order.setOrderStatus(orderStatus);
        orderService.saveOrder(order);

        model.addAttribute("statuses", orderStatusService.findAll());
        model.addAttribute("order", order);

        //13-10-2023 coupon check
        List<OrderItem> orderItems = order.getOrderItems();
        double actualTotal=0.0;
        for (OrderItem orderItem : orderItems) {
            actualTotal += orderItem.getProduct().getPrice() * orderItem.getQuantity();
        }
        if (order.getTotalPrice()!=actualTotal)
            model.addAttribute("couponApplied", "Coupon Applied!");

        model.addAttribute("urlList", storageService.getUrlListForOrder(order));
        return "orderDetailsAdmin";
    }

    @GetMapping("/admin/inventory")
    public String getInventory(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
        return "inventory";
    }

    @GetMapping("/admin/inventory/update/{id}")
    public String updateStock(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).get();
        model.addAttribute("product", product);
        return "updateStock";
    }

    @PostMapping("/admin/inventory/addStock/{id}")
    public String addStock(@PathVariable Long id, @ModelAttribute("addStock") long add, Model model) {
        Product product = productService.getProductById(id).get();
        product.setQuantity(product.getQuantity() + add);
        productService.addProduct(product);

        List<Product> products = new ArrayList<>();
        products.add(product);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("stockStatus", "Stock added successfully");
        return "inventory";
    }

    @PostMapping("/admin/inventory/removeStock/{id}")
    public String removeStock(@PathVariable Long id, @ModelAttribute("removeStock") long subtract, Model model) {

        Product product = productService.getProductById(id).get();

        if (product.getQuantity()<subtract) {
            List<Product> products = new ArrayList<>();
            products.add(product);
            model.addAttribute("products", products);
            model.addAttribute("urlList", storageService.getUrlList(products));
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("stockStatus", "Could not remove required stock due to insufficient stock in inventory");
            return "inventory";
        }

        product.setQuantity(product.getQuantity() - subtract);
        productService.addProduct(product);

        List<Product> products = new ArrayList<>();
        products.add(product);
        model.addAttribute("products", products);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("stockStatus", "Stock removed successfully");
        return "inventory";
    }

    @PostMapping("/admin/inventory")
    public String searchInventory(@ModelAttribute("name") String name, Model model) {

        List<Product> productList = productService.getAllProducts();

        List<Product> searchResult = new ArrayList<>();

        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(name.toLowerCase())) {
                searchResult.add(product);
            }
        }

        if (searchResult.isEmpty()) {
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("searchError", "Your search did not match any products.");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
            return "inventory";
        }

        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(searchResult));

        return "inventory";

    }

    @GetMapping("/admin/inventory/filter/{id}")
    public String filterInventoryByCategory(@PathVariable("id") int id, Model model) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        model.addAttribute("products", products);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "inventory";
    }


    @GetMapping("/admin/wallets")
    public String getAdminWallets(Model model) {

        model.addAttribute("users", userService.findAll().stream().filter(x->x.getWallet()!=null).toList());

        return "walletsAdmin";
    }

}
















