
package ke.patasure.qr;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    private static final Color TEAL = new Color(8, 127, 120);
    private static final Color WHITE = Color.WHITE;

    public byte[] generateQrCode(String content) throws Exception {

        QRCode qrCode = Encoder.encode(
            content,
            ErrorCorrectionLevel.H
        );

        ByteMatrix matrix = qrCode.getMatrix();

        int modules = matrix.getWidth();
        int imageSize = 480;
        int quietZone = 4;

        double cell = (double) imageSize /
            (modules + quietZone * 2);

        double offset = quietZone * cell;

        BufferedImage image = new BufferedImage(
            imageSize,
            imageSize,
            BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = image.createGraphics();

        graphics.setColor(WHITE);
        graphics.fillRect(0, 0, imageSize, imageSize);

        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Draw the QR modules with softly rounded corners.
        graphics.setColor(TEAL);

        for (int y = 0; y < modules; y++) {
            for (int x = 0; x < modules; x++) {

                if (matrix.get(x, y) == 1 &&
                    !isFinderArea(x, y, modules)) {

                    double px = offset + x * cell;
                    double py = offset + y * cell;

                    graphics.fill(new RoundRectangle2D.Double(
                        px + cell * 0.10,
                        py + cell * 0.10,
                        cell * 0.80,
                        cell * 0.80,
                        cell * 0.28,
                        cell * 0.28
                    ));
                }
            }
        }

        // Draw three custom finder patterns.
        drawFinder(graphics, offset, cell, 0, 0);
        drawFinder(graphics, offset, cell, modules - 7, 0);
        drawFinder(graphics, offset, cell, 0, modules - 7);

        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ImageIO.write(image, "PNG", output);

        return output.toByteArray();
    }

    private boolean isFinderArea(int x, int y, int modules) {

        return
            (x < 7 && y < 7) ||
            (x >= modules - 7 && y < 7) ||
            (x < 7 && y >= modules - 7);
    }

    private void drawFinder(
        Graphics2D graphics,
        double offset,
        double cell,
        int gridX,
        int gridY
    ) {

        double x = offset + gridX * cell;
        double y = offset + gridY * cell;

        graphics.setColor(TEAL);
        graphics.fill(new RoundRectangle2D.Double(
            x, y, cell * 7, cell * 7, cell * 1.4, cell * 1.4
        ));

        graphics.setColor(WHITE);
        graphics.fill(new RoundRectangle2D.Double(
            x + cell, y + cell,
            cell * 5, cell * 5,
            cell * 0.8, cell * 0.8
        ));

        graphics.setColor(TEAL);
        graphics.fill(new RoundRectangle2D.Double(
            x + cell * 2, y + cell * 2,
            cell * 3, cell * 3,
            cell * 0.6, cell * 0.6
        ));
    }
}
