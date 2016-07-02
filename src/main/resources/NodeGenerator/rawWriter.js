function RawWriter(){

    this.data = null;

    this.writeValue = function(name, value, parent){
        this.data = value;
    }

    this.writeValues = function(name, values, parent) {
         throw new Error("Unsupported");
    }

    this.writeObject = function(name, parent) {
        throw new Error("Unsupported");
    }

    this.toString = function() {
          return this.data;
    }
}


module.exports = RawWriter;