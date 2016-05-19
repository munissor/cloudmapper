package com.munisso.models;

/**
 * Created by rmunisso on 15/05/2016.
 */
public class Signature {
    public String algorithm; // HmacSHA256
    public String encoding; // base64
    public String format;
    public String separator;
    public String itemFormat;
    public SignatureItem[] items;
}
