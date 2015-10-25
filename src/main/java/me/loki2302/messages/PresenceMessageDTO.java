package me.loki2302.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserHasJoinedMessageDTO.class, name = "join"),
        @JsonSubTypes.Type(value = UserHasLeftMessageDTO.class, name = "leave"),
})
public abstract class PresenceMessageDTO {
    public String user;
}
