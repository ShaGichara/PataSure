
package ke.patasure.qr;

import ke.patasure.product.ProductRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class QrCodeController {

    private final ProductRepository productRepository;
    private final QrCodeService qrCodeService;

    @Value("${patasure.public-base-url:https://verify.patasure.freeddns.org}")
    private String publicBaseUrl;

    public QrCodeController(
            ProductRepository productRepository,
            QrCodeService qrCodeService) {
        this.productRepository = productRepository;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getProductQrCode(@PathVariable UUID id) {

        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found"
            );
        }

        try {
            String verificationUrl =
                publicBaseUrl + "/verify.html?id=" + id;

            byte[] qrImage =
                qrCodeService.generateQrCode(verificationUrl);

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);

        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to generate QR code"
            );
        }
    }
}
