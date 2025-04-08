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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HmacHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/verify", new HmacHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000...");
    }

    static class HmacHandler implements HttpHandler {
        private static final String SECRET_KEY = "Key";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new BufferedReader(new InputStreamReader(is))
                        .lines().reduce("", (acc, line) -> acc + line);

                System.out.println("Received payload: " + body);

                // Extract message and HMAC from JSON
                String message = body.split("\"message\":\"")[1].split("\"")[0];
                String receivedHmac = body.split("\"hmac\":\"")[1].split("\"")[0];

                System.out.println("Message received: " + message);
                System.out.println("HMAC received: " + receivedHmac);

                String generatedHmac = generateHMAC(message, SECRET_KEY);
                System.out.println("HMAC generated: " + generatedHmac);

                String response;
                if (receivedHmac.equals(generatedHmac)) {
                    response = "HMAC verified successfully!";
                } else {
                    response = "HMAC verification failed!";
                }

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }

        private String generateHMAC(String data, String key) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
                mac.init(secretKey);
                return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException("Error generating HMAC", e);
            }
        }
    }
}
