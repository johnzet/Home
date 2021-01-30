/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/19/11
 * Time: 9:19 PM
 * To change this template use File | HvacSettings | File Templates.
 */
function activateZone(selectField) {
    sendAjaxRequest("sprinklersAjax", "activateZone=" + selectField.value);
}

