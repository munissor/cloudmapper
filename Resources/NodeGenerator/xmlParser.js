var xpath = require('xpath');
var xmldom = require('xmldom');


function XMLParser(body) {
    this.data = new xmldom.DOMParser().parseFromString(body);

    var namespace = this.data.documentElement.getAttribute('xmlns');
    if(namespace && namespace.length > 0) {
        this.namespace = 'x:';
        this.select = xpath.useNamespaces({'x': namespace});
    }
    else {
        this.namespace = '';
        this.select = xpath.select
    }
}

XMLParser.prototype.getValue = function(name, parent){
    var path = this._buildXPath(name, parent) + '/text()';

    return this.select(path, parent || this.data )[0].toString();
};

XMLParser.prototype.getObjects = function(name, parent) {
    var path = this._buildXPath(name, parent);
    return this.select(path, parent || this.data)
};

XMLParser.prototype.getObject = function(name, parent){
    var results = this.getObjects(name, parent);
    return results && results.length > 0 ? results[0] : null;
};


XMLParser.prototype._buildXPath = function(name, parent) {
    var path = name;

    if(!parent)
        path = '.' + path;
    else
        path = this.namespace + path;

    path = path.replace(/\./g, '/' + this.namespace);

    return path;
}

module.exports = XMLParser;