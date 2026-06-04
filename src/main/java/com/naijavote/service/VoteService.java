package com.naijavote.service;

import com.naijavote.entity.Party;
import com.naijavote.entity.User;
import com.naijavote.entity.Vote;
import com.naijavote.repository.PartyRepository;
import com.naijavote.repository.UserRepository;
import com.naijavote.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Transactional
    public Vote castVote(Long userId, Long partyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));

        // Check if user already has a vote
        Optional<Vote> existingVote = voteRepository.findByUserId(userId);

        if (existingVote.isPresent()) {
            // User has already voted, prevent changing their vote
            throw new RuntimeException("You have already voted. Your vote cannot be changed.");
        }

        // Create new vote
        Vote vote = new Vote(user, party);
        user.setVote(vote);

        return voteRepository.save(vote);
    }

    public Optional<Vote> getUserVote(Long userId) {
        return voteRepository.findByUserId(userId);
    }

    @Transactional
    public void removeVote(Long userId) {
        // Prevent vote removal - once cast, votes cannot be removed
        Optional<Vote> vote = voteRepository.findByUserId(userId);
        if (vote.isPresent()) {
            throw new RuntimeException("Your vote has been cast and cannot be removed.");
        }
    }

    public int getPartyVoteCount(Long partyId) {
        return voteRepository.countByPartyId(partyId);
    }

    public List<Vote> getPartyVotes(Long partyId) {
        return voteRepository.findAll().stream()
                .filter(v -> v.getParty().getId().equals(partyId))
                .toList();
    }

    public int getTotalVotes() {
        return (int) voteRepository.count();
    }
}
