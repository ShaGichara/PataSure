
package ke.patasure.history;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductHistoryRequest(

    @NotBlank(message = "Event type is required")
    @Pattern(
        regexp = "REPAIR|DAMAGE_DISCLOSURE|OWNERSHIP_TRANSFER",
        message = "Invalid event type"
    )
    String eventType,

    @NotBlank(message = "Event details are required")
    @Size(max = 1000, message = "Details cannot exceed 1000 characters")
    String details

) {}
