function RawParser(body) {
    this.data = body;
}

RawParser.prototype.getValue = function(name){
    return this.data;
};


// Gets multiple scalar values
RawParser.prototype.getValues = function(name, parent){
    throw Error("Unsupported");
};

// Gets multiple objects
RawParser.prototype.getObjects = function(name, parent) {
   throw Error("Unsupported");
};

// Gets a single object
RawParser.prototype.getObject = function(name, parent){
    throw Error("Unsupported");
};


module.exports = RawParser;