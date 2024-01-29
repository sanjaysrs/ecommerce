package com.ecommerce.project.service;

import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    CouponService couponService;

    public void deleteCartItemFromCart(long id, User user) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(id);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            if (cartItem.getCart().getUser().equals(user))
                cartItemRepository.delete(cartItem);
        }
    }

    public double getCartTotal(User user) {

        return user.getCart().getCartItems()
                .stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    private boolean isProductInStock(Product product) {
        return product.getQuantity()>0;
    }

    public boolean addProductToCart(User user, Product product) {

        if (!isProductInStock(product) || isQuantityInCartEqualToOrGreaterThanStock(product, user.getCart()))
            return false;

        Cart cart = user.getCart();

        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, cart);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(1);
            cartItemRepository.save(cartItem);
        }

        return true;
    }

    public boolean removeProductFromCart(User user, Product product) {

        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, user.getCart());

        if (cartItemOptional.isEmpty())
            return false;

        CartItem cartItem = cartItemOptional.get();

        if (cartItem.getQuantity()>1) {
            cartItem.setQuantity(cartItem.getQuantity()-1);
            cartItemRepository.save(cartItem);
        } else if (cartItem.getQuantity()==1) {
            cartItemRepository.delete(cartItem);
        }

        return true;
    }

    public void save(Cart cart) {
        cartRepository.save(cart);
    }

    public int getCartCount(User user) {
        return user.getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
    }

    public int getQuantityOfProductInCart(Product product, Cart cart) {

        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, cart);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            return cartItem.getQuantity();
        }

        return  0;

    }

    public boolean isQuantityInCartEqualToOrGreaterThanStock(Product product, Cart cart) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, cart);
        if (cartItemOptional.isEmpty())
            return false;
        return product.getQuantity()<=cartItemOptional.get().getQuantity();
    }

    public boolean isCartEmpty(Cart cart) {
        return cart.getCartItems().isEmpty();
    }

    public void clearCart(Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            cartItemRepository.delete(cartItem);
        }
    }

    public void applyCouponToCart(User user, Coupon coupon) {
        Cart cart = user.getCart();
        cart.setCoupon(coupon);
        cartRepository.save(cart);
    }
}
