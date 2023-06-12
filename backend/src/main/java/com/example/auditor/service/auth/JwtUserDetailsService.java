package com.example.auditor.service.auth;

import java.util.ArrayList;

import com.example.auditor.domain.user.DbUser;
import com.example.auditor.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DbUser dbUser = userRepository.findByUsername(username);
        if (dbUser == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new User(dbUser.getUsername(), dbUser.getPassword(), new ArrayList<>());
    }
}