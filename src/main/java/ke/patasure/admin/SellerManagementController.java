
package ke.patasure.admin;

import ke.patasure.auth.AppUser;
import ke.patasure.auth.AppUserRepository;
import ke.patasure.auth.SellerCreateRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class SellerManagementController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SellerManagementController(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createSeller(
            @Valid @RequestBody SellerCreateRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists");
        }

        AppUser seller = new AppUser();
        seller.setFullName(request.fullName().trim());
        seller.setEmail(email);
        seller.setPasswordHash(
                passwordEncoder.encode(request.password()));
        seller.setRole("SELLER");
        seller.setEnabled(true);

        AppUser savedSeller = userRepository.save(seller);

        return Map.of(
                "id", savedSeller.getId(),
                "fullName", savedSeller.getFullName(),
                "email", savedSeller.getEmail(),
                "role", savedSeller.getRole()
        );
    }
}
