package fr.utc.sr03.ChatSR03Admin.repository;

import fr.utc.sr03.ChatSR03Admin.entity.Chat;
import fr.utc.sr03.ChatSR03Admin.entity.Invitation;
import fr.utc.sr03.ChatSR03Admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Repository interface for managing Invitation entities.
 */
public interface InvitationRepository extends JpaRepository<Invitation, Integer> {

    /**
     * Saves an invitation entity.
     *
     * @param invitation the invitation entity to save.
     * @return the saved invitation entity.
     */
    @Override
    @NonNull
    Invitation save(@NonNull Invitation invitation);

    /**
     * Finds all invitations by chat.
     *
     * @param chat the chat entity.
     * @return a list of invitations associated with the specified chat.
     */
    List<Invitation> findByChat(Chat chat);

    Invitation findByChatAndUser(Chat c, User u);
    /**
     * Deletes an invitation entity.
     *
     * @param invitation the invitation entity to delete.
     */
    void delete(@NonNull Invitation invitation);
}

