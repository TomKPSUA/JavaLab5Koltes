/**

 * Project: Lab 5
 * Purpose Details: HMAC HTTP Server
 * Course: IST 242
 * Author: Thomas Koltes
 * Date Developed: 4/7/25
 * Last Date Changed: 4/7/25
 * Rev: 1

 */

package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HmacHttpClient {
    public static void main(String[] args) {
        String message = "Hello";
        String secretKey = "Key";

        try {
            String hmac = generateHMAC(message, secretKey);
            System.out.println("Message to send: " + message);
            System.out.println("HMAC to send: " + hmac);

            String payload = "{\"message\":\"" + message + "\",\"hmac\":\"" + hmac + "\"}";

            URL url = new URL("http://localhost:8000/verify");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Print server response
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Server response: " + line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateHMAC(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKey);
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
    }
}
