package com.german.letterservice.services;


import com.german.letterservice.dto.DraftDto;
import com.german.letterservice.dto.ShowDraftDto;
import com.german.letterservice.entities.Draft;
import com.german.letterservice.entities.User;
import com.german.letterservice.repositories.DraftRepository;
import com.german.letterservice.util.constants.Responses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class DraftService {

    private final DraftRepository draftRepository;


    @Autowired
    public DraftService(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }



    public Slice<ShowDraftDto> retrieve(User sender, Pageable pageable) {
        Slice<Draft> drafts=this.draftRepository.findBySenderUsername(sender.getUsername(),pageable);
        Slice<ShowDraftDto> showDrafts=drafts.map(ShowDraftDto::new);

        return showDrafts;
    }



    public Draft save(Draft draftToSave) {
        Draft savedDraft=this.draftRepository.save(draftToSave);

        return savedDraft;
    }



    public String create(User sender, DraftDto draftDto) {


        String receiverUsername=draftDto.getReceiverUsername();

        if ( receiverUsername!=null ) {
            receiverUsername=receiverUsername.trim();
            receiverUsername = receiverUsername.length() < 86 ? receiverUsername : receiverUsername.substring(0,86);
        }


        String subject=draftDto.getSubject();

        if ( subject!=null ) {
            subject=subject.trim();
            subject = subject.length() < 101 ? subject : subject.substring(0,101);
        }


        String text=draftDto.getText();


        if ( text!=null ) {
            text=text.trim();
            text = text.length()<10001 ? text : text.substring(0,10001);
        }

        boolean isReply=false;
        String mainLetterUniqueCode=draftDto.getMainLetterUniqueCode();


        if ( mainLetterUniqueCode!=null ) {
            mainLetterUniqueCode=mainLetterUniqueCode.trim();

            mainLetterUniqueCode = mainLetterUniqueCode.length()<51 ? mainLetterUniqueCode : mainLetterUniqueCode.substring(0,51);

            isReply = ! mainLetterUniqueCode.equals("");

        }

        Draft draft=new Draft();

        draft.setUniqueCode(draftDto.getDraftUniqueCode());
        draft.setSenderUsername(sender.getUsername());
        draft.setReceiverUsername(receiverUsername);
        draft.setSubject(subject);
        draft.setText(text);
        draft.setLeftAsDraftAt(draftDto.getLeftAsDraftAt());
        draft.setReply(isReply);
        draft.setMainLetterUniqueCode(mainLetterUniqueCode);



        this.save(draft);


        return Responses.DRAFT_SAVED.name();

    }





}
