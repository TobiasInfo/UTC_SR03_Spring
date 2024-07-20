package fr.utc.sr03.ChatSR03Admin.repository;

import fr.utc.sr03.ChatSR03Admin.entity.Chat;
import fr.utc.sr03.ChatSR03Admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.List;

/**
 * Repository interface for managing Chat entities.
 */
public interface ChatRepository extends JpaRepository<Chat, Integer> {

    /**
     * Saves a chat entity.
     *
     * @param chat the chat entity to save.
     * @return the saved chat entity.
     */
    @Override
    @NonNull
    Chat save(@NonNull Chat chat);

    /*

     */
    @NonNull
    Chat findChatByIdChat(Integer idChat);
    /**
     * Finds all chats by owner ID.
     *
     * @param ownerId the ID of the owner.
     * @return a list of chats owned by the specified owner.
     */
    List<Chat> findByOwnerId(Integer ownerId);

    /**
     * Finds all chat entities.
     *
     * @return a list of all chat entities.
     */
    @NonNull
    List<Chat> findAll();

    /**
     * Deletes a chat by its ID.
     *
     * @param idChat the ID of the chat to delete.
     */
    void deleteById(@NonNull Integer idChat);

    /**
     * Deletes a chat entity.
     *
     * @param chat the chat entity to delete.
     */
    void delete(@NonNull Chat chat);

    /**
     * Finds chats where the specified user is a guest and the chat has not expired.
     *
     * @param guestId the ID of the guest.
     * @param currentDate the current date to check expiration.
     * @return a list of chats where the user is a guest and the chat has not expired.
     */
    @Query("SELECT c FROM Chat c INNER JOIN Invitation i ON c.idChat = i.chat.idChat WHERE i.user.idUser = :guestId AND c.dateExpiration > :currentDate")
    List<Chat> findByGuestId(@Param("guestId") Integer guestId, @Param("currentDate") Date currentDate);

    /**
     * Finds guests by chat ID.
     *
     * @param chatId the ID of the chat.
     * @return a list of users who are guests in the specified chat.
     */
    @Query("SELECT u FROM User u INNER JOIN Invitation i ON u.idUser = i.user.idUser WHERE i.chat.idChat = :chatId")
    List<User> findGuestByChatId(Integer chatId);

    /**
     * Finds the owner of a chat by chat ID.
     *
     * @param chatId the ID of the chat.
     * @return the user who owns the specified chat.
     */
    @Query("SELECT u FROM User u INNER JOIN Chat c ON c.ownerId = u.idUser WHERE c.idChat = :chatId")
    User findOwnerByChatId(Integer chatId);
}
