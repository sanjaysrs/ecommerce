package com.ecommerce.project.service;

import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
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

}
