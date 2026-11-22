
package ke.patasure.history;

import ke.patasure.auth.AppUser;
import ke.patasure.auth.AppUserRepository;
import ke.patasure.product.ProductRepository;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/history")
public class ProductHistoryController {

    private final ProductHistoryService historyService;
    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;

    public ProductHistoryController(
            ProductHistoryService historyService,
            AppUserRepository userRepository,
            ProductRepository productRepository) {
        this.historyService = historyService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductEvent recordEvent(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductHistoryRequest request,
            Authentication authentication) throws Exception {

        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Account not found"));

        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isAdmin &&
                !productRepository.existsByIdAndSellerId(productId, user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify another seller's product history");
        }

        return historyService.recordEvent(
                productId, request.eventType(), request.details());
    }

    @GetMapping
    public List<ProductEvent> getHistory(@PathVariable UUID productId) {
        return historyService.getHistory(productId);
    }

    @GetMapping("/integrity")
    public Map<String, Object> verifyHistory(
            @PathVariable UUID productId) throws Exception {
        return historyService.verifyHistory(productId);
    }
}
