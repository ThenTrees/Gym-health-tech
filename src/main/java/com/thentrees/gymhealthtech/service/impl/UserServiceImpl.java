package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.exception.BusinessException;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  @Override
  public User getUserById(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(String.format("User with id %s not found", id)));
  }
}
