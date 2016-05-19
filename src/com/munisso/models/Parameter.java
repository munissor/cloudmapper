package com.munisso.models;

/**
 * Created by rmunisso on 21/04/2016.
 */
public class Parameter
{
    public String name;

    public String[] aliases;

    public String logicalName;

    public Types kind;

    public String format;

    public String value;

    public boolean optional;

    public boolean multiple;

    public Parameter[] properties;
}
