var exec = require('cordova/exec');

var CameraPicker = {
    captureImage: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'CameraPicker', 'captureImage', []);
    },
    pickImage: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'CameraPicker', 'pickImage', []);
    }
};

module.exports = CameraPicker;
