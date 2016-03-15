import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientDrawer extends JPanel {
    Image image;
    Graphics imageGraphics;
    int oldX, oldY;

    public ClientDrawer() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX, currentY;
                currentX = e.getX();
                currentY = e.getY();
                imageGraphics.drawLine(oldX, oldY, currentX, currentY);
                repaint();
                oldX = currentX;
                oldY = currentY;
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Image tmpImage = image;

                image = createImage(getWidth(), getHeight());
                imageGraphics = image.getGraphics();
                imageGraphics.drawImage(tmpImage, 0, 0, null);
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        } else {
            image = createImage(getWidth(), getHeight());
            imageGraphics = image.getGraphics();
        }
    }
}
