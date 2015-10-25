package me.loki2302;

import me.loki2302.messages.*;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@IntegrationTest
@WebAppConfiguration
@SpringApplicationConfiguration(classes = AppConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AppTest {
    private final Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void dummy() throws ExecutionException, InterruptedException {
        ChatClient chatClient1 = new ChatClient("testuser1", "testpassword1");
        chatClient1.connect();
        Thread.sleep(1000);
        chatClient1.say("hey there, I am user1!");
        Thread.sleep(1000);
        ChatClient chatClient2 = new ChatClient("testuser2", "testpassword2");
        chatClient2.connect();
        Thread.sleep(1000);
        chatClient2.say("hey there, I am user2!");
        Thread.sleep(1000);
        chatClient1.disconnect();
        Thread.sleep(1000);
        chatClient2.disconnect();
        Thread.sleep(1000);

        List<String> chatClient1Events = chatClient1.getEvents();
        logger.info("chatClient1Events: {}", chatClient1Events);

        List<String> chatClient2Events = chatClient2.getEvents();
        logger.info("chatClient2Events: {}", chatClient2Events);

        assertEquals(chatClient1Events, Arrays.asList(
                "testuser1 says hey there, I am user1!",
                "testuser2 has joined",
                "testuser2 says hey there, I am user2!"
        ));

        assertEquals(chatClient2Events, Arrays.asList(
                "testuser2 says hey there, I am user2!",
                "testuser1 has left"
        ));
    }

    public static class ChatClient {
        private final Logger logger;
        private final String username;
        private final String password;
        private final List<String> events = new ArrayList<>();
        private StompSession stompSession;

        public ChatClient(String username, String password) {
            logger = LoggerFactory.getLogger(String.format("ChatClient(%s)", username));
            this.username = username;
            this.password = password;
        }

        public List<String> getEvents() {
            return events;
        }

        public void connect() throws ExecutionException, InterruptedException {
            WebSocketClient webSocketClient = new StandardWebSocketClient();
            WebSocketStompClient webSocketStompClient = new WebSocketStompClient(webSocketClient);
            webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

            WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
            webSocketHttpHeaders.add("Authorization", makeBasicHttpAuthorizationToken(username, password));

            stompSession = webSocketStompClient.connect(
                    "ws://localhost:8080/chat-endpoint",
                    webSocketHttpHeaders,
                    new ChatSessionHandler()).get();
        }

        public void disconnect() {
            stompSession.disconnect();
        }

        public void say(String text) {
            IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
            incomingMessageDTO.text = text;
            stompSession.send("/in/incoming-messages", incomingMessageDTO);
            logger.info("Sent {}", text);
        }

        private static String makeBasicHttpAuthorizationToken(String username, String password) {
            String usernameColonPasswordString = String.format("%s:%s", username, password);
            String base64EncodedUsernameColonPasswordString = Base64.encodeBase64String(usernameColonPasswordString.getBytes());
            String tokenString = String.format("Basic %s", base64EncodedUsernameColonPasswordString);
            return tokenString;
        }

        private class ChatSessionHandler extends StompSessionHandlerAdapter {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/out/published-messages", new PublishedMessageHandler());
                session.subscribe("/out/presence-messages", new PresenceMessageHandler());
            }
        }

        private class PublishedMessageHandler implements StompFrameHandler {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return PublishedMessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                PublishedMessageDTO publishedMessage = (PublishedMessageDTO)payload;
                logger.info("{} says {}", publishedMessage.user, publishedMessage.text);
                events.add(String.format("%s says %s", publishedMessage.user, publishedMessage.text));
            }
        }

        private class PresenceMessageHandler implements StompFrameHandler {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return PresenceMessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                PresenceMessageDTO presenceMessageDTO = (PresenceMessageDTO)payload;

                if (presenceMessageDTO instanceof UserHasJoinedMessageDTO) {
                    logger.info("{} joins the chat!", presenceMessageDTO.user);
                    events.add(String.format("%s has joined", presenceMessageDTO.user));
                } else if (presenceMessageDTO instanceof UserHasLeftMessageDTO) {
                    logger.info("{} leaves the chat!", presenceMessageDTO.user);
                    events.add(String.format("%s has left", presenceMessageDTO.user));
                } else {
                    throw new RuntimeException("Unknown presence message");
                }
            }
        }
    }
}
