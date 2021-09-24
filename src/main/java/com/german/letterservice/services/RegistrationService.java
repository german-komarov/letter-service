package com.german.letterservice.services;


import com.german.letterservice.dto.RegistrationDto;
import com.german.letterservice.entities.Role;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.RegistrationException;
import com.german.letterservice.exceptions.RoleNotFoundException;
import com.german.letterservice.util.constants.Responses;
import com.german.letterservice.util.validators.RegistrationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class RegistrationService {


    private final RegistrationValidator registrationValidator;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RoleService roleService;


    @Autowired
    public RegistrationService(RegistrationValidator registrationValidator, BCryptPasswordEncoder passwordEncoder, UserService userService, RoleService roleService) {
        this.registrationValidator = registrationValidator;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.roleService = roleService;
    }


    public String createUser(RegistrationDto registrationDto) throws RegistrationException {
        this.registrationValidator.validate(registrationDto);

        String rawUsername=registrationDto.getUsername();

        String username=rawUsername.concat("#lets");


        if( this.userService.doesExist( username ) ) {
            throw new RegistrationException("User with such username already exist");
        }


        String rawPassword=registrationDto.getPassword();

        String password=this.passwordEncoder.encode(rawPassword);

        Role role;
        try {
            role=this.roleService.retrieveByName("ROLE_USER");
        } catch (RoleNotFoundException e) {
            e.printStackTrace();
            throw new RegistrationException("There are some problems with registration on server");
        }
        Set<Role> roles=Collections.singleton(role);


        User userToCreate=new User();

        userToCreate.setUsername(username);
        userToCreate.setPassword(password);
        userToCreate.setRoles(roles);

        userToCreate.setAccountNonExpired(true);
        userToCreate.setAccountNonLocked(true);
        userToCreate.setCredentialsNonExpired(true);
        userToCreate.setEnabled(true);


        this.userService.save(userToCreate);


        return Responses.USER_CREATED.name();
    }
}
