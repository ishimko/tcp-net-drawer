package tcp_net_drawer.drawer_server;

import tcp_net_drawer.drawer_protocol.DrawerMessage;
import tcp_net_drawer.drawer_protocol.Point;
import tcp_net_drawer.drawer_protocol.RemotePoint;

import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ServerDrawer {
    private ServerSocket serverSocket;
    private volatile HashMap<Integer, ObjectOutputStream> clients = new HashMap<>();
    private volatile List<RemotePoint> drawLog = new ArrayList<>();
    private Dimension imageSize = new Dimension(0, 0);

    public ServerDrawer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private synchronized void addPointToLog(Point p, int clientID) {
        drawLog.add(new RemotePoint(p, clientID));
    }

    private synchronized int getNewKey() {
        int i;
        for (i = 0; i < clients.size(); i++) {
            if (!clients.containsKey(i)) {
                return i;
            }

        }
        return i;
    }

    private synchronized void removeClient(int clientID) {
        clients.remove(clientID);
    }

    private synchronized int addClient(ObjectOutputStream client) throws IOException {
        int cliendID = getNewKey();
        clients.put(cliendID, client);
        DrawerMessage message;

        if (clients.size() > 1) {
            message = new DrawerMessage(DrawerMessage.MessageType.MSG_IMAGE_SIZE, imageSize);
            client.writeObject(message);
            client.flush();
        }

        RemotePoint[] points = new RemotePoint[drawLog.size()];
        drawLog.toArray(points);
        message = new DrawerMessage(DrawerMessage.MessageType.MSG_REMOTE_POINTS_LIST, points);
        client.writeObject(message);
        client.flush();

        return cliendID;
    }

    public void start() {
        writeLog("server started");

        while (true) {
            try {
                Socket connection = serverSocket.accept();
                int newClientID = addClient(new ObjectOutputStream(connection.getOutputStream()));
                new ConnectionHandler(connection, newClientID).start();
            } catch (IOException e) {
                System.err.println("Error while adding client" + e);

            }
        }
    }

    private synchronized void sendDot(Point point, int initiatorID) {
        DrawerMessage message = new DrawerMessage(DrawerMessage.MessageType.MSG_REMOTE_POINTS_LIST, new RemotePoint(point, initiatorID));
        sendMessage(message, initiatorID);
    }

    private synchronized void sendMessage(DrawerMessage message, int initiatorID) {
        ObjectOutputStream client;
        try {
            for (Integer clientID : clients.keySet()) {
                if (clientID != initiatorID) {
                    client = clients.get(clientID);
                    client.writeObject(message);
                    client.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error while sending:" + e);
        }
    }

    private synchronized void sendDimension(int initiatorID) {
        DrawerMessage message = new DrawerMessage(DrawerMessage.MessageType.MSG_IMAGE_SIZE, imageSize);
        sendMessage(message, initiatorID);
    }

    private static String reprClient(Socket connection) {
        return connection.getInetAddress().getHostAddress() + ":" + connection.getPort();
    }

    private static String getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(c.getTime());
    }

    private static void writeLog(String msg) {
        System.out.println(getTime() + ": " + msg);
    }

    private static void writeLog(Socket connection, String msg) {
        System.out.println(getTime() + ": " + reprClient(connection) + ": " + msg);
    }

    private class ConnectionHandler extends Thread {
        private Socket connection;
        private int clientID;

        ConnectionHandler(Socket connection, int clientID) throws IOException {
            this.connection = connection;
            this.clientID = clientID;
            writeLog(connection, "connected");
        }

        private void processMessage(DrawerMessage message) {
            switch (message.messageType) {
                case MSG_IMAGE_SIZE:
                    Dimension d = (Dimension) message.messageBody;

                    boolean newDimension = false;

                    if (d.height > imageSize.height){
                        imageSize.height = d.height;
                        newDimension = true;
                    }

                    if (d.getWidth() > imageSize.width){
                        imageSize.width = d.width;
                        newDimension = true;
                    }

                    if (newDimension){
                        sendDimension(clientID);
                    }


                    break;
                case MSG_POINT:
                    Point point = (Point) message.messageBody;
                    addPointToLog(point, clientID);
                    sendDot(point, clientID);
                    break;
                default:
                    writeLog("unknown messageBody received");
            }
        }

        @Override
        public void run() {
            try {
//                synchronized (this) {
//                    if (clients.size() > 1) {
//                        clients.get(clientID).writeObject(new DrawerMessage(DrawerMessage.MessageType.MSG_IMAGE_SIZE, imageSize));
//                        System.out.println("dimension send");
//                    }
//                }
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    processMessage((DrawerMessage) in.readObject());
                }
            } catch (EOFException e) {
                removeClient(clientID);
                writeLog(connection, "disconnected");

            } catch (SocketException e) {
                removeClient(clientID);
                writeLog(connection, "connection lost");

            } catch (IOException e) {
                System.err.println("Error getting inputStream: " + e);
            } catch (ClassNotFoundException e) {
                System.err.println("Unknown class: " + e);
            }
        }
    }

}


