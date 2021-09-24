package com.german.letterservice.services;


import com.german.letterservice.dto.*;
import com.german.letterservice.entities.Attachment;
import com.german.letterservice.entities.Letter;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.*;
import com.german.letterservice.repositories.LetterRepository;
import com.german.letterservice.util.constants.ExceptionMessageTemplates;
import com.german.letterservice.util.constants.Responses;
import com.german.letterservice.util.validators.AttachedFilesValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserService userService;
    private final AttachedFilesValidator attachedFilesValidator;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public LetterService(LetterRepository letterRepository, UserService userService, AttachedFilesValidator attachedFilesValidator, SimpMessagingTemplate simpMessagingTemplate) {
        this.letterRepository = letterRepository;
        this.userService = userService;
        this.attachedFilesValidator = attachedFilesValidator;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }




    public LocalDateTime now() {
        return LocalDateTime.now();
    }




    public Letter retrieveByUniqueCode(String letterUniqueCode) throws LetterNotFoundException {
        Letter letter=this.letterRepository
                .findByUniqueCode(letterUniqueCode)
                .orElseThrow(()->new LetterNotFoundException("Letter with such unique code does not exist"));

        return letter;
    }



    public Letter save(Letter letterToSave) {
        Letter savedLetter=this.letterRepository.save(letterToSave);
        return savedLetter;
    }



    public Slice<ShowMainLetterDto> retrieveIncoming(User receiver, Pageable pageable) {
        Slice<Letter> incomingLetters=this.letterRepository.findIncoming(receiver.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=incomingLetters.map(ShowMainLetterDto::new);

        return showLetters;
    }


    public Slice<ShowMainLetterDto> retrieveSent(User sender, Pageable pageable) {
        Slice<Letter> sentLetters=this.letterRepository.findSent(sender.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=sentLetters.map(ShowMainLetterDto::new);

        return showLetters;
    }



    public List<ShowReplyLetterDto> retrieveReplies(User principal, String mainLetterUniqueCode) throws RetrievingRepliesException {

        Letter mainLetter;

        try {
            mainLetter=this.retrieveByUniqueCode(mainLetterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new RetrievingRepliesException(e.getMessage());
        }

        boolean isPrincipalSender=mainLetter.getSenderUsername().equals(principal.getUsername());
        boolean isPrincipalReceiver=mainLetter.getReceiverUsername().equals(principal.getUsername());

        if ( ! ( isPrincipalSender || isPrincipalReceiver ) )
        {
            throw new RetrievingRepliesException("You are neither sender, nor receiver of the main letter");
        }

        List<Letter> replies=mainLetter.getReplies();
        Stream<Letter> repliesStream=replies.stream();

        repliesStream=repliesStream.filter((letter)->{

            if ( letter.getSenderUsername().equals(principal.getUsername()) ) {
                return ! ( letter.isArchivedBySender() || letter.isDeletedBySenderTemporarily() || letter.isDeletedBySenderFinally() );
            }

            else {
                return ! ( letter.isArchivedByReceiver() || letter.isDeletedByReceiverTemporarily() || letter.isDeletedByReceiverFinally() );
            }

        });


        List<ShowReplyLetterDto> showLetters=repliesStream.map(ShowReplyLetterDto::new).collect(Collectors.toList());

        return showLetters;

    }



    public Slice<ShowMainLetterDto> retrieveChosenIncoming(User receiver, Pageable pageable) {
        Slice<Letter> chosenLetters=this.letterRepository.findChosenIncoming(receiver.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=chosenLetters.map(ShowMainLetterDto::new);

        return showLetters;
    }


    public Slice<ShowMainLetterDto> retrieveChosenSent(User sender, Pageable pageable) {
        Slice<Letter> chosenLetters=this.letterRepository.findChosenSent(sender.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=chosenLetters.map(ShowMainLetterDto::new);

        return showLetters;

    }



    public Slice<ShowMainLetterDto> retrieveSpam(User receiver, Pageable pageable) {
        Slice<Letter> spamLetters=this.letterRepository.findSpam(receiver.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=spamLetters.map(ShowMainLetterDto::new);

        return showLetters;
    }


    public Slice<ShowMainLetterDto> retrieveArchivedIncoming(User receiver, Pageable pageable) {
        Slice<Letter> archivedLetters=this.letterRepository.findArchivedIncoming(receiver.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=archivedLetters.map(ShowMainLetterDto::new);

        return showLetters;
    }



    public Slice<ShowMainLetterDto> retrieveArchivedSent(User sender, Pageable pageable) {
        Slice<Letter> archivedLetters=this.letterRepository.findArchivedSent(sender.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=archivedLetters.map(ShowMainLetterDto::new);

        return showLetters;

    }


    public Slice<ShowMainLetterDto> retrieveDeletedTemporarilyIncoming(User receiver, Pageable pageable) {
        Slice<Letter> deletedLetters=this.letterRepository.findDeletedTemporarilyIncoming(receiver.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=deletedLetters.map(ShowMainLetterDto::new);

        return showLetters;
    }



    public Slice<ShowMainLetterDto> retrieveDeletedTemporarilySent(User sender, Pageable pageable) {
        Slice<Letter> deletedLetters=this.letterRepository.findDeletedTemporarilySent(sender.getUsername(),pageable);
        Slice<ShowMainLetterDto> showLetters=deletedLetters.map(ShowMainLetterDto::new);

        return showLetters;
    }




    public String createAndSend(User sender, MainLetterDto mainLetterDto) throws InternalLetterSendingException {

        this.attachedFilesValidator.validate(mainLetterDto.getAttachments());


        User receiver;

        try {
            receiver=this.userService.retrieveByUsername(mainLetterDto.getReceiverUsername());
        } catch (UserNotFoundException e) {
            throw new InternalLetterSendingException("Receiver with such username does not exist");
        }

        if( this.userService.isContactBlockedByUser(receiver.getId(),sender.getId()) ) {
            throw new InternalLetterSendingException("You are in the blacklist of this receiver");
        }

        if ( this.userService.isContactBlockedByUser(sender.getId(),receiver.getId()) ) {
            throw new InternalLetterSendingException("This receiver is in the your blacklist");
        }


        String letterUniqueCode= mainLetterDto.getLetterUniqueCode();
        String senderUsername=sender.getUsername();
        String receiverUsername=receiver.getUsername();

        String subject=mainLetterDto.getSubject();

        if ( subject!=null ) {
            subject=subject.trim();
            subject = subject.length()<101 ? subject : subject.substring(0,101);
        }

        String text=mainLetterDto.getText();

        if ( text!=null ) {
            text=text.trim();
            text = text.length()<10001 ? text : text.substring(0,10001);
        }



        LocalDateTime sentAt= mainLetterDto.getSentAt();




        Letter letter = new Letter();

        letter.setUniqueCode(letterUniqueCode);
        letter.setSenderUsername(senderUsername);
        letter.setReceiverUsername(receiverUsername);
        letter.setSubject(subject);
        letter.setText(text);

        this.fillWithAttachments(letter, mainLetterDto.getAttachments());

        letter.setSentAt(sentAt);


        if ( this.userService.howManySpammingCases(receiver.getId(),sender.getId()) == receiver.getMaximumAllowedSpammingCases() ) {
            letter.setSpam(true);
            letter.setMarkedAsSpamAt(now());

            this.save(letter);

            return Responses.LETTER_SAVED_AS_SPAM.name();
        }



        letter=this.save(letter);


        this.sendNew(receiver, letter);


        return Responses.LETTER_SAVED_AND_SENT.name();
    }








    public String replyAndSend(User replier, ReplyLetterDto replyLetterDto) throws InternalLetterSendingException {

        this.attachedFilesValidator.validate(replyLetterDto.getAttachments());

        Letter letterToReply;

        try {
            letterToReply=this.retrieveByUniqueCode(replyLetterDto.getMainLetterUniqueCode());
        } catch (LetterNotFoundException e) {
            throw new InternalLetterSendingException(e.getMessage());
        }


        if ( letterToReply.isReply() ) {
            throw new InternalLetterSendingException("Main letter cannot be reply of others");
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;
        boolean isReplierSender=letterToReply.getSenderUsername().equals(replier.getUsername());
        boolean isReplierReceiver=letterToReply.getReceiverUsername().equals(replier.getUsername());

        if ( ! ( isReplierSender || isReplierReceiver))
        {
            throw new InternalLetterSendingException(String.format("%s you are neither sender, nor receiver of the main letter",noAccess));
        }


        if( isReplierSender ) {
            if ( userService.isContactBlockedByUser(letterToReply.getReceiverUsername(), replier.getUsername()) ) {
                throw new InternalLetterSendingException("You are int the blacklist of the main letter receiver");
            }

            if ( userService.isContactBlockedByUser(replier.getUsername(),letterToReply.getReceiverUsername()) ) {
                throw new InternalLetterSendingException("The receiver of the main letter is in the your blacklist");
            }

        }

        if ( isReplierReceiver ) {
            if ( userService.isContactBlockedByUser(letterToReply.getSenderUsername(),replier.getUsername()) ) {
                throw new InternalLetterSendingException("You are in the blacklist of the main letter sender");
            }

            if ( userService.isContactBlockedByUser(replier.getUsername(),letterToReply.getSenderUsername()) ) {
                throw new InternalLetterSendingException("There sender of the main letter is in the your blacklist");
            }

        }


        Letter letterToCreate=new Letter();

        letterToCreate.setUniqueCode(replyLetterDto.getLetterUniqueCode());
        letterToCreate.setSenderUsername(replier.getUsername());

        if (  isReplierSender  ){
            letterToCreate.setReceiverUsername(letterToReply.getReceiverUsername());
        } else {
            letterToCreate.setReceiverUsername(letterToReply.getSenderUsername());
        }

        letterToCreate.setSubject(letterToReply.getSubject());


        String text=replyLetterDto.getText();

        if ( text!=null ) {
            text=text.trim();
            text = text.length()<10001 ? text : text.substring(0,10001);
        }

        letterToCreate.setText(text);

        this.fillWithAttachments(letterToCreate, replyLetterDto.getAttachments());

        letterToCreate.setSentAt(replyLetterDto.getSentAt());
        letterToCreate.setReply(true);
        letterToCreate.setMainLetterUniqueCode(letterToReply.getUniqueCode());


        letterToCreate=this.save(letterToCreate);


        this.insertReply(letterToReply.getId(),letterToCreate.getId());


        this.sendReply(letterToCreate);



        return Responses.LETTER_SAVED_AND_SENT.name();


    }





    public String createAndSendExternal(ExternalLetterDto externalLetterDto) throws ExternalLetterSendingException {

        User sender;

        try {
            sender=this.userService.checkCredentialsAndReturnUser(externalLetterDto.getSenderUsername(),externalLetterDto.getSenderPassword());
        } catch (WrongCredentialsException e) {
            throw new ExternalLetterSendingException(e.getMessage());
        }


        MainLetterDto mainLetterDto=new MainLetterDto(externalLetterDto);



        String responseString;

        try {
            responseString=this.createAndSend(sender,mainLetterDto);
        } catch (InternalLetterSendingException e) {
            throw new ExternalLetterSendingException(e.getMessage());
        }


        return responseString;


    }


    private void fillWithAttachments(Letter letter, List<MultipartFile> files) throws InternalLetterSendingException {
        for(MultipartFile file : files) {
            if(file.getOriginalFilename()==null ||  file.getOriginalFilename().trim().equals("")){
                continue;
            }

            try {
                letter.addAttachment(new Attachment(file,letter.getUniqueCode()));
            } catch (ConversionException e) {
                e.printStackTrace();
                throw new InternalLetterSendingException("There are some problems with processing one of your attached files in the server");
            }
        }
    }


    private void insertReply(Long mainLetterId,Long replyLetterId) {
        this.letterRepository.insertReply(mainLetterId,replyLetterId);
    }



    private void sendNew(User receiver, Letter letterToSend) throws InternalLetterSendingException {
        ShowMainLetterDto showLetter=new ShowMainLetterDto(letterToSend);
        try {
            this.simpMessagingTemplate.convertAndSendToUser(receiver.getUsername(),"/topic/new",showLetter);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new InternalLetterSendingException("Exception was raised during sending the letter");
        }
    }



    private void sendReply(Letter letterToSend) throws InternalLetterSendingException {
        ShowReplyLetterDto showLetter=new ShowReplyLetterDto(letterToSend);
        try {
            this.simpMessagingTemplate.convertAndSendToUser(letterToSend.getReceiverUsername(),"/topic/reply",showLetter);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new InternalLetterSendingException("Exception was raised during sending the letter");
        }

    }







    public String markAsChecked(User receiver, String letterUniqueCode) throws CheckingException {

        Letter letterToCheck;


        try {
            letterToCheck=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new CheckingException(e.getMessage());
        }

        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;


        if ( ! letterToCheck.getReceiverUsername().equals(receiver.getUsername()) ) {

            throw new CheckingException(String.format("%s you are not receiver of this letter",noAccess));

        }



        if ( letterToCheck.isChecked() ) {
            throw new CheckingException(String.format("%s this letter is already marked as checked",noAccess));
        }


        if ( letterToCheck.isDeletedByReceiverFinally() ) {
            throw new CheckingException(String.format("%s this letter is finally deleted",noAccess));
        }



        letterToCheck.setChecked(true);

        this.save(letterToCheck);



        return Responses.LETTER_MARKED_AS_CHECKED.name();

    }





    public String markAsChosenIncoming(User chooser, String letterUniqueCode) throws ChoosingException {

        Letter letterToChoose;

        try {
            letterToChoose=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new ChoosingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToChoose.getReceiverUsername().equals(chooser.getUsername()) ) {
            throw new ChoosingException(String.format("%s you are not receiver of this letter",noAccess));
        }


        if ( letterToChoose.isArchivedByReceiver() ) {
            throw new ChoosingException(String.format("%s this letter is archived",noAccess));
        }


        if ( letterToChoose.isDeletedByReceiverTemporarily() ) {
            throw new ChoosingException(String.format("%s this letter is temporarily deleted",noAccess));
        }


        if ( letterToChoose.isDeletedByReceiverFinally() ) {
            throw new ChoosingException(String.format("%s this letter is finally deleted",noAccess));
        }


        letterToChoose.setChosenByReceiver(true);
        letterToChoose.setChosenByReceiverAt(now());

        this.save(letterToChoose);



        return Responses.INCOMING_LETTER_CHOSEN.name();

    }



    public String markAsChosenSent(User chooser, String letterUniqueCode) throws ChoosingException {

        Letter letterToChoose;

        try {
            letterToChoose=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new ChoosingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToChoose.getSenderUsername().equals(chooser.getUsername()) ) {
            throw new ChoosingException(String.format("%s you are not sender of this letter",noAccess));
        }


        if ( letterToChoose.isArchivedBySender() ) {
            throw new ChoosingException(String.format("%s this letter is archived",noAccess));
        }


        if ( letterToChoose.isDeletedBySenderTemporarily() ) {
            throw new ChoosingException(String.format("%s this letter is temporarily deleted",noAccess));
        }


        if ( letterToChoose.isDeletedBySenderFinally() ) {
            throw new ChoosingException(String.format("%s this letter is finally deleted",noAccess));
        }


        letterToChoose.setChosenBySender(true);
        letterToChoose.setChosenBySenderAt(now());

        this.save(letterToChoose);



        return Responses.SENT_LETTER_CHOSEN.name();

    }



    public String unmarkAsChosenIncoming(User unchooser, String letterUniqueCode) throws UnchoosingException {

        Letter letterToUnchoose;

        try {
            letterToUnchoose=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new UnchoosingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToUnchoose.getReceiverUsername().equals(unchooser.getUsername()) ) {
            throw new UnchoosingException(String.format("%s you are not receiver of this letter",noAccess));
        }


        if ( ! letterToUnchoose.isChosenByReceiver() ) {
            throw new UnchoosingException(String.format("%s letter is not chosen",noAccess));
        }



        if ( letterToUnchoose.isArchivedByReceiver() ) {
            throw new UnchoosingException(String.format("%s this letter is archived",noAccess));
        }

        if ( letterToUnchoose.isDeletedByReceiverTemporarily() ) {
            throw new UnchoosingException(String.format("%s this letter is temporarily deleted",noAccess));
        }


        if ( letterToUnchoose.isDeletedByReceiverFinally() ) {
            throw new UnchoosingException(String.format("%s this letter is finally deleted",noAccess));
        }



        letterToUnchoose.setChosenByReceiver(false);
        letterToUnchoose.setChosenByReceiverAt(null);



        this.save(letterToUnchoose);



        return Responses.INCOMING_LETTER_UNCHOSEN.name();

    }


    public String unmarkAsChosenSent(User unchooser, String letterUniqueCode) throws UnchoosingException {
        Letter letterToUnchoose;

        try {
            letterToUnchoose=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new UnchoosingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToUnchoose.getSenderUsername().equals(unchooser.getUsername()) ) {
            throw new UnchoosingException(String.format("%s you are not sender of this letter",noAccess));
        }


        if ( ! letterToUnchoose.isChosenBySender() ) {
            throw new UnchoosingException(String.format("%s letter is not chosen",noAccess));
        }



        if ( letterToUnchoose.isArchivedBySender() ) {
            throw new UnchoosingException(String.format("%s this letter is archived",noAccess));
        }

        if ( letterToUnchoose.isDeletedBySenderTemporarily() ) {
            throw new UnchoosingException(String.format("%s this letter is temporarily deleted",noAccess));
        }


        if ( letterToUnchoose.isDeletedBySenderFinally() ) {
            throw new UnchoosingException(String.format("%s this letter is finally deleted",noAccess));
        }



        letterToUnchoose.setChosenBySender(false);
        letterToUnchoose.setChosenBySenderAt(null);



        this.save(letterToUnchoose);



        return Responses.SENT_LETTER_UNCHOSEN.name();
    }







    public String markAsSpam(User receiver, String letterUniqueCode) throws SpammingException {
        Letter spamLetter;


        try {
            spamLetter=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e){
            throw new SpammingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! spamLetter.getReceiverUsername().equals(receiver.getUsername()) ) {
            throw new SpammingException(String.format("%s you are not receiver of this letter",noAccess));
        }



        if ( spamLetter.isSpam() ) {
            throw new SpammingException(String.format("%s this letter is already marked as spam",noAccess));
        }

        if ( spamLetter.isReply() ) {
            throw new SpammingException("Reply letter cannot be spam, only main letter can");
        }

        if ( spamLetter.isDeletedByReceiverFinally() ) {
            throw new SpammingException(String.format("%s this letter is finally deleted",noAccess));
        }


        spamLetter.setSpam(true);
        spamLetter.setMarkedAsSpamAt(now());

        try {
            this.userService.addSpammingCase(receiver, spamLetter.getSenderUsername());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            throw new SpammingException("Something went wrong in the server");
        }


        return Responses.LETTER_MARKED_AS_SPAM.name();



    }


    public String unmarkAsSpam(User receiver, String letterUniqueCode) throws SpammingException {
        Letter notSpamLetter;

        try {
            notSpamLetter=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new SpammingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;


        if ( ! notSpamLetter.getReceiverUsername().equals(receiver.getUsername()) ) {
            throw new SpammingException(String.format("%s you are not receiver of this letter", noAccess));
        }



        if ( ! notSpamLetter.isSpam() ) {
            throw new SpammingException(String.format("%s this letter is not marked as spam", noAccess));
        }


        if ( notSpamLetter.isReply() ) {
            throw new SpammingException("Reply letter cannot be spam, only main letter can");
        }


        if ( notSpamLetter.isDeletedByReceiverFinally() ) {
            throw new SpammingException(String.format("%s this letter is finally deleted", noAccess));
        }



        notSpamLetter.setSpam(false);
        notSpamLetter.setMarkedAsSpamAt(null);

        try {
            Integer spamLetterNumber=this.userService.removeSpammingCase(receiver, notSpamLetter.getSenderUsername());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            throw new SpammingException("Something went wrong in the server");
        }


        return Responses.LETTER_MARKED_AS_NOT_SPAM.name();


    }




    public String markAsArchivedIncoming(User archiver, String letterUniqueCode) throws ArchivingException {


        Letter letterToArchive;

        try {
            letterToArchive=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new ArchivingException(e.getMessage());
        }

        String noAccess= ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToArchive.getReceiverUsername().equals(archiver.getUsername()) ) {
            throw new ArchivingException(String.format("%s you are not receiver of this letter", noAccess));
        }

        if ( letterToArchive.isArchivedByReceiver() ) {
            throw new ArchivingException(String.format("%s letter is already archived", noAccess));
        }

        if ( letterToArchive.isDeletedByReceiverTemporarily() ) {
            throw new ArchivingException(String.format("%s letter is temporarily deleted", noAccess));
        }


        if ( letterToArchive.isDeletedByReceiverFinally() ) {
            throw new ArchivingException(String.format("%s this letter is finally deleted", noAccess));
        }


        letterToArchive.setArchivedByReceiver(true);
        letterToArchive.setArchivedByReceiverAt(now());

        this.save(letterToArchive);


        return Responses.INCOMING_LETTER_ARCHIVED.name();

    }


    public String markAsArchivedSent(User archiver, String letterUniqueCode) throws ArchivingException {
        Letter letterToArchive;

        try {
            letterToArchive=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new ArchivingException(e.getMessage());
        }

        String noAccess= ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;


        if ( ! letterToArchive.getSenderUsername().equals(archiver.getUsername()) ) {
            throw new ArchivingException(String.format("%s you are not sender of this letter",noAccess));
        }


        if ( letterToArchive.isArchivedBySender() ) {
            throw new ArchivingException(String.format("%s letter is already archived", noAccess));
        }

        if ( letterToArchive.isDeletedBySenderTemporarily() ) {
            throw new ArchivingException(String.format("%s letter is temporarily deleted", noAccess));
        }


        if ( letterToArchive.isDeletedBySenderFinally() ) {
            throw new ArchivingException(String.format("%s this letter is finally deleted", noAccess));
        }


        letterToArchive.setArchivedBySender(true);
        letterToArchive.setArchivedBySenderAt(now());

        this.save(letterToArchive);


        return Responses.SENT_LETTER_ARCHIVED.name();

    }







    public String unmarkAsArchivedIncoming(User unarchiver, String letterUniqueCode) throws ArchivingException {
        Letter letterToUnarchive;


        try {
            letterToUnarchive=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new ArchivingException(e.getMessage());
        }

        String noAccess= ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToUnarchive.getReceiverUsername().equals(unarchiver.getUsername()) ) {
            throw new ArchivingException(String.format("%s you are not receiver of this letter", noAccess));
        }


        if ( ! letterToUnarchive.isArchivedByReceiver() ) {
            throw new ArchivingException(String.format("%s this letter is not archived", noAccess));
        }


        if ( letterToUnarchive.isDeletedByReceiverTemporarily() ) {
            throw new ArchivingException(String.format("%s letter is temporarily deleted", noAccess));
        }


        if ( letterToUnarchive.isDeletedByReceiverFinally() ) {
            throw new ArchivingException(String.format("%s this letter is finally deleted", noAccess));
        }



        letterToUnarchive.setArchivedByReceiver(false);
        letterToUnarchive.setArchivedByReceiverAt(null);


        this.save(letterToUnarchive);



        return Responses.INCOMING_LETTER_UNARCHIVED.name();

    }


    public String unmarkAsArchivedSent(User unarchiver, String letterUniqueCode) throws ArchivingException {
        Letter letterToUnarchive;


        try {
            letterToUnarchive=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new ArchivingException(e.getMessage());
        }

        String noAccess= ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToUnarchive.getSenderUsername().equals(unarchiver.getUsername()) ) {
            throw new ArchivingException(String.format("%s you are not sender of this letter", noAccess));
        }


        if ( ! letterToUnarchive.isArchivedBySender() ) {
            throw new ArchivingException(String.format("%s this letter is not archived", noAccess));
        }


        if ( letterToUnarchive.isDeletedBySenderTemporarily() ) {
            throw new ArchivingException(String.format("%s letter is temporarily deleted", noAccess));
        }


        if ( letterToUnarchive.isDeletedBySenderFinally() ) {
            throw new ArchivingException(String.format("%s this letter is finally deleted", noAccess));
        }



        letterToUnarchive.setArchivedBySender(false);
        letterToUnarchive.setArchivedBySenderAt(null);


        this.save(letterToUnarchive);



        return Responses.SENT_LETTER_UNARCHIVED.name();
    }







    public String restoreIncoming(User restorer, String letterUniqueCode) throws LetterRestoringException {

        Letter letterToRestore;


        try {
            letterToRestore=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new LetterRestoringException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToRestore.getReceiverUsername().equals(restorer.getUsername()) ) {
            throw new LetterRestoringException(String.format("%s you are not receiver of this letter",noAccess));
        }



        if ( ! letterToRestore.isDeletedByReceiverTemporarily() ) {
            throw new LetterRestoringException(String.format("%s letter is not temporarily deleted",noAccess));
        }


        if ( letterToRestore.isDeletedByReceiverFinally() ) {
            throw new LetterRestoringException(String.format("%s letter is finally deleted",noAccess));
        }



        letterToRestore.setDeletedByReceiverTemporarily(false);
        letterToRestore.setDeletedByReceiverTemporarilyAt(null);

        this.save(letterToRestore);


        return Responses.INCOMING_LETTER_RESTORED.name();

    }

    public String restoreSent(User restorer, String letterUniqueCode) throws LetterRestoringException {
        Letter letterToRestore;


        try {
            letterToRestore=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new LetterRestoringException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToRestore.getSenderUsername().equals(restorer.getUsername()) ) {
            throw new LetterRestoringException(String.format("%s you are not sender of this letter",noAccess));
        }



        if ( ! letterToRestore.isDeletedBySenderTemporarily() ) {
            throw new LetterRestoringException(String.format("%s letter is not temporarily deleted",noAccess));
        }


        if ( letterToRestore.isDeletedBySenderFinally() ) {
            throw new LetterRestoringException(String.format("%s letter is finally deleted",noAccess));
        }



        letterToRestore.setDeletedBySenderTemporarily(false);
        letterToRestore.setDeletedBySenderTemporarilyAt(null);

        this.save(letterToRestore);


        return Responses.SENT_LETTER_RESTORED.name();
    }




    public String markAsTemporarilyDeletedIncoming(User deleter, String letterUniqueCode) throws DeletingException {
        Letter letterToDelete;

        try {
            letterToDelete=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new DeletingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToDelete.getReceiverUsername().equals(deleter.getUsername()) ) {
            throw new DeletingException(String.format("%s you are not receiver of this letter",noAccess));
        }


        if ( letterToDelete.isDeletedByReceiverTemporarily() ) {
            throw new DeletingException(String.format("%s letter is already temporarily deleted", noAccess));
        }

        if ( letterToDelete.isDeletedByReceiverFinally() ) {
            throw new DeletingException(String.format("%s letter is finally deleted",noAccess));
        }


        letterToDelete.setDeletedByReceiverTemporarily(true);
        letterToDelete.setDeletedByReceiverTemporarilyAt(now());

        this.save(letterToDelete);



        return Responses.INCOMING_LETTER_DELETED_TEMPORARILY.name();


    }


    public String markAsTemporarilyDeletedSent(User deleter, String letterUniqueCode) throws DeletingException {
        Letter letterToDelete;

        try {
            letterToDelete=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new DeletingException(e.getMessage());
        }


        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToDelete.getSenderUsername().equals(deleter.getUsername()) ) {
            throw new DeletingException(String.format("%s you are not sender of this letter",noAccess));
        }


        if ( letterToDelete.isDeletedBySenderTemporarily() ) {
            throw new DeletingException(String.format("%s letter is already temporarily deleted", noAccess));
        }

        if ( letterToDelete.isDeletedBySenderFinally() ) {
            throw new DeletingException(String.format("%s letter is finally deleted",noAccess));
        }


        letterToDelete.setDeletedBySenderTemporarily(true);
        letterToDelete.setDeletedBySenderTemporarilyAt(now());

        this.save(letterToDelete);



        return Responses.SENT_LETTER_DELETED_TEMPORARILY.name();
    }




    public String markAsDeletedFinallyIncoming(User deleter, String letterUniqueCode) throws DeletingException {
        Letter letterToDelete;

        try {
            letterToDelete=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new DeletingException(e.getMessage());
        }



        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToDelete.getReceiverUsername().equals(deleter.getUsername()) ) {
            throw new DeletingException(String.format("%s you are not receiver of this letter",noAccess));
        }


        if ( ! letterToDelete.isDeletedByReceiverTemporarily() ) {
            throw new DeletingException(String.format("%s letter is not temporarily deleted yet",noAccess));
        }


        if ( letterToDelete.isDeletedByReceiverFinally() ) {
            throw new DeletingException(String.format("%s letter is already finally deleted",noAccess));
        }



        letterToDelete.setChosenByReceiver(false);
        letterToDelete.setChosenByReceiverAt(null);

        letterToDelete.setArchivedByReceiver(false);
        letterToDelete.setArchivedByReceiverAt(null);

        letterToDelete.setDeletedByReceiverTemporarily(false);
        letterToDelete.setDeletedByReceiverTemporarilyAt(null);


        letterToDelete.setDeletedByReceiverFinally(true);
        letterToDelete.setDeletedByReceiverFinallyAt(now());


        this.save(letterToDelete);


        return Responses.INCOMING_LETTER_DELETED_FINALLY.name();
    }

    public String markAsDeletedFinallySent(User deleter, String letterUniqueCode) throws DeletingException {
        Letter letterToDelete;

        try {
            letterToDelete=this.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new DeletingException(e.getMessage());
        }

        String noAccess=ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( ! letterToDelete.getSenderUsername().equals(deleter.getUsername()) ) {
            throw new DeletingException(String.format("%s you are not sender of this letter",noAccess));
        }


        if ( ! letterToDelete.isDeletedBySenderTemporarily() ) {
            throw new DeletingException(String.format("%s letter is not temporarily deleted yet",noAccess));
        }


        if ( letterToDelete.isDeletedBySenderFinally() ) {
            throw new DeletingException(String.format("%s letter is already finally deleted",noAccess));
        }



        letterToDelete.setChosenBySender(false);
        letterToDelete.setChosenBySenderAt(null);

        letterToDelete.setArchivedBySender(false);
        letterToDelete.setArchivedBySenderAt(null);

        letterToDelete.setDeletedBySenderTemporarily(false);
        letterToDelete.setDeletedBySenderTemporarilyAt(null);


        letterToDelete.setDeletedBySenderFinally(true);
        letterToDelete.setDeletedBySenderFinallyAt(now());


        this.save(letterToDelete);


        return Responses.SENT_LETTER_DELETED_FINALLY.name();
    }


}
