package lk.ijse.eca.productservice.service;

import lk.ijse.eca.productservice.dto.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO createProductWithImage(ProductDTO productDTO, MultipartFile image) throws IOException;
    ProductDTO getProductById(Long id);
    List<ProductDTO> getAllProducts();
    List<ProductDTO> getProductsByCategory(String category);
    List<ProductDTO> searchProducts(String name);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
    boolean isProductAvailable(Long id, Integer quantity);
    void decreaseStock(Long id, Integer quantity);
}
