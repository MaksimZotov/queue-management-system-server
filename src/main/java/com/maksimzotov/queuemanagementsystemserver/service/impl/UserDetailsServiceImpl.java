package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepo accountRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AccountEntity> account = accountRepo.findByUsername(username);
        if (account.isEmpty()) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");
        }
        AccountEntity accountEntity = account.get();
        log.info("User found in the database: {}", username);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        return new User(accountEntity.getUsername(), accountEntity.getPassword(), authorities);
    }
}
