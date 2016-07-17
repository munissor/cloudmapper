var builder = require('xmlbuilder');

function XMLWriter(){

    this.data = {};

    this.writeValue = function(name, value, parent){
        if(!value)
            return;
        this._createNode(parent, name, { '#text':  value });
    };

    this.writeValues = function(name, values, parent) {
         this._createNode(parent, name, values.map(function(i){ return {'#text': i}; }));
    };

    this.writeObject = function(name, parent) {
       return this._createNode(parent, name, {});
    };

    this.toString = function() {
        return builder.create(this.data).toString();
    };

    this._getParent = function(parent){
        return parent || this.data;
    };

    this._createNode = function(parent, name, value){
        var names = name.split('.');
        var p = this._getParent(parent);

        for(var i=0; i<names.length -1; i++){
            var t = p[names[i]];
            if(!t){
                t = {};
                p[names[i]] = t;
            }
            p = t;
        }

        if( typeof p[names[names.length-1]] === 'undefined' ){
                    p[names[names.length-1]] = [];
        }

        p[names[names.length-1]].push(value);
        return value;
    };
}


module.exports = XMLWriter;