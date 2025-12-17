package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.model.User;

/**
 * Service for generating and sending verification tokens to users.
 */
public interface VerificationTokenService {

  /**
   * Generates a secure verification token and sends it to the user via email.
   * This method will:
   * - Delete any existing EMAIL verification tokens for the user
   * - Generate a new secure token
   * - Save the hashed token to the database
   * - Send the verification email to the user
   *
   * @param user the user to generate and send verification token for
   */
  void generateAndSendVerificationToken(User user);
}

