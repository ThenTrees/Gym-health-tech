package com.thentrees.gymhealthtech.constant;

public final class ErrorMessages {
  // AUTH
  public static final String EMAIL_OR_PASSWORD_ALREADY_EXIST = "Email or Password already exists!";

  // NOTIFICATION
  public static final String SEND_MAIL_VERIFICATION_FAILED = "Failed to send verification email to ";
  public static final String SEND_NOTIFICATION_ERROR = "Error sending push notification chunk";

  //EQUIPMENT
  public static final String EQUIPMENT_ALREADY_EXISTS = "Equipment already exists!";
  public static final String UPLOAD_EQUIPMENT_IMAGE_FAILED = "Error uploading equipment image";


  private ErrorMessages() {
    throw new IllegalStateException("Utility class. Don't extended class!");
  }

}
