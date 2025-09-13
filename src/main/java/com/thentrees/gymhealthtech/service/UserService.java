package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.model.User;
import java.util.UUID;

public interface UserService {
  User getUserById(UUID id);

  User getUserByUsername(String username);
}
