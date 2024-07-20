package fr.utc.sr03.ChatSR03Admin.Controller;

import fr.utc.sr03.ChatSR03Admin.Security.PasswordGenerator;
import fr.utc.sr03.ChatSR03Admin.repository.UserRepository;
import fr.utc.sr03.ChatSR03Admin.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;


import java.util.ArrayList;
import java.util.Objects;

/**
 * Controller for handling user-related operations such as viewing, editing, and managing users.
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);


    /**
     * Handles GET requests to the admin user management page.
     *
     * @param model         the model to add attributes to.
     * @param page          the current page number.
     * @param size          the number of users per page.
     * @param disabledUsers flag to include disabled users.
     * @param enabledUsers  flag to include enabled users.
     * @param search        the search query for users.
     * @param sort          the sort order (ASC or DESC).
     * @param req           the current web request.
     * @return the name of the view to be rendered.
     */
    @GetMapping("/admin")
    public String getAdmin(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "true") boolean disabledUsers,
            @RequestParam(defaultValue = "true") boolean enabledUsers,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ASC") String sort, // Nouveau paramètre de tri
            WebRequest req
    ) {
        LOGGER.info("=== GET ADMIN ===");
        LOGGER.info("Page : {}", page);
        LOGGER.info("Size : {}", size);
        LOGGER.info("Disabled Users : {}", disabledUsers);
        LOGGER.info("Enabled Users : {}", enabledUsers);
        LOGGER.info("Sort Order : {}", sort);
        if (checkSession(req)) return "redirect:/connexion";
        Sort sorting = Sort.by(sort.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, "lastName");
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<User> users;
        if (disabledUsers) {
            if (enabledUsers) {
                if (search != null && !search.isEmpty()) {
                    users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(search, search, pageable);
                } else {
                    users = userRepository.findAll(pageable);
                }
            } else {
                if (search != null && !search.isEmpty()) {
                    users = userRepository.findByIsActivatedAndFirstNameContainingIgnoreCaseOrIsActivatedAndLastNameContainingIgnoreCase(false, search, false, search, pageable);
                } else {
                    users = userRepository.findAllByIsActivatedFalse(pageable);
                }
            }
        } else {
            if (enabledUsers) {
                if (search != null && !search.isEmpty()) {
                    users = userRepository.findByIsActivatedAndFirstNameContainingIgnoreCaseOrIsActivatedAndLastNameContainingIgnoreCase(true, search, true, search, pageable);
                } else {
                    users = userRepository.findAllByIsActivatedTrue(pageable);
                }
            } else {
                users = new PageImpl<>(new ArrayList<>());
            }
        }
        LOGGER.info("role sent to thymeleaf = {}", getRole(req));
        model.addAttribute("role", getRole(req));
        User loggedUser = (User) req.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (loggedUser != null) {
            int id = loggedUser.getIdUser();
            model.addAttribute("idUser", id);
        }

        model.addAttribute("utilisateurs", users);
        model.addAttribute("disabledUsers", disabledUsers);
        model.addAttribute("enabledUsers", enabledUsers);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        return "admin";
    }

    /**
     * Handles GET requests to edit a specific user.
     *
     * @param model the model to add attributes to.
     * @param id    the ID of the user to edit.
     * @param req   the current web request.
     * @return the name of the view to be rendered.
     */
    @GetMapping("/edit/{id}")
    public String editUser(Model model,
                           @PathVariable("id") Integer id,
                           WebRequest req
    ) {

        LOGGER.info("=== EDIT USER ===");
        if (checkSession(req)) return "redirect:/connexion";
        if (id == null) {
            // Gérer le cas où l'ID est null, par exemple, rediriger vers une page d'erreur
            return "redirect:/error";
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        model.addAttribute("role", getRole(req));
        model.addAttribute("idUser", id);
        model.addAttribute("title", "Edition de l'utilisateur " + user.getFirstName() + " " + user.getLastName());
        model.addAttribute("pageTitle", "Edition de l'utilisateur " + user.getFirstName() + " " + user.getLastName());
        model.addAttribute("user", user);
        return "editUser";
    }

    @GetMapping("/forgotPassword")
    public String forgotPasswordForm() {
        return "forgotPassword";
    }

    /**
     * Handles forgot password requests.
     *
     * @param email the email of the user who forgot their password.
     * @param model the model to add attributes to be rendered by the view.
     * @return the name of the view to be rendered.
     */
    @PostMapping("/forgotPassword")
    public String forgotPassword(@RequestParam String email, Model model) {
        LOGGER.info("=== FORGOT PASSWORD ===");
        User userFromDb = userRepository.findByEmail(email);
        if (userFromDb != null) {
            String newPassword = PasswordGenerator.generateRandomPassword();
            userFromDb.setPassword(newPassword);
            userRepository.save(userFromDb);
            LOGGER.info("New password sent to {} : {}", email, newPassword);
            model.addAttribute("message", "Un nouveau mot de passe a été envoyé à votre adresse email.");
        } else {
            model.addAttribute("error", "Aucun utilisateur trouvé avec cet email.");
        }
        return "forgotPassword"; // Return to the forgotPassword view
    }

    /**
     * Handles POST requests to log out the current user.
     *
     * @param session the current web request.
     * @return a redirect instruction to the login page.
     */
    @PostMapping("/logout")
    public String logout(WebRequest session) {
        LOGGER.info("=== LOGOUT ===");
        session.removeAttribute("role", WebRequest.SCOPE_SESSION);
        return "redirect:/connexion";
    }

    /**
     * Handles POST requests to update a specific user.
     *
     * @param updatedUser the updated user details.
     * @param id          the ID of the user to update.
     * @param req         the current web request.
     * @return a redirect instruction
     * to the admin user management page.
     */
    @PostMapping("/edit/{id}")
    public String editUser(@ModelAttribute User updatedUser,
                           @PathVariable("id") Integer id,
                           WebRequest req
    ) {
        LOGGER.info("=== POST EDIT USER ===");
        if (checkSession(req)) return "redirect:/connexion";
        // Récupérer l'utilisateur existant à partir de la base de données
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Mettre à jour les propriétés de l'utilisateur existant avec les valeurs fournies dans l'objet updatedUser
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setAdmin(updatedUser.isAdmin());
        existingUser.setActivated(updatedUser.isActivated());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());

        // Enregistrer l'utilisateur mis à jour dans la base de données
        userRepository.save(existingUser);

        return "redirect:/user/admin";
    }

    /**
     * Handles POST requests to update the status of a specific user.
     *
     * @param id  the ID of the user to update.
     * @param req the current web request.
     * @return a redirect instruction to the admin user management page.
     */
    @PostMapping("/updatestatus/{id}")
    public String updateUserStatus(@PathVariable("id") Integer id,
                                   WebRequest req) {
        LOGGER.info("=== UPDATE USER STATUS ===");
        if (checkSession(req)) return "redirect:/connexion";
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActivated(!user.isActivated());
        userRepository.save(user);
        return "redirect:/user/admin";
    }

    /**
     * Handles POST requests to update the admin status of a specific user.
     *
     * @param id  the ID of the user to update.
     * @param req the current web request.
     * @return a redirect instruction to the admin user management page.
     */
    @PostMapping("/updateadmin/{id}")
    public String updateUserAdmin(@PathVariable("id") Integer id,
                                  WebRequest req) {
        LOGGER.info("=== UPDATE USER ADMIN ===");
        if (checkSession(req)) return "redirect:/connexion";
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setAdmin(!user.isAdmin());
        userRepository.save(user);
        return "redirect:/user/admin";
    }

    /**
     * Handles POST requests to delete a specific user.
     *
     * @param id  the ID of the user to delete.
     * @param req the current web request.
     * @return a redirect instruction to the admin user management page.
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id,
                             WebRequest req) {
        LOGGER.info("=== DELETE USER ===");
        if (checkSession(req)) return "redirect:/connexion";
        userRepository.deleteById(id);
        return "redirect:/user/admin";
    }

    /**
     * Checks the session to ensure the user has admin privileges.
     *
     * @param req the current web request.
     * @return true if the session is invalid or the user is not an admin, false otherwise.
     */
    private boolean checkSession(WebRequest req) {
        if (req.getAttribute("role", WebRequest.SCOPE_SESSION) == null) {
            LOGGER.warn("La session n'est pas initialisée");
            return true;
        } else if (Objects.equals((String) req.getAttribute("role", WebRequest.SCOPE_SESSION), "USER")) {
            LOGGER.error("L'utilisateur n'as pas les droits admin");
            return true;
        } else {
            LOGGER.info("L'utilisateur est bien admin");
            LOGGER.info((String) req.getAttribute("role", WebRequest.SCOPE_SESSION));
        }
        return false;
    }

    /**
     * Retrieves the role of the current user from the session.
     *
     * @param req the current web request.
     * @return the role of the user as a string.
     */
    private String getRole(WebRequest req) {
        if (req.getAttribute("role", WebRequest.SCOPE_SESSION) == null) {
            return "NotAuthenticated";
        } else {
            return (String) req.getAttribute("role", WebRequest.SCOPE_SESSION);
        }
    }
}
