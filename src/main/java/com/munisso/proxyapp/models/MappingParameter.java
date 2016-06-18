package com.munisso.proxyapp.models;

/**
 * Created by rmunisso on 02/05/2016.
 */
public class MappingParameter extends Parameter {
    public String location;

    public static MappingParameter duplicate(MappingParameter source){
        MappingParameter m = fromParameter(source);
        m.location = source.location;

        return m;
    }

    public static MappingParameter fromParameter(Parameter source){
        MappingParameter m = new MappingParameter();

        m.name = source.name;
        m.aliases = source.aliases;
        m.logicalName = source.logicalName;
        m.kind = source.kind;
        m.value = source.value;
        m.optional = source.optional;
        m.format = source.format;
        m.multiple = source.multiple;

        return m;
    }

    public MappingParameter[] properties;
}
