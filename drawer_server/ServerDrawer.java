package drawer_server;

import sun.net.ConnectionResetException;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ServerDrawer {
    private ServerSocket serverSocket;
    private volatile HashMap<Integer, ObjectOutputStream> clients = new HashMap<>();

    public ServerDrawer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private int getNewKey() {
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
        writeLog("client removed");
    }

    private synchronized int addClient(ObjectOutputStream client) {
        int cliendID = getNewKey();
        clients.put(cliendID, client);
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
                System.err.println("Error starting connection handler" + e);

            }
        }
    }

    public synchronized void processClients(int[] dataToSend, int initiatorID) {
        ObjectOutputStream client;

        try {
            for (Integer clientID: clients.keySet()) {
                if (clientID != initiatorID) {
                    int[] buf = {initiatorID, dataToSend[0], dataToSend[1]};
                    client = clients.get(clientID);
                    client.writeObject(buf);
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

    class ConnectionHandler extends Thread {
        private Socket connection;
        private int clientID;

        ConnectionHandler(Socket connection, int clientID) throws IOException {
            this.connection = connection;
            this.clientID = clientID;
            writeLog(connection, "connected");
        }

        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    int[] readData = (int[]) in.readObject();
                    processClients(readData, clientID);
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


