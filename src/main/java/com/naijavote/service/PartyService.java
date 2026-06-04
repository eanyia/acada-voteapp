package com.naijavote.service;

import com.naijavote.dto.PartyRequest;
import com.naijavote.entity.Party;
import com.naijavote.repository.PartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartyService {

    @Autowired
    private PartyRepository partyRepository;

    public Party createParty(PartyRequest request) {
        // Check if abbreviation already exists
        if (partyRepository.findByAbbreviation(request.getAbbreviation()).isPresent()) {
            throw new IllegalArgumentException("Party abbreviation already exists");
        }

        Party party = new Party();
        party.setName(request.getName());
        party.setAbbreviation(request.getAbbreviation());
        party.setDescription(request.getDescription());

        return partyRepository.save(party);
    }

    public List<Party> getAllParties() {
        return partyRepository.findAll();
    }

    public Optional<Party> getPartyById(Long id) {
        return partyRepository.findById(id);
    }

    public void deleteParty(Long id) {
        partyRepository.deleteById(id);
    }

    public void updateParty(Long id, PartyRequest request) {
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Party not found"));

        party.setName(request.getName());
        party.setDescription(request.getDescription());
        // Note: abbreviation should typically be immutable

        partyRepository.save(party);
    }
}
