package com.ecommerce.project.service;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.ProductImage;
import com.ecommerce.project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    StorageService storageService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductImageService productImageService;

    public void updateProduct(Product product, ProductDTO productDTO, List<MultipartFile> files) {
        product.setName(productDTO.getName());
        product.setCategory(categoryService.getCategoryById(productDTO.getCategoryId()).get());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDescription(productDTO.getDescription());

        if (!files.get(0).isEmpty()) {
            productImageService.deleteAllByProductId(productDTO.getId());

            List<ProductImage> productImageList = new ArrayList<>();

            for (int i=0; i<files.size();i++) {
                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                String fileName = storageService.uploadFile(files.get(i));
                productImage.setImageName(fileName);
                productImageList.add(productImage);
            }
            product.setProductImages(productImageList);
        }

        productRepository.save(product);
    }

    public boolean isNameChanged(Product product, ProductDTO productDTO) {
        return !product.getName().equals(productDTO.getName());
    }

    public ProductDTO getProductDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setCategoryId(product.getCategory().getId());
        productDTO.setPrice(product.getPrice());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setDescription(product.getDescription());
        return productDTO;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public void saveProduct(ProductDTO productDTO, List<MultipartFile> files) {

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setCategory(categoryService.getCategoryById(productDTO.getCategoryId()).get());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDescription(productDTO.getDescription());

        List<ProductImage> productImageList = new ArrayList<>();

        for (MultipartFile file : files) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            String fileName = storageService.uploadFile(file);
            productImage.setImageName(fileName);
            productImageList.add(productImage);
        }

        product.setProductImages(productImageList);
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

    public void addStock(Long id, long quantity) {
        productRepository.increaseProductQuantity(id, quantity);
    }

    public int removeStock(Long id, long quantity) {
        return productRepository.decreaseProductQuantity(id, quantity);
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    public boolean isFilesEmpty(List<MultipartFile> files) {
        return files.size()==1 && files.get(0).isEmpty();
    }

    public List<Product> getThreeProductsWithDistinctCategory() {
        return productRepository.findDistinctProductsByCategory();
    }
}
