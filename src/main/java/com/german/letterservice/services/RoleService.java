package com.german.letterservice.services;


import com.german.letterservice.entities.Role;
import com.german.letterservice.exceptions.RoleNotFoundException;
import com.german.letterservice.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService {

    private final RoleRepository roleRepository;


    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    public Role retrieveByName(String roleName) throws RoleNotFoundException {
        Role role=this.roleRepository.findByName(roleName).orElseThrow(()-> new RoleNotFoundException("Role with such name does not exist"));
        return role;
    }



    public void fillDatabaseWithRoles(String... roleNames) {

        for(String roleName : roleNames) {

            Optional<Role> optionalRole=this.roleRepository.findByName(roleName);

            if( ! optionalRole.isPresent() ) {

                Role role=new Role(roleName);

                this.roleRepository.save(role);
            }


        }

    }



}
