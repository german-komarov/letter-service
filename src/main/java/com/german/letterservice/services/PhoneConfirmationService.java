package com.german.letterservice.services;


import com.german.letterservice.entities.PhoneConfirmation;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.PhoneConfirmationException;
import com.german.letterservice.exceptions.PhoneConfirmationNotFoundException;
import com.german.letterservice.repositories.PhoneConfirmationRepository;
import com.german.letterservice.util.Generators;
import com.german.letterservice.util.constants.Responses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@Transactional(rollbackFor = Exception.class)
public class PhoneConfirmationService {


    @Value("${letter-service.phone-number}")
    private String letterServicePhoneNumber;
    @Value("${phone-emulator.send-message-url}")
    private String phoneEmulatorSendMessageUrl;


    private final PhoneConfirmationRepository phoneConfirmationRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public PhoneConfirmationService(PhoneConfirmationRepository phoneConfirmationRepository, RestTemplate restTemplate) {
        this.phoneConfirmationRepository = phoneConfirmationRepository;
        this.restTemplate = restTemplate;
    }


    public PhoneConfirmation retrieveByUsername(String username) throws PhoneConfirmationNotFoundException {
        PhoneConfirmation phoneConfirmation=
                this.phoneConfirmationRepository
                        .findByUsername(username)
                        .orElseThrow(()->new PhoneConfirmationNotFoundException("There is no phone confirmation for user with such username"));

        return phoneConfirmation;
    }


    public PhoneConfirmation save(PhoneConfirmation phoneConfirmationToSave) {
        PhoneConfirmation savedPhoneConfirmation=this.phoneConfirmationRepository.save(phoneConfirmationToSave);

        return savedPhoneConfirmation;
    }


    public PhoneConfirmation create(User principal, String phoneNumber)  {


        PhoneConfirmation phoneConfirmation=this.phoneConfirmationRepository.findByUsername(principal.getUsername()).orElse(null);



        if ( phoneConfirmation==null ) {

            phoneConfirmation = new PhoneConfirmation();

            phoneConfirmation.setUsername(principal.getUsername());

        }


        phoneConfirmation.setPhoneNumber(phoneNumber);
        phoneConfirmation.setConfirmationCode(Generators.generaConfirmationCode());

        phoneConfirmation.setCreatedAt(LocalDateTime.now());
        phoneConfirmation.setAttempts(0);

        phoneConfirmation=this.save(phoneConfirmation);




        return phoneConfirmation;


    }


    public String sendConfirmationCode(String phoneNumber, String confirmationCode) throws PhoneConfirmationException {

        try {
            Long.parseLong(phoneNumber);
        } catch (NumberFormatException e) {
            throw new PhoneConfirmationException("Phone number can contain only digits");
        }


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("from", this.letterServicePhoneNumber);
        body.add("to", phoneNumber);
        body.add("text", String.format("This is your confirmation code %s", confirmationCode));

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body);

        restTemplate.exchange(this.phoneEmulatorSendMessageUrl, HttpMethod.POST,httpEntity,String.class);

        return Responses.CONFIRMATION_CODE_SENT.name();

    }





    public boolean isNotExpired(PhoneConfirmation phoneConfirmation) {
        LocalDateTime creationDateTime=phoneConfirmation.getCreatedAt();

        boolean isNotExpired=LocalDateTime.now().isBefore(creationDateTime.plusMinutes(3));

        return isNotExpired;


    }



    public void delete(PhoneConfirmation phoneConfirmation) {
        this.phoneConfirmationRepository.delete(phoneConfirmation);
    }

}
