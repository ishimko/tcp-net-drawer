package tcp_net_drawer.drawer_server;

import tcp_net_drawer.drawer_protocol.DrawerMessage;
import tcp_net_drawer.drawer_protocol.RemotePoint;

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

    public ServerDrawer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private synchronized void addPointToLog(RemotePoint p){
        drawLog.add(p);
    }

    private synchronized int getNewKey() {
        int i;
        for (i = 0; i < clients.size(); i++){
            if (!clients.containsKey(i)){
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
        RemotePoint[] points = new RemotePoint[drawLog.size()];
        drawLog.toArray(points);
        DrawerMessage message = new DrawerMessage(DrawerMessage.MessageType.MSG_REMOTE_POINTS_LIST, points);
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

    private synchronized void processClients(RemotePoint point) {
        ObjectOutputStream client;
        int initiatorID = point.clientID;
        DrawerMessage message = new DrawerMessage(DrawerMessage.MessageType.MSG_REMOTE_POINTS_LIST, point);
        try {
            for (Integer clientID: clients.keySet()) {
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

    private static String reprClient(Socket connection) {
        return connection.getInetAddress().getHostAddress() + ":" + connection.getPort();
    }

    private static String getTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(c.getTime());
    }

    private static void writeLog(String msg){
        System.out.println(getTime() + ": " + msg);
    }

    private static void writeLog(Socket connection, String msg){
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

        private void processMessage(DrawerMessage message){
            switch (message.messageType){
                case MSG_IMAGE_SIZE:
                    break;
                case MSG_POINT:
                    RemotePoint point = (RemotePoint)message.messageBody;
                    addPointToLog(point);
                    processClients(point);
                    break;
                default:
                    writeLog("unknown messageBody received");
            }
        }

        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    processMessage((DrawerMessage)in.readObject());
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


