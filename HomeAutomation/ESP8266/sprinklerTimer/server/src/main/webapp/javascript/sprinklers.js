/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/19/11
 * Time: 9:19 PM
 * To change this template use File | HvacSettings | File Templates.
 */
function activateZone(selectField) {
    sendAjaxRequest("sprinklersAjax/activateZone", "zone=" + selectField.value);
}

function setProgramMultiplier(programName, numberField) {
    sendAjaxRequest("sprinklersAjax/setProgramMultiplier", "programName=" + programName + "&value=" + numberField.value);
}

function setProgramEnable(programName, checkBoxField) {
    sendAjaxRequest("sprinklersAjax/setProgramEnable", "programName=" + programName + "&value=" + checkBoxField.checked);
}

function runNow(programName) {
    sendAjaxRequest("sprinklersAjax/runNow", "programName=" + programName);
}
