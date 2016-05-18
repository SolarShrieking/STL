import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GUI Created by SolarShrieking on 5/15/2016.
 * Function of Class: Creating & Handling the UI for STL.
 */

class stlFrame extends JFrame {

    private JButton buttonAuth;
    private JTextField fieldUsername;
    private JLabel labelMessage;
    private JLabel logoImage;
    private JButton donateButton;
    private JCheckBox useFollowers;
    public boolean useFollows = false;
    private boolean auth = false;
    private final Class<?> referenceClass = Main.class;
    final URL url = referenceClass.getProtectionDomain().getCodeSource().getLocation();
    private BufferedImage imageLogo = ImageIO.read(getClass().getResourceAsStream("resources/logoSTL.png"));
    private BufferedImage imageDonate = ImageIO.read(getClass().getResourceAsStream("resources/donateButton.png"));
    private BufferedImage iconGit = ImageIO.read(getClass().getResourceAsStream("resources/iconGit.png"));

    /**
     *
     * @throws Exception In case Exceptions happen, y'know?
     */
     stlFrame() throws Exception {
        createGUI();
        setTitle("Stellaris Twitch Subscriber Namelist Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setSize(525, 300);
        setLocationRelativeTo(null);
//        pack();


    }

    /**
     *  Creates the UI
     */
    private void createGUI() {

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new BorderLayout() );
        panel.setBackground(Color.LIGHT_GRAY);
        JLabel logoImage = new JLabel();
                try{
                    logoImage.setIcon(new ImageIcon(imageLogo));
        } catch (Exception e) {
            e.printStackTrace();
        }

        fieldUsername = new JTextField();
        fieldUsername.setColumns(20);
        useFollowers = new JCheckBox("Use Followers instead?");
        useFollowers.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    useFollows = true;
                } else{
                    useFollows = false;
                }
            }
        });

        buttonAuth = new JButton("Authenticate");
        buttonAuth.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String username = fieldUsername.getText();
                            if (validateInput(username)) {
                                labelMessage.setText("Authenticating...");
                                System.out.println(username);
                                    if (useFollows) {
                                        System.out.println("using followers instead");
                                    }
                                Main.processAll(username);
                            } else {
                                labelMessage.setText("Invalid input!");
                            }
                        }
                    });

        labelMessage = new JLabel("");
        labelMessage.setPreferredSize(new Dimension(200, 15));
        labelMessage.setVisible(true);
        JLabel gitImage = new JLabel();
        gitImage.createToolTip();
        gitImage.setToolTipText("Check out the GitHub repo for this project here!");
        gitImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Main.openURLInBrowser("https://github.com/SolarShrieking/STL");
                }
        });

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
                Main.openURLInBrowser("https://paypal.me/SolarShrieking/5");
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
        bottomPanel.add(useFollowers);
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.PAGE_END);
    }

    /**
     *
     * @param text   Text to put in the label
     */
    public void updateLabel(String text) {
        labelMessage.setText(text);
    }

    /**
     *
     * @param username      Text entered in the username text field
     * @return              True/False Valid username input
     */
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

    /**
     *
     * @param username      User's Twitch Name
     */
    public void listCreated(String username) {
        JOptionPane.showMessageDialog(null, "List Saved to " + username + ".txt");
    }

    /**
     *  Popup message for people with 1600+ Subs/Followers due to API limits
     */
    public void maxNames() {
        JOptionPane.showMessageDialog(null, "Sorry, TwitchAPI limits to 1600 requests.\nThe names will still be transcribed to your namelist.");
    }

    /**
     *
     * @param message       Message to display in a pop up message box
     */
    public void popupWindow(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
