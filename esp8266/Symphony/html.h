/*
 * html.h
 * The main html used by the captive portal, main config and ota_setup.
 * This also contains the default template for the html pages that each device will display.
 *
 *  Created on: Sep 23, 2018
 *      Author: cels
 */

#ifndef HTML_H_
#define HTML_H_

const char CONTROL_HTML1[] PROGMEM = R"=====(
<html>
<head>
<title>Home Symphony</title>
</head>
<link rel="stylesheet" type="text/css" href="symphony.css">
<script type="text/javascript" src="symphony.js"></script>
<body onload="loadControlPage()">
<input id="hiddenName" type="hidden">
<div class="header" id="theName">thename</div>
<h2>PIR Sensor</h2>
<div class="header" id="msg"></div>
<div class="footer" id="status"></div>
<div class="blurred"></div>
<div id="control" class="tabcontent">
</div>
</body>
</html>
)=====";

const char UPLOAD_HTML[] PROGMEM = R"=====(
<!DOCTYPE html>
<head>
  <title>Home Symphony</title>
<link rel="stylesheet" type="text/css" href="symphony.css">
<script type="text/javascript">
function uploadFile() {
  var fileInput = document.getElementById('fileInput');
  document.getElementById("msg").innerHTML = "";
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {
      document.getElementById("msg").innerHTML = this.responseText;
    }
  }
  var status = document.getElementById("selectedFiles");
  status.innerHTML = ""
  for (var i = 0; i < fileInput.files.length; i++) {
    status.innerHTML += i+1
    status.innerHTML += ". "
    status.innerHTML += fileInput.files[i].name
    status.innerHTML += "  sent<br>"
  }
	var form = document.getElementById('fileUpload');
    var formData = new FormData(form);
    xhttp.open('POST', '/uploadFile', true);
    xhttp.send(formData);
}
</script>
</head>
<body bgcolor="#EFE4B0" onload="init()"">
<h3>Life in Harmony</h3>
<fieldset><legend></legend>
<div>
<div style="width:500;"><p id='files'></p></div><br>
<form id='fileUpload' method='POST' action='/uploadFile' enctype='multipart/form-data'>
<input id='fileInput' type='file' name='file' multiple onchange='showSelectedFiles()' accept=".gz">
<input type='button' value='Upload' onclick='uploadFile()'>
<p id='selectedFiles'></p>
<p id='msg'></p>
</form>
</div>
</fieldset>
</body>
</html>
)=====";

const char AP_ADMIN_HTML[] PROGMEM = R"=====(
<html>
<head>
<title>Home Symphony</title>
<script type="text/javascript">
/*
 * Function that commits the Ap, passkey and Device name, and the (enabled, ip and port) of mqtt broker
 */
function init() {
	if (typeof websocket === 'undefined' || websocket.readyState != 1) {
		var wsUri = "ws://"+location.hostname+":8080/ws";
		websocket = new WebSocket(wsUri);
	} else {
		//do nothing, we are already connected
	}
}
function APcommitConfig() {
	var name = document.getElementById("pName").value;
	var ssid = document.getElementById("pSSID").value;
	var pwd = document.getElementById("pPass").value;
	var obj = { core: 2, 
			data: {
				name: name, 
				ssid: ssid, 
				pwd: pwd
			}
		};
	websocket.send(JSON.stringify(obj));
}
</script>
</head>
<body onload="init()">
<h2>Device Setup</h2>
<div id="device" class="tabcontent">
<h3>Manage Device</h3>
<form action='/config' method='get' id='formDevice'>
<center><fieldset style="width:300px"><legend><b>Settings</b></legend>
<table><tr><td>Name:</td><td><input type='text' id='pName' maxlength="8"></td></tr>
<tr><td>Wifi SSID:</td><td><input type='text' id='pSSID'></td></tr>
<tr><td>Passkey:</td><td><input type='text' id='pPass'></td></tr>
<tr><td><input type='button' id='btnCommit' value='Commit' onclick='APcommitConfig()'></td></tr>
</table>
</fieldset></center>
</form>
</div>
</body>
</html>
)=====";

#endif /* HTML_H_ */
