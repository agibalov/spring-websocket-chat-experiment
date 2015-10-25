package me.loki2302;

import me.loki2302.messages.UserHasJoinedMessageDTO;
import me.loki2302.messages.UserHasLeftMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceListener {
    private final Logger logger = LoggerFactory.getLogger(PresenceListener.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private String presenceDestination;

    public PresenceListener(String presenceDestination) {
        this.presenceDestination = presenceDestination;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent e) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(e.getMessage());
        String username = headerAccessor.getUser().getName();
        logger.info("{} has joined!", username);

        UserHasJoinedMessageDTO messageDTO = new UserHasJoinedMessageDTO();
        messageDTO.user = username;
        simpMessagingTemplate.convertAndSend(presenceDestination, messageDTO);
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent e) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(e.getMessage());
        String username = headerAccessor.getUser().getName();
        logger.info("{} has left!", username);

        UserHasLeftMessageDTO messageDTO = new UserHasLeftMessageDTO();
        messageDTO.user = username;
        simpMessagingTemplate.convertAndSend(presenceDestination, messageDTO);
    }
}
