package com.german.letterservice.controllers;

import com.german.letterservice.dto.DraftDto;
import com.german.letterservice.dto.ShowDraftDto;
import com.german.letterservice.entities.User;
import com.german.letterservice.services.DraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drafts")
public class DraftController {


    private final DraftService draftService;


    @Autowired
    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }



    @GetMapping
    public Slice<ShowDraftDto> draftsGet(
            @AuthenticationPrincipal User sender,
            @PageableDefault(size = 25,sort = "leftAsDraftAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowDraftDto> showDrafts=this.draftService.retrieve(sender,pageable);

        return showDrafts;
    }





    @PostMapping("/save")
    public String draftsSavePost(@AuthenticationPrincipal User sender, DraftDto draftDto) {

        String responseString=this.draftService.create(sender,draftDto);

        return responseString;

    }





}
