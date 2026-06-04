package com.naijavote.controller;

import com.naijavote.entity.Party;
import com.naijavote.entity.User;
import com.naijavote.entity.Vote;
import com.naijavote.service.PartyService;
import com.naijavote.service.UserService;
import com.naijavote.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class VoteController {

    @Autowired
    private PartyService partyService;

    @Autowired
    private UserService userService;

    @Autowired
    private VoteService voteService;

    @GetMapping("")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        List<Party> parties = partyService.getAllParties();
        Optional<Vote> userVote = voteService.getUserVote(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("parties", parties);
        model.addAttribute("userVote", userVote.orElse(null));

        return "vote/dashboard";
    }

    @GetMapping("my-vote")
    public String myVote(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        Optional<Vote> userVote = voteService.getUserVote(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("userVote", userVote.orElse(null));

        return "vote/my-vote";
    }

    @PostMapping("vote/{partyId}")
    public String castVote(@PathVariable Long partyId, Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            voteService.castVote(user.getId(), partyId);
            model.addAttribute("successMessage", "Vote cast successfully!");

            return "redirect:/dashboard";


        } catch (RuntimeException e) {
            // Check if it's the "already voted" error
            if (e.getMessage().contains("already voted")) {
                model.addAttribute("errorMessage", "You have already voted. Your vote cannot be changed.");
            } else {
                model.addAttribute("errorMessage", e.getMessage());
            }
            return "redirect:/dashboard";
        }
    }

    @PostMapping("remove-vote")
    public String removeVote(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            voteService.removeVote(user.getId());
            model.addAttribute("successMessage", "Vote removed successfully!");

            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Your vote has been cast and cannot be removed.");
            return "redirect:/my-vote";
        }
    }
}
