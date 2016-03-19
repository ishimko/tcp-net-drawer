import javax.swing.*;
import java.awt.*;


public class TCPNetDrawer {
    private static void setSystemLookAndFeel(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){
            System.err.println("Error while changing Look and Feel");
        }
    }

    private static class MainWindow extends JFrame{
        public MainWindow(){
            setTitle("Paint.NET");
            setLayout(new BorderLayout());

            ClientDrawer clientDrawer = new ClientDrawer();
            add(clientDrawer, BorderLayout.CENTER);

            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

            controlPanel.add(Box.createVerticalGlue());

            Container containerIP = Box.createHorizontalBox();

            JLabel lblIP = new JLabel("IP:");
            containerIP.add(lblIP);

            JTextField edtIP = new JTextField();
            edtIP.setMaximumSize(new Dimension(Integer.MAX_VALUE, edtIP.getPreferredSize().height));
            containerIP.add(edtIP);

            controlPanel.add(containerIP);

            JButton btnConnect = new JButton("Подключиться");
            btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnConnect.getPreferredSize().height));
            controlPanel.add(btnConnect);

            add(controlPanel, BorderLayout.WEST);

            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setSize(new Dimension(300, 200));
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        setSystemLookAndFeel();
        new MainWindow();
    }
}
