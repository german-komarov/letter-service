package com.german.letterservice.services;


import com.german.letterservice.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {


    private final SessionRegistryImpl sessionRegistryImpl;


    @Autowired
    public SessionService(SessionRegistryImpl sessionRegistryImpl) {
        this.sessionRegistryImpl = sessionRegistryImpl;
    }


    public void expireAllUserSessions(User user) {

        List<SessionInformation> userSessions=this.sessionRegistryImpl.getAllSessions(user, false);

        userSessions.forEach(SessionInformation::expireNow);

    }



}
