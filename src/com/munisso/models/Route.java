package com.munisso.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmunisso on 02/05/2016.
 */
public class Route {

    public Route(){

    }

    public Route(String name, String url, String verb){
        this.name = name;
        this.verb = verb;
        this.url = url;
    }

    public String name;

    public String verb;

    public String url;

    public String remoteVerb;

    public String remoteUrl;

    public MappingError routeError;

    public List<MappingParameter> parseRequest = new ArrayList<>();

    public List<MappingParameter> buildRequest = new ArrayList<>();

    public List<MappingError> requestErrors = new ArrayList<>();

    public List<MappingParameter> parseResponse = new ArrayList<>();

    public List<MappingParameter> buildResponse = new ArrayList<>();

    public List<MappingError> responseErrors = new ArrayList<>();

}
