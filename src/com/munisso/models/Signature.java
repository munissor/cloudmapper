package com.munisso.models;

/**
 * Created by rmunisso on 15/05/2016.
 */
public class Signature {
    public Algorithms algorithm; // HmacSHA256
    public Encodings encoding; // base64
    public String format;
    public String separator;
    public String itemFormat;
    public SignatureItem[] items;
}
