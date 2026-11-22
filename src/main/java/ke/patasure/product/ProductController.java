
package ke.patasure.product;

import ke.patasure.auth.AppUser;
import ke.patasure.auth.AppUserRepository;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final AppUserRepository userRepository;

    public ProductController(
            ProductRepository productRepository,
            AppUserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse registerProduct(
            @Valid @RequestBody Product product,
            Authentication authentication) {

        AppUser seller = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Seller account not found"));

        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        return ProductResponse.from(product);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public List<ProductResponse> getMyProducts(
            Authentication authentication) {

        AppUser seller = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found"));

        return productRepository.findBySellerId(seller.getId())
                .stream()
                .map(ProductResponse::from)
                .toList();
    }
}
