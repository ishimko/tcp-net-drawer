import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientDrawer extends JPanel {
    private Image image;
    private Graphics imageGraphics;
    private int oldX, oldY;
    private Color backgroundColor = Color.white;

    public ClientDrawer() {
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
                Image tmpImage = image;

                image = newImage(getWidth(), getHeight());
                imageGraphics = image.getGraphics();
                imageGraphics.drawImage(tmpImage, 0, 0, null);

                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            image = newImage(getWidth(), getHeight());
            imageGraphics = image.getGraphics();
        }
        g.drawImage(image, 0, 0, null);


    }

    private Image newImage(int width, int height){
        Image image = createImage(width, height);
        Graphics g = image.getGraphics();
        Color previousColor = g.getColor();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);
        g.setColor(previousColor);
        return image;
    }
}
