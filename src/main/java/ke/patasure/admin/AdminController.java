
package ke.patasure.admin;

import ke.patasure.auth.AppUserRepository;
import ke.patasure.product.ProductRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;

    public AdminController(
            AppUserRepository userRepository,
            ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sellers/count")
    public Map<String, Long> getSellerCount() {
        return Map.of(
                "sellerCount",
                userRepository.countByRole("SELLER")
        );
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products/count")
    public Map<String, Long> getProductCount() {
        return Map.of(
                "productCount",
                productRepository.count()
        );
    }
}
