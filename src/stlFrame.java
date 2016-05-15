import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private boolean auth = false;

    public stlFrame() {
        createGUI();


        setTitle("Stellaris Twitch Subscriber Name_list Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setLocationRelativeTo(null);
        pack();
    }

    private void createGUI() {

        JPanel panel = new JPanel();
        getContentPane().add(panel);

        panel.setBackground(Color.DARK_GRAY);

        JLabel label = new JLabel("Twitch Username: ");
        panel.add(label);

        //Text field for twitch username input
        fieldUsername = new JTextField();
        fieldUsername.setColumns(20);
        panel.add(fieldUsername);

        //Authentication button. Sends user to twitch auth page
        buttonAuth = new JButton("Authenticate");
        buttonAuth.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String username = fieldUsername.getText();
                        if (username.isEmpty()) {
                            labelMessage.setText("Invalid input!");
                        } else {
                            if (validateInput(username)) {
                                labelMessage.setText("Prepare for Authentication...");
                                System.out.println(username);
                                Main.authMe();
                            } else {
                                labelMessage.setText("Invalid input!");
                            }

                        }

                    }
                }
        );
        panel.add(buttonAuth);

        labelMessage = new JLabel();
        panel.add(labelMessage);
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
}
