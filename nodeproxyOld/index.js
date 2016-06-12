var request = require('request');
var restify = require('restify');
var config = require('config');
var parserFactory = require('./parserFactory');
var signature = require('./signature');
var formatUtils = require('./formatUtils');

var server = restify.createServer();
server.use(restify.queryParser());

server.get('/', function(req, res, next){
	var reqDate = req.header('x-ms-date') || req.header('Date');
	var reqContentType = 'application/xml';

	var urlString = 'https://s3.amazonaws.com/';

	var rHeaders = {};
	rHeaders['Content-Type'] = reqContentType;
	rHeaders['Date'] = formatUtils.formatDate(reqDate, 'iso8601');

	var body = '';

	var options = {method: 'GET', url: urlString, body: body, headers: rHeaders};

	signature.buildSignature('aws-v4', options);

	request(options, function(error, response, body){

		var resParser = parserFactory.getParser(response.headers['content-type'], body);

		var resContainer = [];
		var oContainer = resParser.getObjects('ListAllMyBucketsResult.Buckets.Bucket');
		oContainer.forEach(function(containerItem){
			var container = {};

			container.Name = resParser.getValue("Name", containerItem);

			resContainer.append(container);
		});

		var status = response.statusCode;
		var rBody = '';
		res.send(status, rBody);
		return next();
	});

});


/*

 <?xml version="1.0" encoding="UTF-8"?>
 <ListAllMyBucketsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
 <Owner>
 	<ID>a04e6119505c1d46f647feefe304fa7fa2bfe0d357202e76ae9641bf815e6aa5</ID>
 	<DisplayName>munissor</DisplayName>
 </Owner>
 <Buckets>
 	<Bucket>
 		<Name>testapicontainer</Name>
 		<CreationDate>2016-05-25T15:06:06.000Z</CreationDate>
 		</Bucket>
 	<Bucket><Name>testapicontainer2</Name><CreationDate>2016-05-25T15:48:42.000Z</CreationDate></Bucket><Bucket><Name>testapicontainer3</Name><CreationDate>2016-05-25T15:56:06.000Z</CreationDate></Bucket></Buckets></ListAllMyBucketsResult>

*/

server.put('/:name', function(req, res, next){
	var reqObjectName = req.params.name;
	var reqDate = req.header('x-ms-date') || req.header('Date');

	var urlString = 'https://s3.amazonaws.com/{name}'.replace('{name}', reqObjectName);

	var rHeaders = {};
	rHeaders['Date'] = formatUtils.formatDate(reqDate, 'iso8601');

	var body = '';

	var options = {method: 'PUT', url: urlString, body: body, headers: rHeaders};

	signature.buildSignature('aws-v4', options);

	request(options, function(error, response, body){
		var status = response.statusCode;
		var rBody = '';
		res.send(status, rBody);
		return next();
	});

});

server.del('/:name', function(req, res, next){
	var reqAccountName = req.params.account;
	var reqObjectName = req.params.name;
	var reqObjectType = req.query.restype;
	var reqOperationTimeout = req.query.timeout;

	var urlString = 'https://{account}.blob.core.windows.net/{name}'.replace('{account}', reqAccountName).replace('{name}', reqObjectName);

	var rHeaders = {};

	var body = '';

	var options = {method: 'DELETE', url: urlString, body: body, headers: rHeaders};

	signature.buildSignature('aws-v4', options);

	request(options, function(error, response, body){
		var status = response.statusCode;
		var rBody = '';
		res.send(status, rBody);
		return next();
	});

});


server.listen(config.port, function() {
	console.log('%s listening at %s', server.name, server.url);
});
