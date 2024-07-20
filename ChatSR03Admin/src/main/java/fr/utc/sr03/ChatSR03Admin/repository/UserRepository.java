package fr.utc.sr03.ChatSR03Admin.repository;

import fr.utc.sr03.ChatSR03Admin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface for managing User entities.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Saves a user entity.
     *
     * @param user the user entity to save.
     * @return the saved user entity.
     */
    @Override
    @NonNull
    User save(@NonNull User user);

    /**
     * Finds a user by their email.
     *
     * @param email the email of the user to find.
     * @return the user with the specified email.
     */
    User findByEmail(String email);

    /**
     * Finds a user by their email and password.
     *
     * @param email    the email of the user to find.
     * @param password the password of the user to find.
     * @return the user with the specified email and password.
     */
    User findByEmailAndPassword(String email, String password);

    /**
     * Finds all users.
     *
     * @param pageable the pagination information.
     * @return a list of all users.
     */
    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    /**
     * Finds all activated users.
     *
     * @param pageable the pagination information.
     * @return a list of all activated users.
     */
    @NonNull
    Page<User> findAllByIsActivatedTrue(Pageable pageable);

    /**
     * Finds all deactivated users.
     *
     * @param pageable the pagination information.
     * @return a list of all deactivated users.
     */
    @NonNull
    Page<User> findAllByIsActivatedFalse(Pageable pageable);

    /**
     * Finds all users by activation status and matching either the first name or the last name (case-insensitive).
     *
     * @param isActivated  the activation status of the user.
     * @param firstName    the first name to search for (case-insensitive).
     * @param isActivated2 the activation status of the user (used for the last name search).
     * @param lastName     the last name to search for (case-insensitive).
     * @param pageable     the pagination information.
     * @return a paginated list of users matching the specified activation status and either the first name or the last name.
     */
    @NonNull
    Page<User> findByIsActivatedAndFirstNameContainingIgnoreCaseOrIsActivatedAndLastNameContainingIgnoreCase(
            boolean isActivated,
            String firstName,
            boolean isActivated2,
            String lastName,
            Pageable pageable
    );

    /**
     * Finds all users by matching either the first name or the last name (case-insensitive).
     *
     * @param firstName the first name to search for (case-insensitive).
     * @param lastName  the last name to search for (case-insensitive).
     * @param pageable  the pagination information.
     * @return a paginated list of users matching either the first name or the last name.
     */
    @NonNull
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete.
     */
    void deleteById(@NonNull Integer id);

    /**
     * Deletes a user entity.
     *
     * @param user the user entity to delete.
     */
    void delete(@NonNull User user);
}
