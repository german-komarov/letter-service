package com.german.letterservice.controllers;


import com.german.letterservice.dto.*;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.*;
import com.german.letterservice.services.LetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/letters")
public class LetterController {

    private final LetterService letterService;


    @Autowired
    public LetterController(LetterService letterService) {
        this.letterService = letterService;
    }


    @GetMapping("/incoming")
    public Slice<ShowMainLetterDto> lettersIncomingGet (
            @AuthenticationPrincipal User receiver,
            @PageableDefault(size = 25,sort = "receivedAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> incomingLetters=this.letterService.retrieveIncoming(receiver,pageable);
        return incomingLetters;
    }


    @GetMapping("/sent")
    public Slice<ShowMainLetterDto> lettersSentGet(
            @AuthenticationPrincipal User sender,
            @PageableDefault(size = 25,sort = "sentAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> sentLetters=this.letterService.retrieveSent(sender,pageable);
        return sentLetters;
    }



    @GetMapping("/replies")
    public List<ShowReplyLetterDto> lettersRepliesGet(
            @AuthenticationPrincipal User principal,
            @RequestParam(name = "mainLetterUC") String mainLetterUniqueCode) throws RetrievingRepliesException {
        List<ShowReplyLetterDto> replies=this.letterService.retrieveReplies(principal,mainLetterUniqueCode);
        return replies;
    }



    @GetMapping("/incoming/chosen")
    public Slice<ShowMainLetterDto> lettersIncomingChosenGet(
            @AuthenticationPrincipal User receiver,
            @PageableDefault(size = 25,sort = "isChosenByReceiver",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> chosenLetters=this.letterService.retrieveChosenIncoming(receiver,pageable);

        return chosenLetters;
    }



    @GetMapping("/sent/chosen")
    public Slice<ShowMainLetterDto> lettersSentChosenGet(
            @AuthenticationPrincipal User sender,
            @PageableDefault(size = 25,sort = "isChosenBySender",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> chosenLetters=this.letterService.retrieveChosenSent(sender, pageable);

        return chosenLetters;
    }


    @GetMapping("/incoming/spam")
    public Slice<ShowMainLetterDto> letterIncomingSpamGet(
            @AuthenticationPrincipal User receiver,
            @PageableDefault(size = 25,sort = "markedAsSpamAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> spamLetters=this.letterService.retrieveSpam(receiver,pageable);

        return spamLetters;
    }



    @GetMapping("/incoming/archived")
    public Slice<ShowMainLetterDto> lettersIncomingArchivedGet(
            @AuthenticationPrincipal User receiver,
            @PageableDefault(size = 25,sort = "archivedByReceiverAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> archivedLetters=this.letterService.retrieveArchivedIncoming(receiver,pageable);

        return archivedLetters;
    }


    @GetMapping("/sent/archived")
    public Slice<ShowMainLetterDto> lettersSentArchivedGet(
            @AuthenticationPrincipal User sender,
            @PageableDefault(size = 25,sort = "archivedBySenderAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> archivedLetters=this.letterService.retrieveArchivedSent(sender,pageable);

        return archivedLetters;
    }





    @GetMapping("/incoming/deleted")
    public Slice<ShowMainLetterDto> lettersIncomingDeletedGet(
            @AuthenticationPrincipal User receiver,
            @PageableDefault(size = 25,sort = "deletedByReceiverTemporarilyAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> deletedLetters=this.letterService.retrieveDeletedTemporarilyIncoming(receiver,pageable);
        return deletedLetters;
    }



    @GetMapping("/sent/deleted")
    public Slice<ShowMainLetterDto> lettersSentDeletedGet(
            @AuthenticationPrincipal User sender,
            @PageableDefault(size = 25,sort = "deletedBySenderTemporarilyAt",direction = Sort.Direction.DESC) Pageable pageable)
    {
        Slice<ShowMainLetterDto> deletedLetters=this.letterService.retrieveDeletedTemporarilySent(sender,pageable);

        return deletedLetters;
    }



    @PostMapping("/new")
    public String lettersNewPost(@AuthenticationPrincipal User sender, @Valid MainLetterDto mainLetterDto) throws InternalLetterSendingException {
        String responseString=this.letterService.createAndSend(sender, mainLetterDto);
        return responseString;
    }


    @PostMapping("/reply")
    public String lettersReplyPost(@AuthenticationPrincipal User sender, @Valid ReplyLetterDto replyLetterDto) throws InternalLetterSendingException, UserNotFoundException {
        String responseString=this.letterService.replyAndSend(sender, replyLetterDto);
        return responseString;
    }


    @PostMapping("/external")
    public String lettersExternalPost(@Valid ExternalLetterDto externalLetterDto) throws ExternalLetterSendingException {
        String responseString=this.letterService.createAndSendExternal(externalLetterDto);

        return responseString;
    }


    @PutMapping("/incoming/check")
    public String lettersIncomingCheckPut(
            @AuthenticationPrincipal User receiver,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws CheckingException {

        String responseString=this.letterService.markAsChecked(receiver, letterUniqueCode);

        return responseString;

    }



    @PutMapping("/incoming/choose")
    public String lettersIncomingChoosePut(
            @AuthenticationPrincipal User chooser,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws ChoosingException
    {
        String responseString=this.letterService.markAsChosenIncoming(chooser,letterUniqueCode);
        return responseString;
    }



    @PutMapping("/sent/choose")
    public String lettersSentChoosePut(
            @AuthenticationPrincipal User chooser,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws ChoosingException
    {
        String responseString=this.letterService.markAsChosenSent(chooser,letterUniqueCode);
        return responseString;
    }


    @PutMapping("/incoming/unchoose")
    public String lettersIncomingUnchoosePut(
            @AuthenticationPrincipal User unchooser,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws UnchoosingException
    {
        String responseString=this.letterService.unmarkAsChosenIncoming(unchooser, letterUniqueCode);
        return responseString;
    }


    @PutMapping("/sent/unchoose")
    public String lettersSentUnchoosePut(
            @AuthenticationPrincipal User unchooser,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws UnchoosingException
    {
        String responseString=this.letterService.unmarkAsChosenSent(unchooser, letterUniqueCode);
        return responseString;
    }


    @PutMapping("/incoming/spam")
    public String lettersIncomingSpamPut(
            @AuthenticationPrincipal User receiver,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws SpammingException
    {
        String responseString=this.letterService.markAsSpam(receiver,letterUniqueCode);
        return responseString;
    }



    @PutMapping("/incoming/unspam")
    public String letterIncomingUnspamPut(
            @AuthenticationPrincipal User receiver,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws SpammingException
    {
        String responseString=this.letterService.unmarkAsSpam(receiver,letterUniqueCode);
        return responseString;
    }



    @PutMapping("/incoming/archive")
    public String lettersIncomingArchivePut(
            @AuthenticationPrincipal User archiver,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws ArchivingException
    {
        String responseString=this.letterService.markAsArchivedIncoming(archiver,letterUniqueCode);
        return responseString;
    }


    @PutMapping("/sent/archive")
    public String lettersSentArchiveSentPut(
            @AuthenticationPrincipal User archiver,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws ArchivingException
    {
        String responseString=this.letterService.markAsArchivedSent(archiver,letterUniqueCode);
        return responseString;
    }



    @PutMapping("/incoming/unarchive")
    public String lettersIncomingUnarchivePut(
            @AuthenticationPrincipal User unarchiver,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws ArchivingException
    {
        String responseString=this.letterService.unmarkAsArchivedIncoming(unarchiver,letterUniqueCode);
        return responseString;
    }


    @PutMapping("/sent/unarchive")
    public String lettersSentUnarchiveSentPut(
            @AuthenticationPrincipal User unarchiver,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws ArchivingException
    {
        String responseString=this.letterService.unmarkAsArchivedSent(unarchiver,letterUniqueCode);
        return responseString;
    }



    @PutMapping("/incoming/restore")
    public String lettersIncomingRestorePut(
            @AuthenticationPrincipal User restorer,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws LetterRestoringException
    {
        String responseString=this.letterService.restoreIncoming(restorer,letterUniqueCode);
        return responseString;
    }


    @PutMapping("/sent/restore")
    public String lettersSentRestorePut(
            @AuthenticationPrincipal User restorer,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws LetterRestoringException
    {
        String responseString=this.letterService.restoreSent(restorer,letterUniqueCode);
        return responseString;
    }




    @DeleteMapping("/incoming/temporarily")
    public String lettersIncomingTemporarilyDelete(
            @AuthenticationPrincipal User deleter,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws DeletingException
    {
        String responseString=this.letterService.markAsTemporarilyDeletedIncoming(deleter,letterUniqueCode);
        return responseString;
    }


    @DeleteMapping("/sent/temporarily")
    public String lettersSentTemporarilyDelete(
            @AuthenticationPrincipal User deleter,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws DeletingException
    {
        String responseString=this.letterService.markAsTemporarilyDeletedSent(deleter,letterUniqueCode);
        return responseString;
    }



    @DeleteMapping("/incoming/finally")
    public String lettersIncomingFinallyDelete(
            @AuthenticationPrincipal User deleter,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws DeletingException
    {
        String responseString=this.letterService.markAsDeletedFinallyIncoming(deleter,letterUniqueCode);
        return responseString;
    }



    @DeleteMapping("/sent/finally")
    public String lettersSentFinallyDelete(
            @AuthenticationPrincipal User deleter,
            @RequestParam(name = "letterUC") String letterUniqueCode) throws DeletingException
    {
        String responseString=this.letterService.markAsDeletedFinallySent(deleter,letterUniqueCode);
        return responseString;
    }














}
