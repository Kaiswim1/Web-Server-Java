import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class GameClient {

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

    // Method to send data (POST request)
    public static void sendData(int score) throws IOException {
        URL url = new URL(SERVER_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // JSON data to send
        String jsonInputString = "{\"score\": " + score + "}";

        // Send the data
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Check response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { // Success
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    System.out.println(responseLine);
                }
            }
        } else {
            System.out.println("Error: " + connection.getResponseMessage());
        }
    }

    // Method to get data (GET request)
    public static void getData() throws IOException {
        URL url = new URL(SERVER_URL);
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

    public static void main(String[] args) {
        // Dynamically retrieve the local IP address and construct the server URL
        String localIP = getLocalIPAddress();
        if (localIP != null) {
            SERVER_URL = "http://" + localIP + ":3000/data"; // Using the local IP address for server communication
            System.out.println("Connecting to server at: " + SERVER_URL);

            try {
                // Example of sending and receiving data
                sendData(180); // Uncomment to send a new score
                getData();    // Retrieve the current score
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Local IP not found.");
        }
    }
}
