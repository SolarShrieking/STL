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
 *      Plan:
 *
 *      stlFrame   {
 *      - User Types Name
 *      - User Press Authenticate Button
 *      - Button sends User to Auth Page
 *      - User Authorizes, Then Presses brand new convert button. }
 *
 *      - Program grabs all subs and stores in a string, formatted properly
 *      - Copies default twitchList, naming to username inputted
 *      - Replaces placeholder in twitchList copy with namelist
 *      - Eat Cake
 *
 *
 *      get Authentication
 *      piece together URL
 *      Request
 *      Get Return JSON, parse out needed info
 *      Format
 *      Place in List
 *
 *
 */
public class Main {

    private static boolean debug = true;
    private static stlFrame stlFrame;
    private static String TWITCH_SUBSCRIBERS = "https://api.twitch.tv/kraken/channels/$values"; // Base of the Subscribers Request URL https://api.twitch.tv/kraken/channels/channel_name/subscriptions
    private static String TWITCH_FOLLOWERS = "https://api.twitch.tv/kraken/channels/$values"; // Base of the Subscribers Request URL https://api.twitch.tv/kraken/channels/channel_name/follows
    String authToken = null; // Authentication token of the user


    public static void main(String args[]) throws Exception {
        stlFrame = new stlFrame(); //Register stlFrame.java to be used.
    }

    /**
     *
     * @param url any URL needed to be opened in a browser window.
     */
    public static void openURLInBrowser(String url) {
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


    public static Boolean authMe(String twitchName) {
        Twitch twitch = new Twitch(); //getting twitch as Twitch from TwitchAPI Wrapper
        twitch.setClientId("5fu22trjshv34ervh1vp1xc28ob011f"); //StellarisTwitchList Client ID
        URI callbackUri = URI.create("http://127.0.0.1:23522/authorize.html"); //Authentication URL
        String authUrl = twitch.auth().getAuthenticationUrl(twitch.getClientId(), callbackUri, Scopes.CHANNEL_SUBSCRIPTIONS); //Gets Authorization Request with permission to view channel subs
        if(debug) {System.out.println(authUrl);}
        openURLInBrowser(authUrl);
        boolean authSuccess = twitch.auth().awaitAccessToken();

        if (authSuccess) {
            String accessToken = twitch.auth().getAccessToken();
            System.out.println("Access Token: " + accessToken);
            return true;
            }
         else {
            stlFrame.updateLabel("Authentication Error!");
            System.out.println(twitch.auth().getAuthenticationError());
        }
        return null;

    }

    //Request sent from the GUI. Handles all functionality, passing needed strings onto other methods.
    static void processAll(String twitchName) throws IOException {
        try{
            boolean authorized = authMe(twitchName);
            System.out.println("Authorization Status: " + authorized);

            if (authorized) {
                String namelist = url(twitchName, 100, 0, 0, "", null);
                System.out.println("Namelist: " + namelist);
                stlFrame.updateLabel("Getting Twitch Subscribers...");
                System.out.println("Namelist final text: " + namelist);
                stringReplace(namelist, twitchName);
                stlFrame.listCreated(twitchName);
            }

//            if (namelist != null) {
//                stlFrame.updateLabel("Namelist Request Success!");
//                return namelist;

        } catch (IOException e){
            System.out.println("Error in processAll");
        }

            }


    static String readFile(String filename, String twitchName) throws IOException {

        File cwdFile = new File(twitchName + ".txt");
        String cwd = cwdFile.getAbsolutePath();
        System.out.println(filename);
        InputStream is = Main.class.getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        PrintStream out = new PrintStream(cwd);
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                if (line != "null") {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                    out.println(line);
                }
            }
            is.close();
            out.close();
            br.close();
            return sb.toString();
        } finally {
            br.close();
        }
    }

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


    public static String authToken(String token) {
        String authtoken = token;
                return token;
    }

    private static String insertURLValues(String url, String channel, int limit, int offset) {
        if (stlFrame.useFollows) {
            return url.replace("$values", channel + "/follows" /**+ "?oauth_token=" + token*/ + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset));
        }
        return url.replace("$values", channel + "/subscriptions" /**+ "?oauth_token=" + token*/ + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset));
    }

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

    private static String usernamesFormat(ArrayList<String> list) {
        String listString = "";
        for (String s : list) {
            listString += s + " ";
        }

        return listString;
    }


    //First reference should be url(username, 100, 0, 0, null)
    public static String url(String twitchUsername, int limit, int offset, int subTotal, String parsedOutput, String parsedInput) {


        try {
            URL url = new URL(insertURLValues(TWITCH_SUBSCRIBERS, twitchUsername, limit, offset));
            System.out.println(url);
            stlFrame.updateLabel("Generating URL, Requesting");

            URLConnection connection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = br.readLine();
            br.close();
            JsonObject jsonObject = Json.parse(inputLine).asObject();

            //TODO: catch error if user doesn't have Subscription features.
//            String test = jsonObject.get("error").asObject().toString();
//            System.out.println(test);
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
                url(twitchUsername, limit, offset + 100, total, parsedInput, null);
            } else if (subTotal < offset) {
                return parsedInput;
            }


        } catch (IOException e) {
            e.printStackTrace();
            stlFrame.updateLabel("Error!");
            stlFrame.popupWindow("Error -- Do you not have any subscribers?");
            return null;
        }
        return parsedInput;
    }


    @SuppressWarnings("deprecation")
    private static ArrayList<String> parseJSON(String input) {
        JsonObject jsonObject = Json.parse(input).asObject();
        JsonArray subs;
        if ( stlFrame.useFollows) {
             subs = Json.parse(input).asObject().get("follows").asArray();
        } else {
             subs = Json.parse(input).asObject().get("subscriptions").asArray();
        }
        ArrayList<String> subList = new ArrayList<>();
        for (com.eclipsesource.json.JsonValue sub : subs) {
            subList.add(sub.toString() + "\n");
        }
        String listString = "";
        for (String s : subList) {
            listString += s + " ";
        }
        return subList;

        //        Old Debug stuff
//        System.out.println(followList.size());
//        System.out.println((!jsonObject.get("_total").isNull()));
//        System.out.println("Total: " + jsonObject.get("_total") + "\n_links: " + jsonObject.get("_links") + "\n_links.self: " + jsonObject.get("_links, self"));

    }

}

