import drawer_server.ServerDrawer;
import drawer_client.ClientWindow;

import javax.swing.*;
import java.io.IOException;


public class TCPNetDrawer {
    private static final int PORT = 55555;

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error while changing Look and Feel");
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
