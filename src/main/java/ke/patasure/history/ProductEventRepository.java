
package ke.patasure.history;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductEventRepository
        extends JpaRepository<ProductEvent, UUID> {

    List<ProductEvent> findByProductIdOrderByRecordedAtAsc(UUID productId);
}
