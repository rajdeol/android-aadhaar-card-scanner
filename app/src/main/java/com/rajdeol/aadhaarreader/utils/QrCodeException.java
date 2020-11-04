package com.rajdeol.aadhaarreader.utils;
/**
 * QrCodeException wraps all the exceptions which occurs while scanning and decoding secure Aadharcard
 * Qr Code
 * @author Raj Deol
 */
public class QrCodeException extends Exception {

    public QrCodeException(String message) {
        super(message);
    }

    public QrCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public QrCodeException(Throwable cause) {
        super(cause);
    }
}
