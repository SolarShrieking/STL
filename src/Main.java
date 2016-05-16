import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

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

    public static void main(String args[]) throws Exception{
        stlFrame = new stlFrame();

        String test = readFile("twitchList.txt");
        String namelist = url("SolarShrieking", 100, 0, 0, "", null);
        System.out.println("Namelist final text: " + namelist);
        stringReplace(namelist);
    }

    static String readFile(String filename) throws IOException {
        System.out.println(filename);
        InputStream is = Main.class.getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        PrintStream out = new PrintStream("C:\\Users\\Michael\\IdeaProjects\\STL\\src\\copyTwitch.txt");
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
                out.println(line);
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    static void stringReplace(String namelist) throws IOException {
        for (String fn : new String[]{"C:\\Users\\Michael\\IdeaProjects\\STL\\src\\copyTwitch.txt"}) {
            String s = new String(Files.readAllBytes(Paths.get(fn)));
            s = s.replace("subscriberList", namelist);
            try (FileWriter fw = new FileWriter(fn)) {
                fw.write(s);
            }
        }


    }

    public static void authMe() {
       // Twitch twitch = new Twitch();
      //  twitch.setClientId("5fu22trjshv34ervh1vp1xc28ob011f"); //StellarisTwitchList client ID
        URI callbackUri = URI.create("http://127.0.0.1:23522/authorize.html");
        String authUrl = "facebook.com"; //twitch.auth().getAuthenticationUrl(twitch.getClientId(), callbackUri, Scopes.USER_READ, Scopes.CHANNEL_READ);
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
        boolean authSuccess = true; // twitch.auth().awaitAccessToken();


        authSuccess = true; //TODO: Remove Debug

        if (authSuccess) {
            String accessToken = "replace"; //twitch.auth().getAccessToken();
            System.out.println(url("SolarShrieking", 100, 0, 0, "", null));
            //System.out.println("Access Token: " + accessToken);
        } else {
            //System.out.println(twitch.auth().getAuthenticationError());
        }
    }

    private static String insertURLValues(String url, String channel, int limit, int offset) {
        return url.replace("$values", channel + "/follows/" + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset));
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

            JsonObject jsonObject = JsonObject.readFrom(inputLine);
            int total = Integer.parseInt(jsonObject.get("_total").toString());

            parsedInput = usernamesFormat(parseList(parseJSON(inputLine)));
            System.out.println("post-parse: " + parsedInput);
            parsedInput = parsedInput.concat(parsedOutput);
            System.out.println("post-concat: " + parsedInput);

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


    private static ArrayList<String> parseJSON(String input) {
        JsonObject jsonObject = JsonObject.readFrom(input);
        JsonArray follows = Json.parse(input).asObject().get("follows").asArray();
        ArrayList<String> followList = new ArrayList<String>();
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

