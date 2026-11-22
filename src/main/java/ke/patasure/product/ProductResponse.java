
package ke.patasure.product;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String brand,
        String model,
        String condition,
        String description,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getModel(),
                product.getCondition(),
                product.getDescription(),
                product.getCreatedAt()
        );
    }
}
