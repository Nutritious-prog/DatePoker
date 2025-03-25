package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.VoteRequest;
import com.datepoker.dp_backend.DTO.VoteResultResponse;
import com.datepoker.dp_backend.annotations.CurrentUser;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.repositories.VoteRepository;
import com.datepoker.dp_backend.services.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/voting")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/vote")
    public ResponseEntity<VoteResultResponse> voteOnCard(
            @CurrentUser User user,
            @RequestBody VoteRequest voteRequest
    ) {
        VoteResultResponse result = voteService.submitVote(user, voteRequest);
        return ResponseEntity.ok(result);
    }


}
