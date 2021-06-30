package Ingwaz;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Objects;


//import jdk.javadoc.internal.doclets.formats.html.SourceToHTMLConverter;

public class IngwazWalletGUI extends JFrame {
    public IngwazWalletGUI() throws FileNotFoundException {
        super();
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        JButton button = new JButton("");

        JLabel label = new JLabel("");
        panel.setBackground(new Color(0x123456));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.add(button);
        panel.add(label);
        // panel.setLayout(new GridLayout(0,1));
        ImageIcon image = new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("Ingwaz.png")));
        frame.setIconImage(image.getImage());

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Ingwaz: The coin among the runes");
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("yay");
        new IngwazWalletGUI();
    }

}
