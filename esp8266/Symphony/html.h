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
<body onload="loadDoc()">
<div class="header" id="theName"></div>
<h2>Symphony</h2>
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
var wsUri = "ws://"+location.hostname+"/ws";
websocket = new WebSocket(wsUri);
function init() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {
      var p = document.getElementById("files");
	  p.innerHTML = "";
	  var jsonObj = JSON.parse(this.responseText);
	  var len = 0;
	  for (i in jsonObj.files) {
		var s = jsonObj.files[i];
	    var lbl = document.createElement('label');
		lbl.innerHTML = s;
		var input = document.createElement('input');
		input.setAttribute('type',"radio");
		input.setAttribute('name',"fNames");
		input.setAttribute('value',s);
		p.appendChild(input);
		p.appendChild(lbl);
	  	var br = document.createElement('br');
		p.appendChild(br);
	    len++;
	  }
	  if (len >0) {
		var input = document.createElement('input');
		input.setAttribute('type',"button");
		input.setAttribute('onclick',"delFile()");
		input.setAttribute('value',"Delete");
		p.appendChild(input);
	  }
    }
  }
  xhttp.open('GET', '/getFiles', true);
  xhttp.send();
}
function delFile() {
	var data = '{"core":3,"data":"';
	var r = document.getElementsByName("fNames");	//radiobutton group
    for (var j=0, len=r.length; j<len; j++) {
    	if (r[j].checked) {
			data = data + r[j].value +'"}';
    	}
    }
	websocket.send(data);
	init();
}
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
function showSelectedFiles() {
  var fileInput = document.getElementById('fileInput');
  var status = document.getElementById("selectedFiles");
  status.innerHTML = ""
  for (var i = 0; i < fileInput.files.length; i++) {
    status.innerHTML += i+1
    status.innerHTML += ". "
    status.innerHTML += fileInput.files[i].name
    status.innerHTML += "<br>"
  }
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

#endif /* HTML_H_ */
