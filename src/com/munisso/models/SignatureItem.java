package com.munisso.models;

/**
 * Created by rmunisso on 15/05/2016.
 */
public class SignatureItem {
    public String location;
    public String name;
    public String matchType; // Match, Doesn't match, null ( we need to do something like matches x-ms-* or doesn't match x-amz-*
    public String sortType; // None, Lexicographic
    // Do we need descending sort?
    public String separator;
    public String format;
}
