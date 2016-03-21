package drawer_client;

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
        while (!stopped) {
            try {
                int[] readData;
                readData = (int[]) in.readObject();
                int clientID = readData[0];
                Point readPoint = new Point(readData[1], readData[2]);

                if (readPoint.x == -1) {
                    drawer.endRemoteLine(clientID);
                } else {
                    drawer.drawRemoteDot(readPoint, clientID);
                }
            } catch (SocketTimeoutException e) {
                System.out.println(stopped);
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

    void sendPoint(Point p) throws IOException {
        int[] dataToSend = {p.x, p.y};
        out.writeObject(dataToSend);
        out.flush();
    }

    void endLine() throws IOException {
        sendPoint(new Point(-1, -1));
    }

    public void stop() {
        stopped = true;
    }

}
