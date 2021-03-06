/*
This code is used for Google App Engine in Google Spread Sheet
 */
var test1 = {
    "parameter": {
        "type": "check",
        "name": "市川詩恩"
    }
};

var test2 = {
    "parameter": {
        "type": "listNotResponded"
    }
}

function doGet(e) {
    return process(e);
}

const c = SpreadsheetApp.getActive().getSheetByName("シート1");

function process(e) {
    // If request does not meet requirement, return 400
    if (!checkQualification(e)) return respond({code: 400});
    const indexD = getRowToday();
    if (indexD === -1) return respond({code: 500});
    if (e.parameter.type === "listNotResponded") return listNotResponded(indexD);
    const indexN = checkNameExistance(e.parameter.name);
    if (indexN === -1) return respond({code: 404});
    const cell = c.getRange(indexD, indexN + 1);
    const oldV = String(cell.getValue());
    if (e.parameter.type === "check") return respond({currentValue: oldV, code: 200});
    cell.setValue(e.parameter.temp);
    return respond({code: 202, oldValue: oldV});
}

function getRowToday() {
    const d = new Date(new Date().toLocaleString('ja-JP', {timeZone: 'Asia/Tokyo'}));
    return searchRowForDate(d.getMonth() + 1, d.getDate());
}

function listNotResponded(index) {
    const sheet = c.getDataRange().getValues();
    const names = sheet[3];
    const today = sheet[index - 1];
    let result = [];
    for (let i = 1; i < today.length; i++) {
        if (today[i] === '' || today[i] === null) {
            result.push(names[i]);
        }
    }
    return respond({code: 200, notResponders: result})
}

function checkNameExistance(name) {
    const data = c.getDataRange().getValues();
    return data[3].findIndex((v) => v === name);
}


//Input format should be conventional number for each field.
function searchRowForDate(month, date) {
    month -= 1;
    const dates = c.getRange(1, 1, c.getLastRow()).getValues();
    for (var i = 2; i <= dates.length; i++) {
        try {
            const d = new Date(dates[i].toLocaleString('ja-JP', {timeZone: 'Asia/Tokyo'}));
            if (!d.getMonth() || !d.getDate()) continue;
            if (month == d.getMonth() && date == d.getDate()) return i + 1;
        } catch (e) {
        }
    }
    return -1;
}

function checkQualification(e) {
    if (!e.parameter.type) return false;
    if (e.parameter.type === "listNotResponded") return true;
    if (e.parameter.type === "check") return e.parameter.name;
    if (e.parameter.type === "register") return e.parameter.name && e.parameter.temp;
    return false;
}

function respond(status) {
    switch (status.code) {
        case 200:
            status.message = "FOUND";
            break;
        case 202:
            status.message = "SUCCESS,REQUEST HAS BEEN PROCESSED"
            break;
        case 400:
            status.message = "BAD REQUEST";
            break;
        case 404:
            status.message = "REQUESTED NAME NOT FOUND";
            break;
        case 500:
            status.message = "FAILED TO FIND TARGET DATE";
            break;
    }
    return ContentService.createTextOutput(JSON.stringify(status))
        .setMimeType(ContentService.MimeType.JSON);
}