package fr.utc.sr03.ChatSR03Admin.entity;

import java.io.Serializable;
import java.util.Objects;

public class InvitationId implements Serializable {
    private Integer chat;
    private Integer user;

    // Default constructor
    public InvitationId() {}

    // Constructor with parameters
    public InvitationId(Integer chat, Integer user) {
        this.chat = chat;
        this.user = user;
    }

    // hashCode and equals methods
    @Override
    public int hashCode() {
        return Objects.hash(chat, user);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InvitationId that = (InvitationId) obj;
        return Objects.equals(chat, that.chat) && Objects.equals(user, that.user);
    }

}
