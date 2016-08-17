var rawParser = require('./rawParser');
var xmlParser = require('./xmlParser');
var jsonParser = require('./jsonParser');

var MIME_TYPES = {
    '_raw': rawParser,
    'application/xml': xmlParser,
    'text/xml': xmlParser,
    'application/json': jsonParser,
};

function convertBody(contentType, body){
    if(Buffer.isBuffer(body)){
        var parser = MIME_TYPES[contentType.toLowerCase()];
        if(parser == null || parser === rawParser ){
            return body.toString('binary');
        }

        return body.toString('utf-8');
    }

    return body;
}

module.exports = {
    getParser: function(contentType, body){
        body = convertBody(contentType, body);
        var ctor = MIME_TYPES[contentType.toLowerCase()];
        if( ctor == null ){
            throw new Error('Unsupported MIME type for the body');
        }

        return new ctor(body);
    }
};