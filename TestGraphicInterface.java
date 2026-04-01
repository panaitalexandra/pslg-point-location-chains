import javax.swing.*;
public class TestGraphicInterface {
    public static void main(String[] args) {
        GraphicInterface f = new GraphicInterface();
        f.setSize(800, 550);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
    }
}