package com.munisso;

class Test {
/*
*

Mapper foreach operation generate a route

    Handle request

        The request has parameters that can be anywhere (url, headers, body)
        Extract parameters from request using source model
        Convert each parameter to the format required by the destination model
        if mandatory parameter of the destination model is missing, output Error

        Foreach parameter, format request using destination model

        TODO: how do we describe the "body" or complex types in general (e.g: header containing JSON)?

        Sent the request to the destination provider

        THe response has parameters that can be in the header or body

        Extract parameters from response using destination model

        Convert each paramter to the format required by the source model

        If mandatory parameters are missing, output Error

 If operations are missing from destination provider, output an Error

*/
}