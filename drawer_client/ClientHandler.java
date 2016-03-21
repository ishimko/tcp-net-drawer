package drawer_client;

import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private volatile boolean stopped = false;
    private ClientDrawer drawer;


    ClientHandler(InetAddress ip, ClientDrawer drawer, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.drawer = drawer;
    }

    public void run() {
        while (!stopped) {
            try {
                int[] readData = (int[]) in.readObject();
                int clientID = readData[0];
                Point readPoint = new Point(readData[1], readData[2]);

                if (readPoint.x == -1) {
                    drawer.endRemoteLine(clientID);
                } else {
                    drawer.drawRemoteDot(readPoint, clientID);
                }
            } catch (IOException e) {
                System.err.println("IOError: " + e);
                stop();
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found");
            }
        }

        try

        {
            clientSocket.close();
            System.out.println("Socket closed");
        } catch (IOException e) {
            System.out.println("Error while closing socket: " + e);
        }

    }

    void sendPoint(Point p) throws IOException {
        int[] dataToSend = new int[2];
        dataToSend[0] = p.x;
        dataToSend[1] = p.y;
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
