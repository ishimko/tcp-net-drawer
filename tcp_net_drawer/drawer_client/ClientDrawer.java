package tcp_net_drawer.drawer_client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.Remote;
import java.util.*;

import tcp_net_drawer.drawer_protocol.DrawerMessage;
import tcp_net_drawer.drawer_protocol.Point;
import tcp_net_drawer.drawer_protocol.RemotePoint;

class ClientDrawer extends JPanel {
    private Image image;
    private Graphics imageGraphics;
    private Point localOldPoint = new Point(-1, -1);
    private HashMap<Integer, Point> remotePoints = new HashMap<>();
    private Color backgroundColor = Color.white;
    private ClientHandler clientHandler;
    private boolean activated = false;
    private int maxHeight;
    private int maxWidth;

    ClientDrawer() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (activated) {
                    processDot(new Point(e.getX(), e.getY()));
                    processEndLine();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
                                   @Override
                                   public void mouseDragged(MouseEvent e) {
                                       if (activated) {
                                           processDot(new Point(e.getX(), e.getY()));
                                       }
                                   }
                               }
        );

        addComponentListener(new ComponentAdapter() {
                                 @Override
                                 public void componentResized(ComponentEvent event) {
                                     resizeImage(getWidth(), getHeight());
                                     if (clientHandler != null){
                                         try {
                                             clientHandler.sendImageDimension(getWidth(), getHeight());
                                         } catch (IOException e){
                                             System.err.println("Network error: " + e);

                                         }
                                     }
                                 }
                             }

        );
    }

    synchronized void resizeImage(int width, int height){
        Image tmpImage = image;

        if (height > maxHeight || width > maxWidth) {
            maxHeight = height;
            maxWidth = width;

            newImage(width, height);
            imageGraphics.drawImage(tmpImage, 0, 0, null);
            repaint();
        }
    }

    private void processDot(Point p) {
        drawDot(p, localOldPoint);

        try {
            clientHandler.sendPoint(p);
        } catch (IOException e) {
            System.err.println("Network error: " + e);
        }

    }

    private synchronized void drawDot(Point newPoint, Point oldPoint) {
        if (oldPoint.x == -1) {
            oldPoint.x = newPoint.x;
            oldPoint.y = newPoint.y;
        }

        imageGraphics.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y);

        oldPoint.x = newPoint.x;
        oldPoint.y = newPoint.y;

        repaint();
    }

    synchronized void drawRemoteDot(RemotePoint p) {
        if (remotePoints.containsKey(p.clientID)) {
            drawDot(p.point, remotePoints.get(p.clientID));
        } else {
            remotePoints.put(p.clientID, p.point);
        }
    }

    synchronized void processRemoteDot(RemotePoint p){
        if (p.point.x == -1 || p.point.y == -1){
            endRemoteLine(p.clientID);
        } else {
            drawRemoteDot(p);
        }
    }

    synchronized void processDotsList(RemotePoint[] pointsList){
        for (RemotePoint p: pointsList){
            processRemoteDot(p);
        }

    }

    synchronized void endRemoteLine(int clientID) {
        endLine(remotePoints.get(clientID));
    }

    private synchronized void endLine(Point p) {
        p.x = -1;
        p.y = -1;
    }

    private void processEndLine() {
        endLine(localOldPoint);
        try {
            clientHandler.endLine();
        } catch (IOException e) {
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
        clear();
    }

    private void clear() {
        Color previousColor = imageGraphics.getColor();
        imageGraphics.setColor(backgroundColor);
        imageGraphics.fillRect(0, 0, getWidth(), getHeight());
        imageGraphics.setColor(previousColor);

        repaint();
    }

    void connect(InetAddress ip, int port) throws IOException {
        clientHandler = new ClientHandler(ip, this, port);
        new Thread(clientHandler).start();
    }

    void disconnect() throws IOException {
        clientHandler.stop();
    }

    void setActivated(boolean activated) {
        this.activated = activated;
        clear();
    }
}