package tcp_net_drawer.drawer_client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

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
    private Dimension imageDimension = new Dimension(0, 0);

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
                                     resizeImage(getSize());
                                 }
                             }

        );
    }

    private synchronized void resizeImage(Dimension newDimension){
        boolean redraw = false;
        if (newDimension.height > imageDimension.height) {
            imageDimension.height = newDimension.height + 2;
            redraw = true;
        }
        if (newDimension.width > imageDimension.width) {
            imageDimension.width = newDimension.width + 2;
            redraw = true;
        }

        if (redraw){
            Image tmpImage = image;
            newImage();
            imageGraphics.drawImage(tmpImage, 0, 0, null);
        }
        repaint();
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

        resizeImage(new Dimension(newPoint.x, newPoint.y));

        imageGraphics.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y);

        oldPoint.x = newPoint.x;
        oldPoint.y = newPoint.y;

        repaint();
    }

    private synchronized void drawRemoteDot(RemotePoint p) {
        if (remotePoints.containsKey(p.clientID)) {
            drawDot(p.point, remotePoints.get(p.clientID));
        } else {
            remotePoints.put(p.clientID, p.point);
        }
    }

    private synchronized void processRemoteDot(RemotePoint p){
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

    private synchronized void endRemoteLine(int clientID) {
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
            newImage();
        }
        g.drawImage(image, 0, 0, null);
    }

    private void newImage() {
        image = createImage(imageDimension.width, imageDimension.height);
        imageGraphics = image.getGraphics();
        clearImage();
    }

    private void clearImage() {
        Color previousColor = imageGraphics.getColor();
        imageGraphics.setColor(backgroundColor);
        imageGraphics.fillRect(0, 0, imageDimension.width, imageDimension.height);
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
        clearImage();
    }

    synchronized void localClear(){
        remotePoints.clear();
        localOldPoint = new Point(-1, -1);

        clearImage();
    }
    synchronized void clear(){
        try {
            clientHandler.sendClearMessage();
        } catch (IOException e){
            System.out.println("Network error while sending localClear message: " + e);
        }
        localClear();
    }
}