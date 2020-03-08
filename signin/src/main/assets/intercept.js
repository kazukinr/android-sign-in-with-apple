// intercept submit form
HTMLFormElement.prototype._submit = HTMLFormElement.prototype.submit;
HTMLFormElement.prototype.submit = interceptor;
window.addEventListener('submit', function(e) {
    interceptor(e);
}, true);
function interceptor(e) {
    var frm = e ? e.target : this;
    interceptor_onsubmit(frm);
    frm._submit();
}
function interceptor_onsubmit(f) {
    if (f.action == '${redirect_uri}') {
        var jsonArr = [];
        for (i = 0; i < f.elements.length; i++) {
            var parName = f.elements[i].name;
            var parValue = f.elements[i].value;
            var parType = f.elements[i].type;
            jsonArr.push({
                name : parName,
                value : parValue,
                type : parType
            });
        }
        recorder.recordPayload(f.method, f.action, JSON.stringify(jsonArr))
    }
}

// intercept ajax send
XMLHttpRequest.prototype.origOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
    this.recordedMethod = method;
    this.recordedUrl = url;
    this.origOpen(method, url, async, user, password);
};
XMLHttpRequest.prototype.origSend = XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.send = function(body) {
    if (body && this.recordedUrl == '${redirect_uri}') {
        recorder.recordPayload(this.recordedMethod, this.recordedUrl, body);
    }
    this.origSend(body);
};
