package com.naijavote.controller;

import com.naijavote.dto.PartyRequest;
import com.naijavote.entity.Party;
import com.naijavote.entity.Vote;
import com.naijavote.service.PartyService;
import com.naijavote.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PartyService partyService;

    @Autowired
    private VoteService voteService;

    @GetMapping("parties")
    public String listParties(Model model) {
        List<Party> parties = partyService.getAllParties();
        model.addAttribute("parties", parties);
        model.addAttribute("partyRequest", new PartyRequest());

        return "admin/parties";
    }

    @PostMapping("parties")
    public String createParty(@ModelAttribute PartyRequest partyRequest, Model model) {
        try {
            partyService.createParty(partyRequest);
            model.addAttribute("successMessage", "Party created successfully!");
            return "redirect:/admin/parties";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("parties", partyService.getAllParties());
            model.addAttribute("partyRequest", partyRequest);
            return "admin/parties";
        }
    }

    @PostMapping("parties/{id}/delete")
    public String deleteParty(@PathVariable Long id, Model model) {
        try {
            partyService.deleteParty(id);
            model.addAttribute("successMessage", "Party deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting party");
        }
        return "redirect:/admin/parties";
    }

    @GetMapping("voters/{partyId}")
    public String viewVoters(@PathVariable Long partyId, Model model) {
        Party party = partyService.getPartyById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));

        List<Vote> votes = voteService.getPartyVotes(partyId);

        model.addAttribute("party", party);
        model.addAttribute("votes", votes);
        model.addAttribute("voteCount", votes.size());

        return "admin/voters";
    }

    @GetMapping("statistics")
    public String statistics(Model model) {
        List<Party> parties = partyService.getAllParties();
        int totalVotes = voteService.getTotalVotes();

        model.addAttribute("parties", parties);
        model.addAttribute("totalVotes", totalVotes);

        return "admin/statistics";
    }
}
