package lk.ijse.eca.productservice.controller;

import lk.ijse.eca.productservice.dto.ProductDTO;
import lk.ijse.eca.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        log.info("Creating product: {}", productDTO.getName());
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProductWithImage(
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart("image") MultipartFile image) throws IOException {
        log.info("Creating product with image: {}", productDTO.getName());
        ProductDTO created = productService.createProductWithImage(productDTO, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        boolean available = productService.isProductAvailable(id, quantity);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<Map<String, String>> decreaseStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        productService.decreaseStock(id, quantity);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Stock decreased successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "product-service");
        return ResponseEntity.ok(response);
    }
}
