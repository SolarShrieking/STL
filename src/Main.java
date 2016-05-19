import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.auth.Scopes;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plan:
 * <p>
 * stlFrame   {
 * - User Types Name
 * - User Press Authenticate Button
 * - Button sends User to Auth Page
 * - User Authorizes, Then Presses brand new convert button. }
 * <p>
 * - Program grabs all subs and stores in a string, formatted properly
 * - Copies default twitchList, naming to username inputted
 * - Replaces placeholder in twitchList copy with namelist
 * - Eat Cake
 * <p>
 * <p>
 * get Authentication
 * piece together URL
 * Request
 * Get Return JSON, parse out needed info
 * Format
 * Place in List
 */

public class Main {

    private static boolean debug = true;
    private static stlFrame stlFrame;
    private static String TWITCH_SUBSCRIBERS = "https://api.twitch.tv/kraken/channels/$values"; // Base of the Subscribers/Followers Request URL https://api.twitch.tv/kraken/channels/channel_name/subscriptions
    private static String authToken = null; // Authentication token of the user

    public static void main(String args[]) throws Exception {
        stlFrame = new stlFrame(); //Register stlFrame.java to be used.
    }

    /**
     * @param twitchName The user's Twitch username
     */
    static void processAll(String twitchName) {
        try {
            if (authMe()) {
                String namelist = url(twitchName, 100, 0, 0, "");
                System.out.println("Namelist: " + namelist);
                stlFrame.updateLabel("Getting Twitch Subscribers...");
                readFile(twitchName);
                System.out.println("Namelist final text: " + namelist);
                stringReplace(namelist, twitchName);
                stlFrame.listCreated(twitchName);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.out.println("Error in processAll");
        }
    }

    /**
     * @return True/False Authorized
     */
    private static Boolean authMe() {
        Twitch twitch = new Twitch(); //getting twitch as Twitch from TwitchAPI Wrapper
        twitch.setClientId("5fu22trjshv34ervh1vp1xc28ob011f"); //StellarisTwitchList Client ID
        URI callbackUri = URI.create("http://127.0.0.1:23522/authorize.html"); //Authentication URL
        String authUrl = twitch.auth().getAuthenticationUrl(twitch.getClientId(), callbackUri, Scopes.CHANNEL_SUBSCRIPTIONS); //Gets Authorization Request with permission to view channel subs
        if (debug) {
            System.out.println(authUrl);
        }
        openURLInBrowser(authUrl);
        boolean authSuccess = twitch.auth().awaitAccessToken();

        if (authSuccess) {
            authToken = twitch.auth().getAccessToken();
            System.out.println("Access Token: " + authToken);
            return true;
        } else {
            stlFrame.updateLabel("Authentication Error!");
            System.out.println(twitch.auth().getAuthenticationError());
        }
        return false;
    }

    /**
     * @param url any URL needed to be opened in a browser window.
     */
    static void openURLInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param twitchUsername User's twitch username
     * @param limit          Limit per request (Almost always 100)
     * @param offset         Offset for pagination (+100 per request)
     * @param subTotal       Total number of users/subscribers
     * @param parsedOutput   Parsed username output from the previous request
     * @return Full namelist
     */
    private static String url(String twitchUsername, int limit, int offset, int subTotal, String parsedOutput) {
        String parsedInput;
        try {
            URL url = new URL(insertURLValues(TWITCH_SUBSCRIBERS, twitchUsername, limit, offset));
            System.out.println(url);
            stlFrame.updateLabel("Generating URL, Requesting");

            URLConnection connection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = br.readLine();
            br.close();
            JsonObject jsonObject = Json.parse(inputLine).asObject();

            int total = Integer.parseInt(jsonObject.get("_total").toString());
            stlFrame.updateLabel("Total Subs" + total);

            parsedInput = usernamesFormat(parseList(parseJSON(inputLine)));
            System.out.println("post-parse: " + parsedInput);
            parsedInput = parsedInput.concat(parsedOutput);
            System.out.println("post-concat: " + parsedInput);

            if (offset == 1600) {
                stlFrame.maxNames();
            }
            if (total > offset) {
                url(twitchUsername, limit, offset + 100, total, parsedInput);
            } else if (subTotal < offset) {
                return parsedInput;
            }

        } catch (IOException e) {
            e.printStackTrace();
            stlFrame.popupWindow("Error -- Do you not have any subscribers?" /**+ "\n"+ s*/); // debug stacktrace popup removed.
            return null;
        }
        return parsedInput;
    }

    /**
     * @param url     base URL constant
     * @param channel User's Twitch channel name
     * @param limit   Max names per request, almost always 100
     * @param offset  paging offset for usernames, +100 per request
     * @return Full URL with values inserted
     */
    private static String insertURLValues(String url, String channel, int limit, int offset) {
        // https://api.twitch.tv/kraken/channels/SolarShrieking/subscriptions?limit=100&offset=0&oauth_token=tc7111mgvyxbtk777ucmw726ztb23k&scope=channel_subscriptions
        if (stlFrame.useFollows) {
            return url.replace("$values", channel + "/follows" /**+ "?oauth_token=" + token*/ + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset) + "&oauth_token=" + authToken);
        }
        return url.replace("$values", channel + "/subscriptions" /***/ + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset) + "&oauth_token=" + authToken);
    }

    /**
     * @param input Gets data from json in url()
     * @return ArrayList of usernames
     */
    @SuppressWarnings("deprecation")
    private static ArrayList<String> parseJSON(String input) {
        JsonArray subs;
        if (stlFrame.useFollows) {
            subs = Json.parse(input).asObject().get("follows").asArray();
        } else {
            subs = Json.parse(input).asObject().get("subscriptions").asArray();
        }
        ArrayList<String> subList = new ArrayList<>();
        for (com.eclipsesource.json.JsonValue sub : subs) {
            subList.add(sub.toString() + "\n");
        }
        return subList;
    }

    /**
     * @param list Subscriber/Follower ArrayList
     * @return List of usernames with all the unneeded data filtered
     */
    private static ArrayList<String> parseList(ArrayList<String> list) {
        String pattern = "name\":\"+(\\w+)+\"";
        System.out.println("\nList is " + list.size() + " lines long");
        for (int i = 0; i < list.size(); i++) {
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(list.get(i));
            if (m.find()) {
                String username = m.group(0);
                username = username.replace("name\":", "");
                list.set(i, username);
            } else {
                System.out.println("No Match!");
            }
        }
        return list;
    }

    /**
     * @param list Subscriber/Follower ArrayList
     * @return String of usernames, formatted for the namelist
     */
    private static String usernamesFormat(ArrayList<String> list) {
        String listString = "";
        for (String s : list) {
            listString += s + " ";
        }
        return listString;
    }

    /**
     * @param twitchName =  The user's Twitch username
     * @return Spits out the file.
     * @throws IOException
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static String readFile(String twitchName) throws IOException {

        File cwdFile = new File(twitchName + ".txt");
        String cwd = cwdFile.getAbsolutePath();
        InputStream is = Main.class.getResourceAsStream("twitchList.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        PrintStream out = new PrintStream(cwd);
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
                out.println(line);
            }
            is.close();
            out.close();
            br.close();
            System.out.println(sb.toString());
            return sb.toString();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param namelist   list of user's subscribers/followers
     * @param twitchname The user's Twitch username
     * @throws IOException because IOExceptions happen, y'know?
     */
    private static void stringReplace(String namelist, String twitchname) throws IOException {

        File cwdFile = new File(twitchname + ".txt");
        String cwd = cwdFile.getAbsolutePath();
        System.out.println(cwd);

        for (String fn : new String[]{cwd}) {
            String s = new String(Files.readAllBytes(Paths.get(fn)));
            s = s.replace("subscriberList", namelist);
            try (FileWriter fw = new FileWriter(cwd)) {
                if (s != null) {
                    fw.write(s);
                }
                fw.close();
            }
        }
    }
}

