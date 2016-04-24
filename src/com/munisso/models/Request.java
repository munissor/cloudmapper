package com.munisso.models;

/**
 * Created by rmunisso on 21/04/2016.
 */
public class Request {
    public String verb;
    public String url;

    public Parameter[] urlReplacements;

    public Parameter[] queryString;

    public Parameter[] headers;

    public Body body;
}
