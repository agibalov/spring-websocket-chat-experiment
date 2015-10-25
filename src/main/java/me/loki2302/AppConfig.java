package me.loki2302;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({WebSocketConfig.class, SecurityConfig.class})
public class AppConfig {
    @Bean
    public ChatController chatController() {
        return new ChatController();
    }

    @Bean
    public PresenceListener presenceListener() {
        return new PresenceListener("/out/presence-messages");
    }

}
