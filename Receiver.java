/**

 * Project: Lab 5
 * Purpose Details: HMAC HTTP Server
 * Course: IST 242
 * Author: Thomas Koltes
 * Date Developed: 4/7/25
 * Last Date Changed: 4/10/25
 * Rev: 2

 */

import com.rabbitmq.client.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Receiver {
    private static final String QUEUE_NAME = "secure_queue";
    private static final String SECRET_KEY = "132194";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("Waiting for messages...");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String fullPayload = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String[] parts = fullPayload.split("\\|");
            String message = parts[0];
            String receivedHmac = parts[1];
            String generatedHmac = null;
            try {
                generatedHmac = generateHmacSHA256(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("Received Message: " + message);
            System.out.println("Received HMAC:   " + receivedHmac);
            System.out.println("Generated HMAC:  " + generatedHmac);
            if (generatedHmac.equals(receivedHmac)) {
                System.out.println("Success. Message received.");
            } else {
                System.out.println("Error! Resend message.");
            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
    private static String generateHmacSHA256(String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(Receiver.SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secret);
        byte[] hmacBytes = mac.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}
