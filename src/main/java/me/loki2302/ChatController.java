package me.loki2302;

import me.loki2302.messages.IncomingMessageDTO;
import me.loki2302.messages.PublishedMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {
    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @MessageMapping("/incoming-messages")
    @SendTo("/out/published-messages")
    public PublishedMessageDTO publishMessage(
            IncomingMessageDTO incomingMessageDTO,
            Principal author) {

        logger.info("Got incoming message from {}: {}", author.getName(), incomingMessageDTO.text);

        PublishedMessageDTO publishedMessageDTO = new PublishedMessageDTO();
        publishedMessageDTO.user = author.getName();
        publishedMessageDTO.text = incomingMessageDTO.text;

        return publishedMessageDTO;
    }
}
