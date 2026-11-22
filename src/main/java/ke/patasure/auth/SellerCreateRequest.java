
package ke.patasure.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerCreateRequest(

    @NotBlank
    @Size(max = 120)
    String fullName,

    @NotBlank
    @Email
    @Size(max = 180)
    String email,

    @NotBlank
    @Size(min = 12, max = 72)
    String password

) {}
