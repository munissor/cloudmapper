package com.munisso.proxyapp.models;

/**
 * Created by rmunisso on 21/04/2016.
 */
public class Model {
    public String provider;
    public String service;
    public String serviceType;
    public String signature;
    public Operation commonParameters;
    public Operation[] operations;
    public Configuration[] configurations;
}
