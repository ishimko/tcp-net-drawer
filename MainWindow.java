import javax.swing.*;
import java.awt.*;


public class MainWindow {
    public static void main(String[] args){
        JFrame mainFrame = new JFrame("Paint.NET");
        ClientDrawer clientDrawer = new ClientDrawer();
        mainFrame.add(clientDrawer);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(new Dimension(300, 200));
        mainFrame.setVisible(true);
    }
}
