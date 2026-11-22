
package ke.patasure.history;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class HashingService {

    public String calculateHash(
            UUID productId,
            String eventType,
            String details,
            LocalDateTime recordedAt,
            String previousHash) throws Exception {

        String data = productId + "|"
                + eventType + "|"
                + details + "|"
                + recordedAt + "|"
                + (previousHash == null ? "" : previousHash);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = digest.digest(
                data.getBytes(StandardCharsets.UTF_8)
        );

        return HexFormat.of().formatHex(hashBytes);
    }
}
