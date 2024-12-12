import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class GameHoster {

    // URL of the Node.js server - dynamically assigned based on local IP
    private static String SERVER_URL;

    // Method to get the local IP address
    public static String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    // Check if the IP is not the loopback address (127.0.0.1) and is IPv4
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to send a specific variable (POST request)
    public static void sendData(String key, int value) throws IOException {
        URL url = new URL(SERVER_URL + "data");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // JSON data to send
        String jsonInputString = String.format("{\"key\":\"%s\",\"value\":%d}", key, value);


        // Send the data
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Check response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Value updated successfully.");
        } else {
            System.out.println("Error: " + connection.getResponseMessage());
        }
    }

    // Method to get a specific variable by key (GET request)
    // Method to get a specific variable by key (GET request)
    // Method to get a specific variable by key (GET request)
    public static void getData(String key) throws IOException {
        // Fix the URL to match the server route (/data/:key)
        URL url = new URL(SERVER_URL + "data/" + key);  // Use "data/" before the key
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get the response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                System.out.println("Response from server: " + responseLine);
            }
        }
    }



    // Method to connect to the server and demonstrate sending/retrieving data
    public static void connect() {
        String localIP = getLocalIPAddress();
        if (localIP != null) {
            SERVER_URL = "http://" + localIP + ":3000/";
            System.out.println("Connecting to server at: " + SERVER_URL);

            Thread thread = new Thread(() -> {
                try {
                    sendData("score", 123); // Update score
                    sendData("level", 5); // Update level
                    sendData("time", 120); // Update time

                    getData("score"); // Retrieve score
                    getData("level"); // Retrieve level
                    getData("time"); // Retrieve time
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        } else {
            System.out.println("Local IP not found.");
        }
    }

    public static void main(String[] args) {
        connect();
    }
}
