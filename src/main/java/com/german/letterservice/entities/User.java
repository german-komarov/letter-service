package com.german.letterservice.entities;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="username")
@ToString(exclude = {"blockedContacts","contactSpammingCasesNumber"})
public class User implements UserDetails, Serializable {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;




    @Column(unique = true,nullable = false,updatable = false)
    private String username;


    @Column(nullable = false)
    private String password;



    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns = @JoinColumn(name = "user_id"),inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles=new HashSet<>();

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean isPhoneNumberApproved;


    @Column(nullable = false)
    private boolean isAccountNonExpired;

    @Column(nullable = false)
    private boolean isAccountNonLocked;

    @Column(nullable = false)
    private boolean isCredentialsNonExpired;

    @Column(nullable = false)
    private boolean isEnabled;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(joinColumns = @JoinColumn(name = "user_id"),inverseJoinColumns = @JoinColumn(name = "blocked_contact_id"))
    private Set<User> blockedContacts=new HashSet<>();





    private int maximumAllowedSpammingCases=3;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "users_spamming_cases_numbers_contacts",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "spamming_cases_number")
    @MapKeyJoinColumn(name = "contact_id")
    private Map<User,Integer> contactSpammingCasesNumber =new HashMap<>();






    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }



    public Integer addSpammingCase(User spammer) {
        Map<User,Integer> map=this.getContactSpammingCasesNumber();

        Integer spammingCasesNumber = map.get(spammer);
        spammingCasesNumber = spammingCasesNumber != null ? spammingCasesNumber : 0 ;

        spammingCasesNumber=map.put(spammer,spammingCasesNumber+1);

        return spammingCasesNumber;
    }



    public Integer removeSpammingCase(User notSpammer) {
        Map<User,Integer> map=this.getContactSpammingCasesNumber();

        Integer spammingCasesNumber=map.get(notSpammer);

        if (spammingCasesNumber != 0) {
            spammingCasesNumber = map.put(notSpammer, spammingCasesNumber - 1);
        }

        else {
            spammingCasesNumber=map.remove(notSpammer);
        }

        return spammingCasesNumber;
    }



}
