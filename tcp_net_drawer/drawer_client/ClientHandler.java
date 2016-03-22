package tcp_net_drawer.drawer_client;

import tcp_net_drawer.drawer_protocol.DrawerMessage;
import tcp_net_drawer.drawer_protocol.Point;
import tcp_net_drawer.drawer_protocol.RemotePoint;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private volatile boolean stopped = false;
    private ClientDrawer drawer;


    ClientHandler(InetAddress ip, ClientDrawer drawer, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        clientSocket.setSoTimeout(1000);
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.drawer = drawer;
    }

    public void run() {
        try{
            sendImageDimension(drawer.getDimension());
        } catch (IOException e){
            System.out.println("Error sending dimension: " + e);
        }
        while (!stopped) {
            try {
                processMessage((DrawerMessage)in.readObject());
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                System.err.println("IOError: " + e);
                stop();
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found");
            }
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error while closing socket: " + e);
        }

    }

    synchronized void processMessage(DrawerMessage drawerMessage){
        switch (drawerMessage.messageType){
            case MSG_IMAGE_SIZE:
                Dimension d = (Dimension)drawerMessage.messageBody;
                drawer.resizeImage(d.width, d.height);
                //System.out.println("Dimension msg");
                break;
            case MSG_REMOTE_POINTS_LIST:
                drawer.processDotsList((RemotePoint[])drawerMessage.messageBody);
                //System.out.println("Dots list msg");
                break;
            default:
                System.err.println("unknown messageBody received");
        }
    }

    synchronized void sendPoint(Point p) throws IOException {
        out.writeObject(new DrawerMessage(DrawerMessage.MessageType.MSG_POINT, p));
        out.flush();
    }

    synchronized void sendImageDimension(Dimension d) throws IOException{
        DrawerMessage message = new DrawerMessage(DrawerMessage.MessageType.MSG_IMAGE_SIZE, d);
        out.writeObject(message);
        out.flush();
    }

    void endLine() throws IOException {
        sendPoint(new Point(-1, -1));
    }

    void stop() {
        stopped = true;
    }

}
