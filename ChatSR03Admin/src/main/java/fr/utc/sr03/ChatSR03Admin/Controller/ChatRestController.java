package fr.utc.sr03.ChatSR03Admin.Controller;

import fr.utc.sr03.ChatSR03Admin.entity.Chat;
import fr.utc.sr03.ChatSR03Admin.entity.Invitation;
import fr.utc.sr03.ChatSR03Admin.entity.User;
import fr.utc.sr03.ChatSR03Admin.repository.ChatRepository;
import fr.utc.sr03.ChatSR03Admin.repository.InvitationRepository;
import fr.utc.sr03.ChatSR03Admin.repository.UserRepository;
import fr.utc.sr03.ChatSR03Admin.websocket.WebSocketServer;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RestController for handling chat-related API requests.
 */
@RestController
@RequestMapping("api/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatRestController.class);

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    /**
     * Retrieves all chats by user.
     *
     * @param idUser the user ID.
     * @param page   the page number.
     * @param size   the page size.
     * @return ResponseEntity containing a page of chats for the user.
     */
    @GetMapping("/all/{idUser}")
    public ResponseEntity<Page<Chat>> getAllChatsByUser(
            @PathVariable Integer idUser,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "2") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Date currentDate = new Date();

        // Fetching chats where user is owner
        List<Chat> ownerChats = chatRepository.findByOwnerId(idUser);
        // Fetching chats where user is guest and not expired
        List<Chat> guestChats = chatRepository.findByGuestId(idUser, currentDate);

        // Combining both lists
        List<Chat> combinedList = new ArrayList<>();
        combinedList.addAll(ownerChats);
        combinedList.addAll(guestChats);

        removeExpiredChats(combinedList);

        // Creating a sublist based on the page and size
        int start = Math.min((int) pageable.getOffset(), combinedList.size());
        int end = Math.min((start + pageable.getPageSize()), combinedList.size());
        List<Chat> subList = combinedList.subList(start, end);

        // Creating a Page object for the sublist
        Page<Chat> result = new PageImpl<>(subList, pageable, combinedList.size());

        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves the set of connected users for a specific chat.
     *
     * @param idChat the chat ID.
     * @return Set of user IDs of connected users.
     */
    @GetMapping("/connectedUsers/{idChat}")
    public Set<Integer> getConnectedUsers(@PathVariable Integer idChat) {
        WebSocketServer webSocketServer = WebSocketServer.getInstance();
        Set<Session> sessions = webSocketServer.getSessions(idChat);
        if (sessions == null) {
            LOGGER.info("No sessions found for chat {}", idChat);
            return Set.of();
        }
        return sessions.stream()
                .map(session -> (Integer) session.getUserProperties().get("idUser"))
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves the users participating in a specific chat.
     *
     * @param chatId the chat ID.
     * @return ResponseEntity containing the list of users in the chat.
     */
    @GetMapping("/users/{chatId}")
    public ResponseEntity<List<User>> getUserByChat(@PathVariable Integer chatId) {
        List<User> user = chatRepository.findGuestByChatId(chatId);
        User tmp = chatRepository.findOwnerByChatId(chatId);
        user.add(tmp);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves a chat by its ID.
     *
     * @param chatId the chat ID.
     * @return ResponseEntity containing the chat.
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<Chat> getChatById(@PathVariable Integer chatId) {
        return ResponseEntity.ok(chatRepository.findById(chatId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found")));
    }

    /**
     * Creates a new chat with a specified chat object.
     *
     * @param newChat the new chat object.
     * @return ResponseEntity containing the created chat.
     */
    @PostMapping("/create")
    public ResponseEntity<Chat> updateChat(@RequestBody Chat newChat) {
        newChat.setIdChat(0);
        chatRepository.save(newChat);
        return ResponseEntity.ok(newChat);
    }

    /**
     * Invites a user to a chat.
     *
     * @param userId the user ID.
     * @param chatId the chat ID.
     * @return ResponseEntity containing the created invitation.
     */
    @PutMapping("/invite/{userId}/{chatId}")
    public ResponseEntity<Invitation> inviteUserToChat(@PathVariable Integer userId, @PathVariable Integer chatId) {
        Invitation i = new Invitation(
                userRepository.findById(userId).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")),
                chatRepository.findById(chatId).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"))
        );

        invitationRepository.save(i);
        return ResponseEntity.ok(i);
    }

    /**
     * Updates an existing chat.
     *
     * @param chatId  the chat ID.
     * @param newChat the new chat details.
     * @return ResponseEntity containing the updated chat.
     */
    @PutMapping("/update/{chatId}")
    public ResponseEntity<Chat> updateChat(@PathVariable Integer chatId, @RequestBody Chat newChat) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));
        chat.setTitle(newChat.getTitle());
        chat.setDescription(newChat.getDescription());
        chat.setDateExpiration(newChat.getDateExpiration());
        chatRepository.save(chat);
        return ResponseEntity.ok(chat);
    }

    /**
     * Deletes a chat and its invitations.
     *
     * @param chatId the chat ID.
     * @return ResponseEntity indicating the result of the deletion.
     */
    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<?> deleteChat(@PathVariable Integer chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));
        List<Invitation> i = invitationRepository.findByChat(chat);
        for (Invitation inv : i) {
            invitationRepository.delete(inv);
        }
        chatRepository.delete(chat);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invitation/{idChat}/{idUser}")
    public ResponseEntity<?> deleteInvitation(
            @PathVariable Integer idChat,
            @PathVariable Integer idUser
    ) {
        Chat chat = chatRepository.findChatByIdChat(idChat);
        User user = userRepository.findById(idUser).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with this id"));
        Invitation i = invitationRepository.findByChatAndUser(chat, user);
        invitationRepository.delete(i);
        return ResponseEntity.ok().build();
    }
    /**
     * Remove the expired chats from the list
     *
     * @param chats
     */
    private void removeExpiredChats(List<Chat> chats) {
        Date now = new Date();
        chats.removeIf(chat -> chat == null || (chat.getDateExpiration() != null && chat.getDateExpiration().before(now)));
    }

}
