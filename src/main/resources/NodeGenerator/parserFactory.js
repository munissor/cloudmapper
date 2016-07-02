
var rawParser = require('./rawParser');
var xmlParser = require('./xmlParser');
var jsonParser = require('./jsonParser');

var MIME_TYPES = {
    '_raw': rawParser,
    'application/xml': xmlParser,
    'text/xml': xmlParser,
    'application/json': jsonParser,
};

module.exports = {
    getParser: function(contentType, body){

        var ctor = MIME_TYPES[contentType.toLowerCase()];
        if( ctor == null ){
            throw new Error('Unsupported MIME type for the body');
        }

        return new ctor(body);
    }
};