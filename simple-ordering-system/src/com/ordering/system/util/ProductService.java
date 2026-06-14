package com.ordering.system.util;

import com.ordering.system.dao.ProductDAO;
import com.ordering.system.model.Product;
import java.util.*;

public class ProductService extends AbstractService {
    private ProductDAO productDao = new ProductDAO();

    // Polymorphism: Implementing abstract method from AbstractService
    @Override
    public boolean validateInput(Object data) {
        if (!(data instanceof Product)) return false;
        Product p = (Product) data;
        return p.getItemName() != null && !p.getItemName().isEmpty() && p.getItemPrice() > 0;
    }

    @Override
    public double calculateTotal(List<?> items) {
        return 0.0; // Not used for product inventory management
    }

    public boolean addNewProduct(Product product) {
        if (!validateInput(product)) {
            System.err.println("[Error] Invalid product details.");
            return false;
        }
        return productDao.addProduct(product);
    }

    public List<Product> getAllProducts() {
        return productDao.getAllProducts();
    }

    public Optional<Product> searchProduct(String query) {
        Product p = productDao.searchProduct(query);
        return Optional.ofNullable(p);
    }

    public boolean updateStock(int id, int change) {
        Product p = productDao.getProductById(id);
        if (p == null) return false;
        int newQty = p.getItemQty() + change;
        if (newQty < 0) return false;
        return productDao.updateStock(id, newQty);
    }

    public boolean deleteProduct(int id) {
        return productDao.deleteProduct(id);
    }

    public Optional<Product> getProductById(int id) {
        return Optional.ofNullable(productDao.getProductById(id));
    }
}