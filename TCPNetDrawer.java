import drawer_client.ClientDrawer;
import drawer_server.ServerDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;


public class TCPNetDrawer {
    private static final int PORT = 55555;

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error while changing Look and Feel");
        }
    }

    private static class ClientWindow extends JFrame {
        static final int MIN_FRAME_WIDTH = 400;
        static final int MIN_FRAME_HEIGHT = 300;
        static final int EDT_IP_WIDTH = 100;

        ClientWindow(int port) {
            setTitle("Paint.NET");
            setLayout(new BorderLayout());

            ClientDrawer clientDrawer = new ClientDrawer();
            clientDrawer.setEnabled(false);
            JLabel lblIP = new JLabel("IP:");
            JTextField edtIP = new JTextField();
            int edtIPHeight = edtIP.getPreferredSize().height;
            JButton btnConnect = new JButton("Подключиться");
            btnConnect.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    try {
                        clientDrawer.connect(InetAddress.getByName(edtIP.getText()), port);
                        clientDrawer.setEnabled(false);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Ошибка подключения!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(lblIP)
                                    .addComponent(edtIP, EDT_IP_WIDTH, EDT_IP_WIDTH, EDT_IP_WIDTH)
                            )
                            .addComponent(btnConnect)
                    )
                    .addComponent(clientDrawer));

            layout.setVerticalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup()
                                    .addComponent(lblIP)
                                    .addComponent(edtIP, edtIPHeight, edtIPHeight, edtIPHeight)
                            )
                            .addComponent(btnConnect)
                    )
                    .addComponent(clientDrawer));


            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(MIN_FRAME_WIDTH, MIN_FRAME_HEIGHT));
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong number of arguments arguments!");
            System.exit(1);
        }

        String mode = args[0];
        switch (mode) {
            case "-c":
                setSystemLookAndFeel();
                new ClientWindow(PORT);
                break;
            case "-s":
                try {
                    new ServerDrawer(PORT).start();
                } catch (IOException e) {
                    System.err.println("Network error: " + e);
                }
                break;
            default:
                System.err.println("Unknown argument!");
                System.exit(1);

        }


    }
}
