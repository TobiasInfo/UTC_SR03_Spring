package fr.utc.sr03.ChatSR03Admin.Controller;

import fr.utc.sr03.ChatSR03Admin.entity.User;
import fr.utc.sr03.ChatSR03Admin.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;

/**
 * Controller for handling user connection operations.
 */
@Controller
public class ConnexionController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnexionController.class);

    /**
     * Handles GET requests to the home page.
     *
     * @param model the model to add attributes to.
     * @param req   the current web request.
     * @return the name of the view to be rendered.
     */
    @GetMapping("/")
    public String getHome(Model model,
                          WebRequest req) {
        LOGGER.info("=== GET HOME ===");
        model.addAttribute("title", "Page d'accueil");
        if (req.getAttribute("role", WebRequest.SCOPE_SESSION) == null) {
            return "redirect:/connexion";
        } else if (Objects.equals((String) req.getAttribute("role", WebRequest.SCOPE_SESSION), "ADMIN")) {
            return "redirect:/user/admin";
        } else {
            return "redirect:/connexion";
        }
    }

    /**
     * Handles GET requests to the login page.
     *
     * @param model the model to add attributes to.
     * @return the name of the view to be rendered.
     */
    @GetMapping("/connexion")
    public String getLogin(Model model) {
        LOGGER.info("=== GET CONNEXION ===");
        model.addAttribute("title", "Page de connexion");
        model.addAttribute("user", new User());
        return "login";
    }

    /**
     * Handles POST requests for user login.
     *
     * @param user  the user attempting to log in.
     * @param model the model to add attributes to.
     * @param req   the current web request.
     * @return the name of the view to be rendered or a redirect instruction.
     */
    @PostMapping("/connexion")
    public String postLogin(@ModelAttribute User user,
                            Model model,
                            WebRequest req
    ) {
        if (user.getEmail() == null || user.getPassword() == null || user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            model.addAttribute("invalid", true);
            return "login";
        }
        User loggedUser = userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());
        LOGGER.info("=== POST CONNEXION ===");
        LOGGER.info("Email User : " + user.getEmail());
        LOGGER.info("Password User : " + user.getPassword());
        if (loggedUser == null) {
            handleFailedLoginAttempt(user, model);
            return "login";
        }
        return handleSuccessfulLogin(loggedUser, model, req);
    }


    /**
     * Handles a failed login attempt by a user.
     *
     * @param user  the user who attempted to log in.
     * @param model the model to add attributes to.
     */
    private void handleFailedLoginAttempt(User user, Model model) {
        User userByMail = userRepository.findByEmail(user.getEmail());
        if (userByMail != null) {
            userByMail.setLoginAttempts(userByMail.getLoginAttempts() + 1);
            if (userByMail.getLoginAttempts() >= 3) {
                userByMail.setActivated(false);
                model.addAttribute("desactivated", true);
            }
            userRepository.save(userByMail);
        } else {
            LOGGER.info("User not found by email : {}", user.getEmail());
        }
    }

    /**
     * Handles a successful login attempt by a user.
     *
     * @param loggedUser the successfully logged-in user.
     * @param model      the model to add attributes to.
     * @param req        the current web request.
     * @return the name of the view to be rendered or a redirect instruction.
     */
    private String handleSuccessfulLogin(User loggedUser, Model model, WebRequest req) {
        if (!loggedUser.isActivated()) {
            model.addAttribute("askAdmin", true);
            return "login";
        }
        loggedUser.setLoginAttempts(0);
        userRepository.save(loggedUser);
        req.setAttribute("user", loggedUser, WebRequest.SCOPE_SESSION);
        if (loggedUser.isAdmin()) {
            LOGGER.info("J'ai bien été identifié avec les droits admin");
            req.setAttribute("role", "ADMIN", WebRequest.SCOPE_SESSION);
            return "redirect:/user/admin";
        } else {
            LOGGER.info("J'ai bien été identifié avec les droits utilisateur");
            req.setAttribute("role", "USER", WebRequest.SCOPE_SESSION);
            model.addAttribute("isUser", true);
            return "login";
        }
    }
}
