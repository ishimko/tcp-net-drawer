package drawer_client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class ClientDrawer extends JPanel {
    private Image image;
    private Graphics imageGraphics;
    private Point localOldPoint = new Point(-1, -1);
    private HashMap<Integer, Point> remotePoints = new HashMap<>();
    private Color backgroundColor = Color.white;
    private ClientHandler clientHandler;

    public ClientDrawer() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                processDot(new Point(e.getX(), e.getY()));
                processEndLine();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
                                   @Override
                                   public void mouseDragged(MouseEvent e) {
                                       processDot(new Point(e.getX(), e.getY()));
                                   }
                               }
        );

        addComponentListener(new ComponentAdapter() {
                                 @Override
                                 public void componentResized(ComponentEvent e) {
                                     Image tmpImage = image;

                                     newImage(getWidth(), getHeight());
                                     imageGraphics.drawImage(tmpImage, 0, 0, null);

                                     repaint();
                                 }
                             }

        );
    }

    private void processDot(Point p) {
        drawDot(p, localOldPoint);

        try {
            clientHandler.sendPoint(p);
        } catch (IOException e){
            System.err.println("Network error: " + e);
        }

    }

    synchronized void drawDot(Point newPoint, Point oldPoint) {
        if (oldPoint.x == -1) {
            oldPoint.x = newPoint.x;
            oldPoint.y = newPoint.y;
        }

        imageGraphics.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y);

        oldPoint.x = newPoint.x;
        oldPoint.y = newPoint.y;

        repaint();
    }

    synchronized void drawRemoteDot(Point p, int clientID){
        if (remotePoints.containsKey(clientID)) {
            drawDot(p, remotePoints.get(clientID));
        } else {
            remotePoints.put(clientID, p);
        }
    }

    synchronized void endRemoteLine(int clientID){
        endLine(remotePoints.get(clientID));
    }

    synchronized void endLine(Point p){
        p.x = -1;
        p.y = -1;
    }

    private void processEndLine(){
        endLine(localOldPoint);
        try {
            clientHandler.endLine();
        } catch (IOException e){
            System.err.println("Network error: " + e);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            newImage(getWidth(), getHeight());
        }
        g.drawImage(image, 0, 0, null);
    }

    private void newImage(int width, int height) {
        image = createImage(width, height);
        imageGraphics = image.getGraphics();
        Color previousColor = imageGraphics.getColor();
        imageGraphics.setColor(backgroundColor);
        imageGraphics.fillRect(0, 0, width, height);
        imageGraphics.setColor(previousColor);
    }

    public void connect(InetAddress ip, int port) throws IOException {
        clientHandler = new ClientHandler(ip, this, port);
        new Thread(clientHandler).start();
    }
}