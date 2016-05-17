import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GUI Created by SolarShrieking on 5/15/2016.
 * Function of Class: ${doingThings}
 */
public class stlFrame extends JFrame {

    private JButton buttonAuth;
    private JTextField fieldUsername;
    private JLabel labelMessage;
    private JLabel logoImage;
    private JButton donateButton;

    private boolean auth = false;
    private final Class<?> referenceClass = Main.class;
    final URL url = referenceClass.getProtectionDomain().getCodeSource().getLocation();

    BufferedImage imageLogo = ImageIO.read(getClass().getResourceAsStream("resources/logoSTL.png"));

    BufferedImage imageDonate = ImageIO.read(getClass().getResourceAsStream("resources/donateButton.png"));

    BufferedImage iconGit = ImageIO.read(getClass().getResourceAsStream("resources/iconGit.png"));

    public stlFrame() throws Exception {
        createGUI();
        setTitle("Stellaris Twitch Subscriber Namelist Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setSize(525, 300);
        setLocationRelativeTo(null);
//        pack();


    }


    private String getFilePath() {

//        try {
//            final File jarPath = new File(url.toURI()).getParentFile();
//            String path = jarPath.toString();
//            System.out.println(path);
//            return path;
//        } catch (final URISyntaxException e) {
//            e.printStackTrace();
//        }
//        return null;
        return null;
    }

    private void createGUI() {

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new BorderLayout() );


        panel.setBackground(Color.LIGHT_GRAY);

//        ImageIcon logo = new ImageIcon("logoSTL.png");
//        JMenuItem logoMenuItem = new JMenuItem(logo);
//        panel.add(logoMenuItem);


        JLabel logoImage = new JLabel();

                try{
                    logoImage.setIcon(new ImageIcon(imageLogo));
        } catch (Exception e) {
            e.printStackTrace();
        }





        //Text field for twitch username input
        fieldUsername = new JTextField();
        fieldUsername.setColumns(20);


        //Authentication button. Sends user to twitch auth page
        buttonAuth = new JButton("Authenticate");
        buttonAuth.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String username = fieldUsername.getText();
                            if (validateInput(username)) {
                                labelMessage.setText("Authenticating...");
                                System.out.println(username);

                                try{
                                    String namelist = Main.authMe(username);
                                    Main.processAll(username, namelist);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                labelMessage.setText("Invalid input!");
                            }
                        }
                    }
        );



        labelMessage = new JLabel("");
        labelMessage.setPreferredSize(new Dimension(100, 15));
        labelMessage.setVisible(true);

        JLabel gitImage = new JLabel();
        gitImage.createToolTip();
        gitImage.setToolTipText("Check out the GitHub repo for this project here!");
        gitImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();

                    try {
                        desktop.browse(new URI("https://github.com/SolarShrieking/STL"));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec("xdg-open" + "https://github.com/SolarShrieking/STL");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }}}});

        try{
            gitImage.setIcon(new ImageIcon(iconGit));
        } catch (Exception e) {
            e.printStackTrace();
        }



        JLabel donateImage = new JLabel();
        donateImage.createToolTip();
        donateImage.setToolTipText("If you found this tool useful and would like to throw a RedBull my way, feel free! :)");
        donateImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
}});

        try{
            donateImage.setIcon(new ImageIcon(imageDonate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        donateImage.setLocation(panel.getWidth()-donateImage.getX(), panel.getHeight()-donateImage.getY());

        JLabel label = new JLabel("Twitch Username: ");

        JPanel topPanel = new JPanel(); getContentPane().add(topPanel);
        JPanel centerPanel = new JPanel(); getContentPane().add(centerPanel);
        JPanel bottomPanel = new JPanel(); getContentPane().add(bottomPanel);

        topPanel.add(logoImage, BorderLayout.NORTH);

        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(label);
        centerPanel.add(fieldUsername);
        centerPanel.add(buttonAuth);



        centerPanel.add(labelMessage);

        bottomPanel.add(donateImage);
        bottomPanel.add(gitImage, FlowLayout.LEFT);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.PAGE_END);
    }

    public void updateLabel(String text) {
        labelMessage.setText(text);
    }

    public boolean validateInput(String username) {
        //Uses regex to check if the username is valid.
        String pattern = "^[a-zA-Z0-9_]{4,25}$";
        for (int i = 0; i < username.length(); i++) {
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(username);
            if (!m.matches()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void listCreated(String username) {
        JOptionPane.showMessageDialog(null, "List Saved to " + username + ".txt");
    }

    public void maxNames() {
        JOptionPane.showMessageDialog(null, "Sorry, TwitchAPI limits to 1600 requests.\nThe names will still be transcribed to your namelist.");
    }
}
