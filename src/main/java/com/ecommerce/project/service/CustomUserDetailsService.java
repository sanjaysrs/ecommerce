package com.ecommerce.project.service;

import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userOptional = userRepository.findUserByEmail(username);

        if (userOptional.isEmpty())
            throw new RuntimeException("Invalid username");

        User user = userOptional.get();

        if (!user.isEnabled())
            throw new DisabledException("User is disabled");

        if (!user.isVerified()) {
            throw new DisabledException("User account is not verified");
        }

        String password = user.getPassword();

        List<GrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role->authorities.add(new SimpleGrantedAuthority(role.getName())));

        return new org.springframework.security.core.userdetails.User(username, password, authorities);

    }

}
