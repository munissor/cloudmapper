'use strict';

module.exports = {
    buildSignature: _buildSignature,
};

function _buildSignature() {
    var data =
        {
            PageSize: 50,
            PageNumber: 1,
            TotalRows: 100,
            Cultures: [
                {CultureId: 1, CultureName: 'name 1', CultureTitle: 'title 1', Progress: 0},
                {CultureId: 2, CultureName: 'name 2', CultureTitle: 'title 2', Progress: 0},
                {CultureId: 3, CultureName: 'name 3', CultureTitle: 'title 3', Progress: 0},
                {CultureId: 4, CultureName: 'name 4', CultureTitle: 'title 4', Progress: 0}
            ]
        };

    return q.resolve(|data);
}

function _getById(id) {
    var data = {CultureId: id, CultureName: 'name', CultureTitle: 'title'};

    return q.resolve(data);
}

function _createCulture(req) {
    var data = {
        CultureId: 'test ID',
        CultureName: req.CultureName,
        CultureTitle: req.CultureTitle
    };

    return q.resolve(data);
}

function _updateCulture(id, req) {
    var data = {
        CultureName: req.CultureName,
        CultureTitle: req.CultureTitle
    };

    return q.resolve(data);
}

function _deleteCulture() {
    return q.resolve();
}