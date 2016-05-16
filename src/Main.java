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

public class Main {

    private static stlFrame stlFrame;

    private static String TWITCH_FOLLOWERS = "https://api.twitch.tv/kraken/channels/$values";
    //Subscription request URL: https://api.twitch.tv/kraken/channels/userName/subscriptions

    public static void main(String args[]) throws Exception{
        stlFrame = new stlFrame();

    }

    //Request sent from the GUI. Handles all functionality, passing needed strings onto other methods.
    static void processAll(String twitchName) throws IOException{
        String namelist = null;
        String twitchSubs = null;

         twitchSubs = readFile("twitchList.txt", twitchName);
            if (twitchSubs != null) {
                stlFrame.updateLabel("Getting Twitch Subscribers...");
                 namelist = url(twitchName, 100, 0, 0, "", null);
                if (namelist != null) {
                    stlFrame.updateLabel("Printing to the list...");
                    System.out.println("Namelist final text: " + namelist);
                    stringReplace(namelist, twitchName);
                    stlFrame.updateLabel("");
                    stlFrame.listCreated(twitchName);
                }
            } else {
                stlFrame.updateLabel("Error! No Twitch Subs?");
            }







    }

    static String readFile(String filename, String twitchName) throws IOException {

        File cwdFile = new File (twitchName + ".txt");
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

        File cwdFile = new File (twitchname + ".txt");
        String cwd = cwdFile.getAbsolutePath();
        System.out.println(cwd);

        for (String fn : new String[]{cwd}) {
            String s = new String(Files.readAllBytes(Paths.get(fn)));
            s = s.replace("subscriberList", namelist);
            try (FileWriter fw = new FileWriter(cwd)) {
                if (s != null) {
                    fw.write(s);
                } fw.close();
            }
        }


    }

    public static boolean authMe() {
        boolean debug = true; //TODO: Remove debugging authpass
        if (debug) { return true;}
        Twitch twitch = new Twitch();
        twitch.setClientId("5fu22trjshv34ervh1vp1xc28ob011f"); //StellarisTwitchList client ID
        URI callbackUri = URI.create("http://127.0.0.1:23522/authorize.html");
        String authUrl = twitch.auth().getAuthenticationUrl(twitch.getClientId(), callbackUri, Scopes.USER_READ, Scopes.CHANNEL_READ);
        System.out.println(authUrl);

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            try {
                desktop.browse(new URI(authUrl));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + authUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //TODO: re-enable this when launching. Auth servers needed for Sub list
        boolean authSuccess = twitch.auth().awaitAccessToken();

        if (authSuccess) {
            String accessToken = twitch.auth().getAccessToken();
            System.out.println("Access Token: " + accessToken);
            return true;
        } else {
            System.out.println(twitch.auth().getAuthenticationError());
        } return false;

    }

    private static String insertURLValues(String url, String channel, int limit, int offset) {
        // https:
        return url.replace("$values", channel + "/subscriptions/" + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset));
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
            URL url = new URL(insertURLValues(TWITCH_FOLLOWERS, twitchUsername, limit, offset));
            System.out.println(url);
            URLConnection connection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = br.readLine();
            br.close();

            JsonObject jsonObject = Json.parse(inputLine).asObject();

            int total = Integer.parseInt(jsonObject.get("_total").toString());

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
        }
        return parsedInput;
    }


    @SuppressWarnings("deprecation")
    private static ArrayList<String> parseJSON(String input) {
        JsonObject jsonObject = Json.parse(input).asObject();
        JsonArray follows = Json.parse(input).asObject().get("follows").asArray();
        ArrayList<String> followList = new  ArrayList<String>();
        for (com.eclipsesource.json.JsonValue follow : follows) {
            followList.add(follow.toString() + "\n");
        }


//        System.out.println(followList.size());
//        System.out.println((!jsonObject.get("_total").isNull()));
//        System.out.println("Total: " + jsonObject.get("_total") + "\n_links: " + jsonObject.get("_links") + "\n_links.self: " + jsonObject.get("_links, self"));


        String listString = "";
        for (String s : followList) {
            listString += s + " ";
        }
        return followList;
    }
}

