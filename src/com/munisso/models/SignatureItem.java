package com.munisso.models;

/**
 * Created by rmunisso on 15/05/2016.
 */
public class SignatureItem {
    public String location;
    public String name;
    public MatchTypes matchType;
    public SortTypes sortType = SortTypes.Lexicographic;
    // Do we need descending sort?
    public String separator;
    public String format;
}
