package com.ecommerce.project.service;

import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        removeCouponFromCartIfMinimumPriceLess(user);
    }

    public double getCartTotalWithCouponDiscount(User user) {

        double total = user.getCart().getCartItems()
                .stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();

        Coupon coupon = user.getCart().getCoupon();

        if(coupon!=null) {
            if (coupon.getDiscountType().equals("ABSOLUTE")) {
                total = total - coupon.getDiscountValue();
            }

            if (coupon.getDiscountType().equals("PERCENTAGE")) {
                total = total - (coupon.getDiscountValue()/100*total);
            }
        }

        return total;
    }

    public double getCartTotalWithoutCouponDiscount(User user) {
        return user.getCart().getCartItems()
                .stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    public boolean addProductToCart(User user, Product product) {

        if (isProductEqualStock(user.getCart(), product) || isProductOutOfStock(user.getCart(), product))
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

        removeCouponFromCartIfMinimumPriceLess(user);

        return true;
    }

    public void removeCouponFromCartIfMinimumPriceLess(User user) {
        Optional<Cart> cartOptional = cartRepository.findByUser(user);
        if (cartOptional.isEmpty())
            return;
        Cart cart = cartOptional.get();
        if (cart.getCoupon()==null)
            return;
        double minimumPurchase = cart.getCoupon().getMinimumPurchase();
        double cartTotal = cart.getCartItems().stream().mapToDouble(CartItem::getTotalPrice).sum();
        if (cartTotal < minimumPurchase) {
            cart.setCoupon(null);
            cartRepository.save(cart);
        }
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

    public boolean isProductInStock(Cart cart, Product product) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, cart);
        if (cartItemOptional.isEmpty()) {
            return product.getQuantity()>0;
        }
        return cartItemOptional.get().getQuantity() < product.getQuantity();
    }

    public boolean isProductEqualStock(Cart cart, Product product) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, cart);
        if (cartItemOptional.isEmpty())
            return false;
        return cartItemOptional.get().getQuantity() == product.getQuantity();
    }

    public boolean isProductOutOfStock(Cart cart, Product product) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, cart);
        if (cartItemOptional.isEmpty())
            return product.getQuantity() == 0;
        return cartItemOptional.get().getQuantity() > product.getQuantity();
    }

    public boolean isCartEmpty(Cart cart) {
        return cart.getCartItems().isEmpty();
    }

    @Transactional
    public void clearCart(Cart cart) {
        cartItemRepository.deleteByCart(cart);
    }

    public void applyCouponToCart(User user, Coupon coupon) {
        Cart cart = user.getCart();
        cart.setCoupon(coupon);
        cartRepository.save(cart);
    }

    public void removeCouponFromCart(User user) {

        Cart cart = user.getCart();
        cart.setCoupon(null);
        cartRepository.save(cart);

    }

    @Transactional
    public void removeCouponFromCart(int couponId) {
        cartRepository.removeCouponFromCart(couponId);
    }

    public boolean hasMinimumPurchase(Coupon coupon, User user) {
        return getCartTotalWithoutCouponDiscount(user) >= coupon.getMinimumPurchase();
    }

}
