package lk.ijse.eca.productservice.service;

import lk.ijse.eca.productservice.dto.ProductDTO;
import lk.ijse.eca.productservice.entity.Product;
import lk.ijse.eca.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final GcsStorageService gcsStorageService;
    private final ModelMapper modelMapper;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);
        product.setId(null);
        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getId());
        return modelMapper.map(saved, ProductDTO.class);
    }

    @Override
    public ProductDTO createProductWithImage(ProductDTO productDTO, MultipartFile image) throws IOException {
        String imageUrl = gcsStorageService.uploadFile(image, "products");
        Product product = modelMapper.map(productDTO, Product.class);
        product.setId(null);
        product.setImageUrl(imageUrl);
        product.setImageGcsPath(imageUrl);
        Product saved = productRepository.save(product);
        log.info("Product created with image, id: {}", saved.getId());
        return modelMapper.map(saved, ProductDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> modelMapper.map(p, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(p -> modelMapper.map(p, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(p -> modelMapper.map(p, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        existing.setName(productDTO.getName());
        existing.setDescription(productDTO.getDescription());
        existing.setPrice(productDTO.getPrice());
        existing.setStock(productDTO.getStock());
        existing.setCategory(productDTO.getCategory());
        Product updated = productRepository.save(existing);
        return modelMapper.map(updated, ProductDTO.class);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        if (product.getImageGcsPath() != null) {
            gcsStorageService.deleteFile(product.getImageGcsPath());
        }
        productRepository.deleteById(id);
        log.info("Product deleted with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return product.getStock() >= quantity;
    }

    @Override
    public void decreaseStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + id);
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}
