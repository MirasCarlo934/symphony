/*
 * mainhtml.h
 * The main html used by the captive portal, main config and ota_setup.
 * This also contains the default template for the html pages that each device will display.
 *
 *  Created on: Mar 11, 2017
 *      Author: cels
 */

#ifndef MAINHTML_H_
#define MAINHTML_H_

const char CONTROL_HTML1[] PROGMEM = R"=====(
<!DOCTYPE html>
<head>
  <title>Home Symphony</title>
<link rel="stylesheet" type="text/css" href="control.css">
<script type="text/javascript">
  var name = "$NAME$";
</script>
<script type="text/javascript" src="symphony.js"></script>  
</head>
<body onload="loadDoc()" bgcolor="#EFE4B0">
<center>
<h3>Life in Harmony</h3>
<fieldset style="width:30em;text-align:center;"><legend>$AAA$</legend>
<div id="main" data-role="header" align="center">
</div></fieldset>
<div id='status' class='init'>Connecting...</div>
</center>
<div align="center">
For Debug Only:<input type="text" id="txt1"><br>
From Websocket:<textarea rows="5" cols="25" id="txt2"></textarea><br>
<input type="button" onclick="show()" value="test">
</div>
</body>
</html>
)=====";

const char UPLOAD_HTML1[] PROGMEM = R"=====(
<!DOCTYPE html>
<head>
  <title>Home Symphony</title>
<script type="text/javascript">
function init() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {
      var p = document.getElementById("files");
      p.innerHTML = this.responseText;
    }
  }
  xhttp.open('GET', '/getFiles', true);
  xhttp.send();
}
function uploadFile() {
  document.getElementById("msg").innerHTML = "";
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {
      document.getElementById("msg").innerHTML = this.responseText;
    }
  }
  var form = document.getElementById('fileUpload');
  var formData = new FormData(form);
  xhttp.open('POST', '/uploadFile', true);
  xhttp.send(formData);
}
</script>
</head>
<body bgcolor="#EFE4B0" onload="init()">
<h3>Life in Harmony</h3>
<fieldset><legend>Upload Data File</legend>
<div>
<p id='files'></p><br>
<form id='fileUpload' method='POST' action='/uploadFile' enctype='multipart/form-data'>
<input type='file' name='file'>
<input type='button' value='Upload' onclick='uploadFile()'>
<p id='msg'></p>
</form>
</div>
</fieldset>
</body>
</html>
)=====";

const char SHOWUPDATE[] PROGMEM = R"=====(
<!DOCTYPE html>
<head>
<title>Home Symphony</title>
<script>
var updateDone;
var counter = 0;
var xhttp = new XMLHttpRequest();
function getProgress() {
    counter++;
    xhttp.open("GET", "/progress", true);
    xhttp.send();
    document.getElementById("btn").innerHTML = counter;
}
function updateFirmware() {
  var choice = confirm("Warning!\nThis will update the firmware.");
  if (choice == true) {
    xhttp.onreadystatechange = function() {
	  if (this.readyState == 4 && this.status == 202) {
       document.getElementById("progress").innerHTML = this.responseText;
       document.getElementById("btn").disabled = true;
       updateDone = setInterval(getProgress, 1000);
      } else if (this.readyState == 4 && this.status == 200) {
        clearInterval(updateDone);
        counter = 0;
        document.getElementById("progress").innerHTML = this.responseText;
        document.getElementById("btn").innerHTML = "Done";
      } else if (this.readyState == 4 && this.status == 203) {
        document.getElementById("progress").innerHTML = "HTTP_UPDATE_FAILED";
        document.getElementById("btn").disabled = false;
        document.getElementById("btn").innerHTML = "Submit";
      } else {
        //document.getElementById("debug").innerHTML = document.getElementById("debug").innerHTML+"<br>state="+this.readyState+ " status="+this.status;
      }
    };
    var form = document.getElementById("update");
    xhttp.open("GET", "/handleUpdate?update=1", true);
    xhttp.send();
  } 
}
</script>
</head>
<body bgcolor="#EFE4B0">
<center>
<h3>Life in Harmony</h3>
<fieldset style="width:30em;text-align:center;"><legend>Firmware Update</legend>
<div id="main" data-role="header" align="center">
    <div id="progress"><H1>Press button to update firmware.</H1></div>
	<form action='/handleUpdate' method='get' id='update'><br>
    <button type="button" id="btn" onclick="updateFirmware()">Submit</button>
	</form>
    <!--div id="debug">debug</div-->
</div>
</fieldset>

</center>
</body>
</html>
)=====";

const char SHOWINIT[] PROGMEM = R"=====(
<!DOCTYPE html>
<html>
<head>
<title>Home Symphony</title>
<script type="text/javascript">
function uploadFile() {
  document.getElementById("msg").innerHTML = "Uploading file";
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {    	
    	document.getElementById("msg").innerHTML =  this.responseText +"\n"+ document.getElementById("msg").innerHTML;
    }
  }
  var form = document.getElementById('fileUpload');
  var formData = new FormData(form);
  xhttp.open('POST', '/uploadFile', true);
  xhttp.send(formData);
}
</script>
</head>
<body>
<div id="wifi" class="tabcontent">
<h3>Configure Wifi Settings</h3>
<form action='/handleWifiConfig' method='get' id='form'>
<fieldset style="width:300px"><legend><b>Wifi Settings:</b></legend>
SSID:<br><input type='text' name='ssid'><br>
Pass Phrase:<br><input type='text' name='pass'><br>
<select id='apmode' name='apmode'>
<option value='1'>Station/AP</option>
<option value='2'>Station+AP</option>
<option value='3'>Station</option>
</select><br>
<input type='submit' value='Submit'>
</form>
</div>
<div id="upload" class="tabcontent">
<h3>File Management</h3>
<fieldset><legend>Upload Data File</legend>
<p id='files'></p><br>
<form id='fileUpload' method='POST' action='/uploadFile' enctype='multipart/form-data'>
<input type='file' name='file'>
<input type='button' value='Upload' onclick='uploadFile()'>
</form>
</fieldset>
<p id='msg'></p>
</div>
</body>
</html>
)=====";

#endif /* MAINHTML_H_ */
