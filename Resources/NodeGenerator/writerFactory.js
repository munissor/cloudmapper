var xmlWriter = require('./xmlWriter');
var jsonWriter = require('./jsonWriter');

var MIME_TYPES = {
    'application/xml': xmlWriter,
    'application/json': jsonWriter,
};

module.exports = {
    getWriter: function(contentType){

        var ctor = MIME_TYPES[contentType.toLowerCase()];
        if( ctor == null ){
            throw new Error('Unsupported MIME type for the body writer');
        }

        return new ctor();
    }
};