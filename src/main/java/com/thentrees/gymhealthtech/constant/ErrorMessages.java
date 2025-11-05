package com.thentrees.gymhealthtech.constant;

public final class ErrorMessages {
  // AUTH
  public static final String EMAIL_OR_PASSWORD_ALREADY_EXIST = "Email or Password already exists!";

  // NOTIFICATION
  public static final String SEND_MAIL_VERIFICATION_FAILED = "Failed to send verification email to ";



  private ErrorMessages() {
    throw new IllegalStateException("Utility class. Don't extended class!");
  }

}
