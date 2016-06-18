var xmlParser = require('./xmlParser');
var jsonParser = require('./jsonParser');

var MIME_TYPES = {
    'application/xml': xmlParser,
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