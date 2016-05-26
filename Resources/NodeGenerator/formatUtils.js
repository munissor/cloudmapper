var moment = require('moment');

module.exports = {
    formatDate: formatDate
};

var DATE_ISO8601 = 'YYYYMMDDTHHmmss';

function formatDate(value, format){
    var d;
    if(format == 'iso8601'){
        d = _parseDate(value);
        return d.utc().format(DATE_ISO8601) + 'Z';
    }
    else if(format == 'rfc1123'){
        d = _parseDate(value);
        return d.format('ddd, DD MMM YYYY HH:mm:ss z');
    }

    return value;
}

function _parseDate(value){
    var formats = [
        DATE_ISO8601
    ];

    var momentDate = null;
    for(var i=0; i<formats.length; i++){
        var m = moment(value, formats[i], true);
        if(m.isValid()){
            momentDate = m;
            break;
        }
    }

    if( !momentDate ){
        momentDate = moment(new Date(value));
    }

    return momentDate;
}

