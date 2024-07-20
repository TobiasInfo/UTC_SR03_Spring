package fr.utc.sr03.ChatSR03Admin.Controller;

import fr.utc.sr03.ChatSR03Admin.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller for handling requests to the "aPropos" page.
 */
@Controller
@RequestMapping("/aPropos")
public class AProposController {

    /**
     * Handles GET requests to the "/aPropos" endpoint.
     * Adds the user's role and ID to the model if the user is logged in.
     *
     * @param req   the web request, used to retrieve session attributes.
     * @param model the model to which attributes are added.
     * @return the name of the view to render, in this case "aPropos".
     */
    @GetMapping
    public String getAPropos(WebRequest req, Model model) {
        String role = getRole(req);
        model.addAttribute("role", role);
        User loggedUser = (User) req.getAttribute("user", WebRequest.SCOPE_SESSION);
        if (loggedUser != null) {
            int id = loggedUser.getIdUser();
            model.addAttribute("idUser", id);
        }
        return "aPropos";
    }

    /**
     * Retrieves the role of the user from the session.
     *
     * @param req the web request, used to retrieve session attributes.
     * @return the role of the user, or "NotAuthenticated" if the role is not set in the session.
     */
    private String getRole(WebRequest req) {
        if (req.getAttribute("role", WebRequest.SCOPE_SESSION) == null) {
            return "NotAuthenticated";
        } else {
            return (String) req.getAttribute("role", WebRequest.SCOPE_SESSION);
        }
    }
}
