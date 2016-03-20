package drawer_server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerDrawer {
    private ServerSocket serverSocket;
    private List<ObjectOutputStream> clients = new ArrayList<>();

    public ServerDrawer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start(){
        System.out.println("Server started");
        while (true) {
            try {
                Socket connection = serverSocket.accept();
                clients.add(new ObjectOutputStream(connection.getOutputStream()));
                new ConnectionHandler(connection, clients.size() - 1).start();
            } catch (IOException e) {
                System.err.println("Error starting connection handler" + e);

            }
        }
    }

    public synchronized void processClients(int[] dataToSend, int initiatorNumber) {
        ObjectOutputStream client;

        System.out.println("Sending point");
        try {
            for (int i = 0; i < clients.size(); i++){
                if (i != initiatorNumber) {
                    int[] buf = {initiatorNumber, dataToSend[0], dataToSend[1]};
                    client = clients.get(i);
                    client.writeObject(buf);
                    client.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error while sending:" + e);
        }
    }

    class ConnectionHandler extends Thread {
        private Socket connection;
        private int clientID;

        ConnectionHandler(Socket connection, int clientID) throws IOException {
            this.connection = connection;
            this.clientID = clientID;
            System.out.println(connection.getInetAddress().toString() + " connected");
        }

        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    int[] readData = (int[]) in.readObject();
                    processClients(readData, clientID);
                }
            } catch (IOException e) {
                System.err.println("Error getting inputStream: " + e);
            } catch (ClassNotFoundException e) {
                System.err.println("Unknown class: " + e);
            }
        }
    }

}


