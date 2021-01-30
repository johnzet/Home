/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/19/11
 * Time: 9:19 PM
 * To change this template use File | HvacSettings | File Templates.
 */
function increaseTemp() {
    sendAjaxRequest("hvacAjax", "tempChangeAction=incrTemp", changeTempField);
}

function decreaseTemp() {
    sendAjaxRequest("hvacAjax", "tempChangeAction=decrTemp", changeTempField);
}

function changeTempField() {
    if (this.readyState != 4) {
        return;
    }
    var jsonData = eval('(' + this.responseText + ')');
    var field = document.getElementById("setTempField");
    field.innerHTML = jsonData.setTempField;
}

function modeChange(selectField) {
    sendAjaxRequest("hvacAjax", "modeChangeAction=" + selectField.value);
}

function fanOn(checkBoxField) {
    sendAjaxRequest("hvacAjax", "fanChangeAction=" + ((checkBoxField.checked)? "true" : "false"));
}




// history servlet

function periodicRefresh(period) {
    setTimeout("refreshHistory();", period);
}

function refreshHistory() {
    var startHoursAgo = document.getElementById("startHoursAgo").value;
    var endHoursAgo = document.getElementById("endHoursAgo").value;

    location.href = "/house/history?startHoursAgo=" + startHoursAgo + "&endHoursAgo=" + endHoursAgo;
}


//function
