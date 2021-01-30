/**
 * Open a connection to the specified URL.
 *
 * @param  url    The URL to connect to.
 * @param  toSend The data to send to the server; must be URL encoded.
 * @param  responseHandler The function handling server response.
 */
function sendAjaxRequest(url, toSend, responseHandler)
{
    if (window.XMLHttpRequest)
    {
       // browser has native support for XMLHttpRequest object
       req = new XMLHttpRequest();
    }
    else if (window.ActiveXObject)
    {
        // try XMLHTTP ActiveX (Internet Explorer) version
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }

    if(req)
    {
        req.onreadystatechange = responseHandler;
        req.open("POST", url, true);
        req.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
        req.setRequestHeader("Cache-Control","no-cache");
        req.send(toSend);
    }
    else
    {
        alert('Your browser does not seem to support XMLHttpRequest.');
    }
}