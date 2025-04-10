/**

 * Project: Lab 5
 * Purpose Details: HMAC HTTP Server
 * Course: IST 242
 * Author: Thomas Koltes
 * Date Developed: 4/7/25
 * Last Date Changed: 4/10/25
 * Rev: 2

 */


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


public class Sender {
    private static final String QUEUE_NAME = "secure_queue";
    private static final String SECRET_KEY = "132194";
    public static void main(String[] args) throws Exception {
        String message = "The weather is nice today";
        String hmac = generateHmacSHA256(message, SECRET_KEY);
        String fullPayload = message + "|" + hmac;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, fullPayload.getBytes());
            System.out.println("Sent Message: " + message);
            System.out.println("Sent HMAC:    " + hmac);
        }
    }
    private static String generateHmacSHA256(String message, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secret);
        byte[] hmacBytes = mac.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}
