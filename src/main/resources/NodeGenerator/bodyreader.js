// Copyright 2012 Mark Cavage, Inc.  All rights reserved.

'use strict';
var zlib = require('zlib');
var assert = require('assert-plus');

///--- Helpers

function createBodyWriter(req) {
    var buffers = [];

    var contentType = req.contentType();
    var isText = false;

    if (!contentType ||
        contentType === 'application/json' ||
        contentType === 'application/xml' ||
        contentType === 'application/x-www-form-urlencoded' ||
        contentType === 'multipart/form-data' ||
        contentType.substr(0, 5) === 'text/') {
        isText = true;
    }

    req.body = new Buffer(0);
    return {
        write: function (chunk) {
            buffers.push(chunk);
        },
        end: function () {
            req.body = Buffer.concat(buffers);
            if (isText) {
                req.body = req.body.toString('utf8');
            }
        }
    };
}


///--- API

/**
 * reads the body of the request.
 * @public
 * @function bodyReader
 * @throws   {BadDigestError | PayloadTooLargeError}
 * @param    {Object} options an options object
 * @returns  {Function}
 */
function bodyReader(options) {
    options = options || {};
    assert.object(options, 'options');

    var maxBodySize = options.maxBodySize || 0;

    function readBody(req, res, next) {
        if (req.getContentLength() === 0 && !req.isChunked()) {
            next();
            return;
        }
        var bodyWriter = createBodyWriter(req);

        var bytesReceived = 0;
        var gz;

        function done() {
            var errorMessage;
            bodyWriter.end();

            if (!req.body.length) {
                next();
                return;
            }

            next();
        }

        if (req.headers['content-encoding'] === 'gzip') {
            gz = zlib.createGunzip();
            gz.on('data', bodyWriter.write);
            gz.once('end', done);
            req.once('end', gz.end.bind(gz));
        } else {
            req.once('end', done);
        }

        req.on('data', function onRequestData(chunk) {
            if (maxBodySize) {
                bytesReceived += chunk.length;

                if (bytesReceived > maxBodySize) {
                    return;
                }
            }

            if (gz) {
                gz.write(chunk);
            } else {
                bodyWriter.write(chunk);
            }
        });

        req.once('error', next);
        req.resume();
    }

    return (readBody);
}

module.exports = bodyReader;
