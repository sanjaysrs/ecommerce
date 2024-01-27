package com.ecommerce.project.service;

import com.ecommerce.project.entity.Product;
import com.ecommerce.project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void addProduct(Product product) {
        productRepository.save(product);
    }

    public void removeProductById(Long id) {
        productRepository.deleteById(id);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProductsByCategoryId(int id) {
        return productRepository.findAllByCategory_Id(id);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    public List<Product> getProductsByNameContaining(String keyword) {
        return productRepository.findAllByNameContaining(keyword);
    }

    public List<Product> getProductsByNameAndCategory(String keyword, int categoryId) {
        return productRepository.findAllByNameContainingAndCategory_Id(keyword, categoryId);
    }

    public List<Product> getSearchResult(String keyword, int categoryId) {
        if (categoryId==0)
            return getProductsByNameContaining(keyword);
        return getProductsByNameAndCategory(keyword, categoryId);
    }

    public boolean isProductInStock(Product product) {
        return product.getQuantity()>0;
    }

}
