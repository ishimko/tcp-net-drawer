package tcp_net_drawer.drawer_client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;


public class ClientWindow extends JFrame {
    private enum Mode{
        M_CONNECTED,
        M_DISCONNECTED
    }

    static final int MIN_FRAME_WIDTH = 400;
    static final int MIN_FRAME_HEIGHT = 300;
    static final int EDT_IP_WIDTH = 100;
    Mode currentMode = Mode.M_DISCONNECTED;

    public ClientWindow(int port) {
        setTitle("Paint.NET");
        setLayout(new BorderLayout());

        ClientDrawer clientDrawer = new ClientDrawer();
        JLabel lblIP = new JLabel("IP:");
        JTextField edtIP = new JTextField();
        int edtIPHeight = edtIP.getPreferredSize().height;

        JButton btnConnect = new JButton("Подключиться");
        btnConnect.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                switch (currentMode){
                    case M_DISCONNECTED:
                        try {
                            clientDrawer.connect(InetAddress.getByName(edtIP.getText()), port);
                            clientDrawer.setActivated(true);
                            btnConnect.setText("Отключиться");

                            currentMode = Mode.M_CONNECTED;
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Ошибка подключения!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                        break;

                    case M_CONNECTED:
                        try {
                            clientDrawer.disconnect();
                            clientDrawer.setActivated(false);
                            btnConnect.setText("Подключиться");

                            currentMode = Mode.M_DISCONNECTED;
                        } catch (IOException e){
                            JOptionPane.showMessageDialog(null, "Невозможно закрыть соединение!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
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
