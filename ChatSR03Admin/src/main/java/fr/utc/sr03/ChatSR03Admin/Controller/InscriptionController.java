package fr.utc.sr03.ChatSR03Admin.Controller;

import fr.utc.sr03.ChatSR03Admin.entity.User;
import fr.utc.sr03.ChatSR03Admin.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller for handling user registration.
 */
@Controller
@RequestMapping("/inscription")
public class InscriptionController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(InscriptionController.class);

    /**
     * Handles GET requests to the registration page.
     *
     * @param model   the model to add attributes to.
     * @param request the current HTTP request.
     * @param req     the current web request.
     * @return the name of the view to be rendered.
     */
    @GetMapping
    public String getInscription(
            Model model,
            HttpServletRequest request,
            WebRequest req) {
        LOGGER.info("=== GET INSCRIPTION ===");
        if (req.getAttribute("role", WebRequest.SCOPE_SESSION) == null) {
            model.addAttribute("role", "NotAuthenticated");
        } else {
            model.addAttribute("role", (String) req.getAttribute("role", WebRequest.SCOPE_SESSION));
            User loggedUser = (User) req.getAttribute("user", WebRequest.SCOPE_SESSION);
            if (loggedUser != null) {
                int id = loggedUser.getIdUser();
                model.addAttribute("idUser", id);
            }
        }
        String referer = request.getHeader("Referer");
        LOGGER.info("Referer : {}", referer);

        if (referer != null && referer.contains("/user/admin")) {
            model.addAttribute("pageTitle", "Ajout d'un utilisateur");
        } else {
            model.addAttribute("pageTitle", "Inscription");
        }
        model.addAttribute("title", "Page d'inscription");
        model.addAttribute("user", new User());
        return "formulaireUser";
    }

    /**
     * Handles POST requests for user registration.
     *
     * @param user  the user to be registered.
     * @param model the model to add attributes to.
     * @return the name of the view to be rendered or a redirect instruction.
     */
    @PostMapping
    public String postInscription(@ModelAttribute User user, Model model) {
        LOGGER.info("=== POST INSCRIPTION ===");
        LOGGER.info("Email User : {}", user.getEmail());
        LOGGER.info("Password User : {}", user.getPassword());
        LOGGER.info("isAdmin User : {}", user.isAdmin());
        LOGGER.info("isActif User : {}", user.isActivated());
        LOGGER.info("firstname User : {}", user.getFirstName());
        LOGGER.info("lastname User : {}", user.getLastName());

        model.addAttribute("title", "Page d'inscription");
        model.addAttribute("pageTitle", "Inscription");
        model.addAttribute("user", user);

        // Check if the email is already used
        if (userRepository.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Cet email est déjà utilisé");
            return "formulaireUser";
        }

        // Validate password
        String password = user.getPassword();
        String errorMessage = validatePassword(password);

        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
            return "formulaireUser";
        }
        userRepository.save(user);
        return "redirect:/user/admin";
    }

    /**
     * Validates the password for required strength criteria.
     *
     * @param password the password to validate.
     * @return the error message if validation fails, otherwise null.
     */
    private String validatePassword(String password) {
        if (password.length() < 12) {
            return "Le mot de passe doit contenir au moins 12 caractères";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Le mot de passe doit contenir au moins une lettre majuscule";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Le mot de passe doit contenir au moins une lettre minuscule";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Le mot de passe doit contenir au moins un chiffre";
        }
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            return "Le mot de passe doit contenir au moins un caractère spécial";
        }
        return null;
    }
}
