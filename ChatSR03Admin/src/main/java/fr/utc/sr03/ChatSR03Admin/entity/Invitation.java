package fr.utc.sr03.ChatSR03Admin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Invitation")
@IdClass(InvitationId.class)
public class Invitation {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_chat")
    private Chat chat;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    public Invitation(User user, Chat chat) {
        this.user = user;
        this.chat = chat;
    }

    public Invitation() {}

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
