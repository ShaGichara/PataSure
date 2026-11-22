
package ke.patasure.history;

import ke.patasure.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.Objects;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductHistoryService {

    private final ProductEventRepository eventRepository;
    private final ProductRepository productRepository;
    private final HashingService hashingService;

    public ProductHistoryService(
            ProductEventRepository eventRepository,
            ProductRepository productRepository,
            HashingService hashingService) {
        this.eventRepository = eventRepository;
        this.productRepository = productRepository;
        this.hashingService = hashingService;
    }

    @Transactional
    public ProductEvent recordEvent(
            UUID productId,
            String eventType,
            String details) throws Exception {

        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found");
        }

        List<ProductEvent> history =
                eventRepository.findByProductIdOrderByRecordedAtAsc(productId);

        String previousHash = history.isEmpty()
                ? null
                : history.get(history.size() - 1).getEventHash();

        LocalDateTime recordedAt = LocalDateTime.now()
        .truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        String eventHash = hashingService.calculateHash(
                productId,
                eventType,
                details,
                recordedAt,
                previousHash
        );

        ProductEvent event = new ProductEvent();
        event.setId(UUID.randomUUID());
        event.setProductId(productId);
        event.setEventType(eventType);
        event.setDetails(details);
        event.setRecordedAt(recordedAt);
        event.setPreviousHash(previousHash);
        event.setEventHash(eventHash);

        return eventRepository.save(event);
    }

    public List<ProductEvent> getHistory(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found");
        }

        return eventRepository
                .findByProductIdOrderByRecordedAtAsc(productId);
    }

    public Map<String, Object> verifyHistory(UUID productId) throws Exception {

        List<ProductEvent> history = getHistory(productId);
        String expectedPreviousHash = null;

        for (int i = 0; i < history.size(); i++) {

            ProductEvent event = history.get(i);

            if (!Objects.equals(
                    event.getPreviousHash(), expectedPreviousHash)) {

                return Map.of(
                    "valid", false,
                    "eventsChecked", i + 1,
                    "message", "Previous hash mismatch at event " + (i + 1)
                );
            }

            String calculatedHash = hashingService.calculateHash(
                    event.getProductId(),
                    event.getEventType(),
                    event.getDetails(),
                    event.getRecordedAt(),
                    event.getPreviousHash()
            );

            if (!calculatedHash.equals(event.getEventHash())) {

                return Map.of(
                    "valid", false,
                    "eventsChecked", i + 1,
                    "message", "Event hash mismatch at event " + (i + 1)
                );
            }

            expectedPreviousHash = event.getEventHash();
        }

        return Map.of(
            "valid", true,
            "eventsChecked", history.size(),
            "message", "Product history integrity verified"
        );
    }

}
