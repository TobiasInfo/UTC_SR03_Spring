package fr.utc.sr03.ChatSR03Admin.Controller;

import fr.utc.sr03.ChatSR03Admin.Security.PasswordGenerator;
import fr.utc.sr03.ChatSR03Admin.entity.User;
import fr.utc.sr03.ChatSR03Admin.repository.UserRepository;
import fr.utc.sr03.ChatSR03Admin.Security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserRestController {


    private static final Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider JwtTokenProvider;

    /**
     * Retrieves all users.
     *
     * @return a list of all users.
     */
    @GetMapping("userList")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to retrieve.
     * @return an Optional containing the user if found, otherwise empty.
     */
    @GetMapping("/id/{id}")
    public Optional<User> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id);
    }

    /**
     * Updates a user with the given ID.
     *
     * @param id   the ID of the user to update.
     * @param user the user data to update.
     * @return the updated user.
     * @throws ResponseStatusException if the user with the given ID is not found.
     */
    @PutMapping("/update/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User user) {
        User userToUpdate = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with this id"));
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setPassword(user.getPassword());
        userToUpdate.setActivated(user.isActivated());
        userToUpdate.setAdmin(user.isAdmin());
        userToUpdate.setLoginAttempts(user.getLoginAttempts());
        userToUpdate.setFirstName(user.getFirstName());
        userToUpdate.setLastName(user.getLastName());
        return userRepository.save(userToUpdate);
    }

    /**
     * Registers a new user.
     *
     * @param user the user to register.
     * @return a ResponseEntity containing the registered user.
     * @throws ResponseStatusException if a user with the same email already exists.
     */
    @PostMapping("/inscription")
    public ResponseEntity<User> inscription(@RequestBody User user) {
        User userFromDb = userRepository.findByEmail(user.getEmail());
        if (userFromDb != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
        user.setLoginAttempts(0);
        user.setActivated(true);
        user.setAdmin(false);
        User userSaved = userRepository.save(user);
        return ResponseEntity.ok(userSaved);
    }

    /**
     * Handles forgot password requests.
     *
     * @param payload a map containing the email of the user who forgot their password.
     * @throws ResponseStatusException if no user with the given email is found.
     */
    @PostMapping("/forgotPassword")
    public void forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        User userFromDb = userRepository.findByEmail(email);
        if (userFromDb != null) {
            String newPassword = PasswordGenerator.generateRandomPassword();
            userFromDb.setPassword(newPassword);
            userRepository.save(userFromDb);
            LOGGER.info("New password sent to {} : {}", email, newPassword);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with this email");
        }
    }

    /**
     * Handles user login.
     *
     * @param user the user attempting to log in.
     * @return a ResponseEntity containing the logged-in user and a JWT token.
     * @throws ResponseStatusException if the email or password is missing, the user is not found, the account is deactivated, or the password is incorrect.
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> postLogin(@RequestBody User user) {
        LOGGER.info("User :{} logged in", user.getEmail());
        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email or password is missing");
        }

        User userFromDb = userRepository.findByEmail(user.getEmail());
        if (userFromDb == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with this email");
        }

        if (!userFromDb.isActivated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is deactivated");
        }

        if (userFromDb.getPassword().equals(user.getPassword())) {
            String token = JwtTokenProvider.createSimpleToken(userFromDb.getIdUser(), userFromDb.getEmail(), userFromDb.isAdmin());
            LOGGER.info("Voici le token généré " + token);
            userFromDb.setLoginAttempts(0);
            userRepository.save(userFromDb);
            return ResponseEntity.ok()
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body(userFromDb);
        } else {
            userFromDb.setLoginAttempts(userFromDb.getLoginAttempts() + 1);
            if (userFromDb.getLoginAttempts() >= 3) {
                userFromDb.setActivated(false);
                userRepository.save(userFromDb);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is deactivated");
            } else {
                userRepository.save(userFromDb);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect login or password");
            }
        }
    }
}
