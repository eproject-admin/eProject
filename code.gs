/**
 * eProject - Google Apps Script Backend (code.gs)
 * Untuk sinkronisasi data dari aplikasi Android eProject ke Google Sheets & Firebase,
 * sekaligus menyajikan website Dashboard Pelaporan Jalan & Jembatan.
 * 
 * eProject by Dendy Sofian ©2024
 */

function doGet(e) {
  return HtmlService.createHtmlOutputFromFile('index')
      .setTitle('eProject - Aplikasi Jalan dan Jembatan')
      .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL);
}

function doPost(e) {
  try {
    var jsonString = e.postData.contents;
    var data = JSON.parse(jsonString);
    var action = data.action;

    if (action === "syncData") {
      // Sinkronisasi data dari Android
      syncAccounts(data.accounts);
      syncContracts(data.contracts);
      syncOpnames(data.opnames);
      
      return ContentService.createTextOutput(JSON.stringify({
        status: "success",
        message: "Sinkronisasi berhasil disinkronkan ke Google Sheets!"
      })).setMimeType(ContentService.MimeType.JSON);
    }

    return ContentService.createTextOutput(JSON.stringify({
      status: "error",
      message: "Action tidak dikenal."
    })).setMimeType(ContentService.MimeType.JSON);

  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({
      status: "error",
      message: err.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

// --- Helper Sync Functions to write into Sheets ---

function syncAccounts(accounts) {
  if (!accounts) return;
  var ss = SpreadsheetApp.getActiveSpreadsheet() || SpreadsheetApp.create("eProject_Database_BPJN_Kaltara");
  var sheet = ss.getSheetByName("Akun") || ss.insertSheet("Akun");
  
  if (sheet.getLastRow() === 0) {
    sheet.appendRow(["Email", "Role", "Approved", "Nomor Akun", "Masa Berlaku", "Nama Paket"]);
  }
  
  // Clear and rewrite with latest sync snapshot
  sheet.getRange(2, 1, sheet.getLastRow() + 1, sheet.getLastColumn() + 1).clearContent();
  
  accounts.forEach(function(acc) {
    sheet.appendRow([
      acc.email,
      acc.role,
      acc.isApproved ? "YES" : "NO",
      acc.userNumber || "-",
      acc.expiryDateString,
      acc.packageName
    ]);
  });
}

function syncContracts(contracts) {
  if (!contracts) return;
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName("Kontrak") || ss.insertSheet("Kontrak");
  
  if (sheet.getLastRow() === 0) {
    sheet.appendRow(["ID", "User Email", "Divisi", "Item Pekerjaan", "Satuan", "Volume", "Harga Satuan", "Jumlah Harga"]);
  }
  
  sheet.getRange(2, 1, sheet.getLastRow() + 1, sheet.getLastColumn() + 1).clearContent();
  
  contracts.forEach(function(con) {
    var total = con.volume * con.unitPrice;
    sheet.appendRow([
      con.id,
      con.userEmail,
      con.division,
      con.itemCodeAndName,
      con.unit,
      con.volume,
      con.unitPrice,
      total
    ]);
  });
}

function syncOpnames(opnames) {
  if (!opnames) return;
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName("Opname") || ss.insertSheet("Opname");
  
  if (sheet.getLastRow() === 0) {
    sheet.appendRow(["ID", "User Email", "Lokasi", "Sisi", "Ruas Jalan", "Tanggal", "Satuan", "Panjang (m)", "Lebar (m)", "Tinggi (m)", "Tebal (cm)", "Luasan (m2)", "Berat Jenis", "Divisi", "Item Pekerjaan", "Volume Total"]);
  }
  
  sheet.getRange(2, 1, sheet.getLastRow() + 1, sheet.getLastColumn() + 1).clearContent();
  
  opnames.forEach(function(op) {
    sheet.appendRow([
      op.id,
      op.userEmail,
      op.location,
      op.side,
      op.roadName,
      op.dateString,
      op.unit,
      op.length,
      op.width,
      op.height,
      op.thickness,
      op.area,
      op.density,
      op.division,
      op.itemName,
      op.calculatedVolume
    ]);
  });
}

// --- Fetch data for dashboard frontend ---

function getDashboardData() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  if (!ss) return { accounts: [], contracts: [], opnames: [] };
  
  return {
    accounts: getSheetRows(ss, "Akun"),
    contracts: getSheetRows(ss, "Kontrak"),
    opnames: getSheetRows(ss, "Opname")
  };
}

function getSheetRows(ss, name) {
  var sheet = ss.getSheetByName(name);
  if (!sheet) return [];
  var data = sheet.getDataRange().getValues();
  if (data.length <= 1) return [];
  
  var headers = data[0];
  var rows = [];
  for (var i = 1; i < data.length; i++) {
    var row = {};
    for (var j = 0; j < headers.length; j++) {
      row[headers[j].toString().toLowerCase().replace(/[^a-z0-9]/g, "")] = data[i][j];
    }
    rows.push(row);
  }
  return rows;
}
