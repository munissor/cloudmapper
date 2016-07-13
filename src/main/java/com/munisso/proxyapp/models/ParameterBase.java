package com.munisso.proxyapp.models;

/**
 * Created by rmunisso on 21/04/2016.
 */
public abstract class ParameterBase
{
    public String name;

    public String[] aliases;

    public String logicalName;

    public Types kind;

    public String format;

    public String value;

    public boolean optional;

    public boolean multiple;

    public FallbackOption fallback;

}
