package com.shinkson47.opex.backend.runtime.errormanagement.exceptions;

public class OPEXStartFailure extends Exception  {
    public OPEXStartFailure(Exception e) {
        super("A startup call was rejected due to the following exception; ", e) ;
    }
}

