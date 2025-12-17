package com.thentrees.gymhealthtech.exception;

public abstract class InfrastructureException extends RuntimeException {
  public InfrastructureException(String msg, Throwable cause) { super(msg, cause); }
}
