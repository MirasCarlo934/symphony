const CMD_INIT = 1
const CMD_VALUES = 2
const CMD_CLIENT = 10
const CORE_GETDEVICEINFO = 4;
const CORE_TOCHILD = 7;
const CORE_START_HEARTBEAT = 8;
const CORE_VALUERESPONSE = 20


var itm;
var updateDone;
var counter = 0;
var isUpdateFW = false;
var mac;		//the identity of the device server
var cid;		//the identity of this client
var hb;			//the variable for the heartbeat timeout timer


initWs();

function initWs() {
	if (typeof websocket === 'undefined' || websocket.readyState != 1) {
		var wsUri = "ws://"+location.hostname+":8080/ws";
		websocket = new WebSocket(wsUri);
		wsHandler();
	} else {
		//do nothing, we are already connected
	}
}

function openTab(evt, tabName) {
	document.getElementById("msg").innerHTML = "";
	document.getElementById("status").innerHTML = "";
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
}
function initialize() {
  document.getElementById("defaultOpen").click(); 
}
/*
 * This is the function that sends the form data to the ESP for the update of the firmware.
 * We are using ajax here.
 */
function updateFirmware() {
	var start = new Date();
	  document.getElementById("status").innerHTML = "Updating firmware...";
	  var xhttp = new XMLHttpRequest();
	  xhttp.onreadystatechange = function() {
	    if (this.readyState==4 && this.status==200) {
	      document.getElementById("status").innerHTML = this.responseText;
//	      document.getElementById("msg").innerHTML = "Please refresh page or wait for READY status.";
	      isUpdateFW = true;
	    }
	  }
	  xhttp.upload.addEventListener('progress', 
		function(e){
		  var elapsed = (new Date() - start ) / 1000;
//		  document.getElementById("status").innerHTML = 'Uploaded: '+Math.ceil(e.loaded/e.total * 100) + '%' ;
		  document.getElementById("status").innerHTML = 'Elapsed: '+ Math.ceil(elapsed) + 's' ;
		  document.getElementById("pct").innerHTML = Math.ceil(e.loaded/e.total * 100)+ '%'  ;
		  document.getElementById("pct_bar").style.width = Math.ceil(e.loaded/e.total * 100) + '%';
	  	}, false);
	  xhttp.upload.addEventListener("error", 
		function(e){
		  alert("firmware load error");
	  	}, false);
	  xhttp.upload.addEventListener("abort", 
		function(e){
		  alert("firmware load aborted");
	  	}, false);
	  xhttp.upload.addEventListener("load", 
		function(e){
		  //alert("firmware successfully loaded");
	  	}, false);
	  if( document.getElementById('bin').files.length === 0){
		  document.getElementById("status").innerHTML = "Please choose bin file.";
	  } else {
		  var form = document.getElementById('fileUpload');
		  var formData = new FormData(form);
		  xhttp.open('POST', '/updateFirmware', true);
		  xhttp.send(formData);  
	  }
}
/**
 * toggles the mqttIp and mqttPort to enabled/disabled
 * @returns
 */
function toggleMqtt() {
	document.getElementById("mqttIp").disabled = !document.getElementById("mqttEnabled").checked;
	document.getElementById("mqttPort").disabled = !document.getElementById("mqttEnabled").checked;	
}
/*
 * Function that commits the Ap, passkey and Device name, and the (enabled, ip and port) of mqtt broker
 */
function commitConfig() {
	var name = document.getElementById("pName").value;
	var ssid = document.getElementById("pSSID").value;
	var pwd = document.getElementById("pPass").value;
	var mqttIp = document.getElementById("mqttIp").value;
	var mqttPort = document.getElementById("mqttPort").value;
	var chk = document.getElementById("mqttEnabled");
	var mqttEnabled = 0;
	if (chk.checked)
		mqttEnabled = 1;
	var obj = { core: 2, 
			data: {
				name: name, 
				ssid: ssid, 
				pwd: pwd,
				mqttEnabled: mqttEnabled, 
				mqttIp: mqttIp, 
				mqttPort: mqttPort
			}
		};
	websocket.send(JSON.stringify(obj));
}
/**
 * Sends AJAX request to the server
 * 
 * @param method = 'POST', 'GET'
 * @param url = the url for the request 
 * @param responseHandler = the function that will be called onreadystatechange.  This should handle the response of the server.
 * @returns
 */
function sendToServer(method, url, responseHandler, formId){
  var xhttp;
  xhttp=new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
    	responseHandler(this);
    }
  };  
  xhttp.open(method, url, true);
  if (method == "GET") {
	  xhttp.send();  
  } 
  if (method == "POST") {  
	  var form = document.getElementById(formId);
	  var formData = new FormData(form);
	  xhttp.send(formData);
  }
}
/*
 * cels TODO, may not be needed, we could have this handled in wsHandler
 */
//function genericHandler(xhttp) {
//	var jsonObj = JSON.parse(xhttp.responseText);
//	if (jsonObj.resp == "1") {
//		document.getElementById("msg").innerHTML = jsonObj.msg;
//		document.getElementById("btn").disabled = true;
//		updateDone = setInterval(getProgress, 1000);
//	} else if (jsonObj.resp == "0") {
//		document.getElementById("msg").innerHTML = jsonObj.msg;
//		clearInterval(updateDone);
//		counter = 0;
//		document.getElementById("btn").innerHTML = "Done";
//	} else if (jsonObj.resp == "203") {
//		document.getElementById("msg").innerHTML = jsonObj.msg;
//		clearInterval(updateDone);
//		counter = 0;
//		document.getElementById("btn").innerHTML = "Update";
//		document.getElementById("btn").disabled = false;
//	}
//}

function uploadFile() {
  document.getElementById("status").innerHTML = "";
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {
      document.getElementById("status").innerHTML = this.responseText;
    }
  }
  var form = document.getElementById('fileUpload');
  var formData = new FormData(form);
  xhttp.open('POST', '/uploadFile', true);
  xhttp.send(formData);
}
//function getProgress() {
//	counter++;
//	sendToServer('GET', '/progress', genericHandler);
//	document.getElementById("btn").innerHTML = counter;
//}	

/*
 * Loads the control page
 */
function loadControlPage() {
  var xhttp;
  xhttp=new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
    	renderPage(this);
    }
  };
  xhttp.open("GET", "/properties.html", true);
  xhttp.send();
}
/**
 * Renders the control page returned by /properties.html
 * @param xhttp
 * @returns
 */
function renderPage(xhttp) {
	//reponseText is of this form:
	//	{"cmd":1,"name":"symphonyLight",
	//		"data":[
	//			{"id":"0001","typ":10,"lbl":"Red","min":0,"max":1,"val":1,"grp":"Grp1"},
	//			{"id":"0002","typ":10,"lbl":"Blue","min":0,"max":1,"val":0,"grp":"Grp1"},
	//			{"id":"0003","typ":10,"lbl":"Green","min":0,"max":1,"val":0,"grp":"Grp1"}]}
	//RADIO_CTL = 10, BUTTON_CTL = 20, SLIDER_CTL = 30 , RADIO_SNSR = 50, BUTTON_SNSR = 60, SLIDER_SNSR = 70, UNDEF = 99
	//this is the temp array of object {typ:1,lbl:'RED',val:'0007', grp:'g2'}
    jsonResponse = JSON.parse(xhttp.responseText);
//alert(xhttp.responseText)
    itm = jsonResponse["data"];
    name = jsonResponse["name_mac"];
    var splitStr = name.split("_");
    var hiddenName = document.getElementById("hiddenName");
	hiddenName.value = splitStr[0];
	mac = splitStr[1];
	document.getElementById("theName").innerHTML = jsonResponse["name_mac"];
    getFirmwareVersion();	//we get the firmware version and show it in the header
	//we will create a new array within an array grouped according to temp[i].grp
	//[ 
	//    [{typ:1,lbl:'RED',val:'0007'},{typ:'Rad',lbl:'GREEN',val:'0007'}],
	//    [{typ:1,lbl:'HUE',val:'0007'},{typ:'Rad',lbl:'SATURATION',val:'0007'}],
	// ]
	//  grp['grp1']:[{typ:1,lbl:'RED',val:'0007'},{typ:'Rad',lbl:'GREEN',val:'0007'}]
	//  grp['grp2']:[{typ:1,lbl:'HUE',val:'0007'},{typ:'Rad',lbl:'SATURATION',val:'0007'}]
    var arrayLength = itm.length;
    var grp = new Object();   //a vector object where the key is the grpid.
    /** we need to create this DOM
    <label class="bar barchkbox">
    <div class="lbl">RED</div>
    <input lbl="RED" id="0001" name=mac value="0001" onclick="sendOnOffWs(this)" type="radio">
    <div class="btn btntxt"></div>
    </label><br>
    */
    for (var i = 0; i < arrayLength; i++) {
    	var thisGroup = grp[itm[i].grp];
    	if(thisGroup==undefined) {
        	thisGroup = new groups();
        	thisGroup.add(itm[i]);
        	grp[itm[i].grp] = thisGroup;
    	} else {
        	thisGroup.add(itm[i]);
    	}
    }
    var mainDiv = document.getElementById("control");
    mainDiv.innerHTML = ""; //let us reset the contents for control element
    var cntr = document.createElement('center');
    mainDiv.appendChild(cntr);
    for (var k in grp) {
    	var fs = document.createElement('fieldset');
    	fs.setAttribute('style', 'width:290px;text-align:center;')
    	var legend = document.createElement('legend');
    	legend.innerHTML = k;
    	fs.appendChild(legend);
    	cntr.appendChild(fs);
    	for (var j = 0; j < grp[k].ctr; j++) {
    		var p = grp[k].properties[j];
    		var lbl = document.createElement('label');
    		lbl.setAttribute('class', "bar barrng");
    		fs.appendChild(lbl);
    		var div1 = document.createElement('div');
    		div1.setAttribute('class', "lbl");
    		div1.innerHTML = p.lbl;
    		var div2 = document.createElement('div');
    		div2.setAttribute('class', "btn btntxt");
    		var input = document.createElement('input');
    		input.setAttribute('onclick',"sendOnOffWs(this)");
    		input.setAttribute('lbl',p.lbl);
//    		input.setAttribute('id',name+":"+p.val);
    		input.setAttribute('id',p.id);
    		input.setAttribute('name', k);
    		if (p.typ == 10 || p.typ == 50) { //RADIO_CTL = 10, RADIO_SNSR = 50 
    			input.setAttribute('type',"radio");
    			if (p.val == 1)
    				input.checked=true;
    		} else if (p.typ == 30 || p.typ == 70) { //SLIDER_CTL = 30 , SLIDER_SNSR = 70
        		input.setAttribute('type',"text");
        		input.setAttribute('min',p.min);
        		input.setAttribute('max',p.max);
        		input.setAttribute('onclick',"getRange(this)");
        		input.value = p.val;
        		div2.setAttribute('class', "txt btntxt");
        		div2.setAttribute('id', "lbl_"+p.id+":rng");
        		div2.innerHTML = p.val;
    		} else {  //BUTTON_CTL = 20, BUTTON_SNSR = 60, UNDEF = 99
    			lbl.setAttribute('class', "bar barchkbox");
    			input.setAttribute('type',"checkbox");
    			if (p.val==1)
	              input.checked=true;
	            else
	              input.checked=false;
    		}
    		lbl.appendChild(div1);
    		lbl.appendChild(input);
    		lbl.appendChild(div2);
    		br = document.createElement('br');
    		fs.appendChild(br);
        } 
    }
    openTab(event, 'control');
    websocket.send('{"core":20,"data":"VALUES"}');//we send a VALUES command to get the values for the displayed elements
}

function groups() {
	this.ctr = 0;
	this.properties = [];
	this.add = function(prop) {
		//alert("groups ctr"+this.ctr+" prop.lbl="+prop.lbl)
		this.properties[this.ctr] = prop;
		this.ctr++;
	}
}
function getRange(e) {
  var tdiv = document.getElementById("lbl_"+e.id+":rng");
  var rangeVal = 0;
  if (tdiv.textContent.length>0) {
    rangeVal = tdiv.textContent;
  }
  var popup = document.getElementById("popup");
  if (popup!=null) {
    popup.remove()
  }
  var divRange = document.createElement('div');
  divRange.setAttribute('class', "init popup");
  divRange.setAttribute('id', "popup");
  divRange.setAttribute('onclick','closeIt()');
  divRange.innerHTML = "<label class='rng barrng'><div class='lbl'>"+e.getAttribute("lbl")+
    "</div><div class='slider'><input type='range' id='"+ e.id + "_" + 
    "rng' parent='"+e.id+"' min='"+e.getAttribute("min")+"' max='"+e.getAttribute("max")+
    "' onchange='sendRangeWs(this)' value="+rangeVal+"></div></label><br>";
  document.body.appendChild(divRange);
}
/**
 * Generic function to send data to server using websocket
 * @param is the command to be sent to the server
 * @param formId is the id of the container form
 * @returns
 */
function sendWSRequestToServer(cmd, formId) {
	var count = document.getElementById(formId).elements.length;
	var jsonRequest = {"core":7, "cmd":cmd};//we are sending a cmd to the device
	for (i=0;i<count;i++) {
		var id = document.getElementById(formId).elements[i].id;
		var typ = document.getElementById(formId).elements[i].type;
		if (typ != "button" ) {
			//we construct a json request if element is not a button
			jsonRequest[id] = document.getElementById(formId).elements[i].value;	
		}
	}
	websocket.send(JSON.stringify(jsonRequest));
}
function sendOnOffWs(e) {
	var jsonResponse = {"core":7, "cmd":10};// core:7 - this transaction is to control the device
	jsonResponse["mac"] = mac;
	jsonResponse["ssid"] = e.id;
	jsonResponse["cid"] = cid;
	if (e.checked) {
		jsonResponse["val"] = 1;
	} else {
		jsonResponse["val"] = 0;
	}
	websocket.send(JSON.stringify(jsonResponse));
}
function sendRangeWs(e) {
//  var txt = document.getElementById("txt1");
//  txt.value="range " +e.id+ "=" +e.value;
	document.getElementById("popup").remove();
  	var tdiv = document.getElementById("lbl_"+e.getAttribute("parent")+":rng");
  	tdiv.textContent = e.value;
  	var jsonResponse = {"core":7, "cmd":10};// core:7 - this transaction is to control the device
	jsonResponse["mac"] = mac;
	jsonResponse["ssid"] = e.getAttribute("parent");
	jsonResponse["cid"] = cid;
	jsonResponse["val"] =  e.value;
	websocket.send(JSON.stringify(jsonResponse));
}
/**
 * The websocket keep alive
 * Will only be executed during transactions where device is doing reboot
 * 		1. commitConfig()
 * 		2. updateFirmware() 
 */
function wsHeartbeat() {
  if (typeof websocket === 'undefined' || websocket.readyState != 1) {
	  //do nothing, there is no websocket connection
  } else {
	  websocket.send('{"core":5}');//send a ping core command
	  hb = setTimeout(wsHeartbeat, 10000);	//interval is 10s
  }
}
/**
 * The websocket handler
 * @returns
 */
function wsHandler() {	 
     websocket.onopen = function(evt) {
//       for (i=1;i<=2;i++) {
//         var hover = document.getElementById("tmp"+i);
//         hover.parentNode.removeChild(hover);
//       }
    	 var status = document.getElementById("status");
         status.innerHTML="READY";
         clearTimeout(hb);
     };
     websocket.onclose = function(evt) {
       //alert("DISCONNECTED");
    	 var status = document.getElementById("status");
         status.innerHTML="DISCONNECTED";
         initWs();
     };
     websocket.onmessage = handleWsMessage;
     websocket.onerror = function(evt) {
       console.log("ERROR: " + evt.data);
     };
} 
/**
 * Function that handles control transactions from directly connected WS Clients.
 * @param evt
 * @returns
 */
function handleControl(evt) {
	var jsonResponse = JSON.parse(evt.data);
  	var core = jsonResponse["core"];
	if (core == CMD_VALUES) //device VALUES changed
	{
//alert(JSON.stringify(jsonResponse));
		name = document.getElementById("hiddenName").value;
//alert(" name " + name +" mac " +mac);
//alert(" name_mac " + jsonResponse["name_mac"]);
 		if (jsonResponse["name_mac"] == name+'_'+mac) {
//alert(" jsonResponse.data " + jsonResponse.data);
 			for (i in jsonResponse.data){
				var input =  document.getElementById(jsonResponse.data[i].id);
//alert(" id start " + jsonResponse.data[i].id);
				if (input.type == "checkbox" || input.type == "radio") {
	            	if (jsonResponse.data[i].val == 1)
	    				input.checked=true;	
	            	else
	            		input.checked=false;
	            }
//alert(" id end " + jsonResponse.data[i].id);
 			}
 		}
	}
}
/*
 * evt.data is of the form
 * {"cmd":1,"box":"status","msg":"message"}
 * 
 * WE WILL DEPRECATE THIS!!! - Sep 06 2019
 * Websocket should only be used for handling control transactions from directly connected WS clients (function handleControl).
 * Handling for admin and config transactions should be via Ajax.
 * 
 */
function handleWsMessage(evt) {
  	var jsonResponse = JSON.parse(evt.data);
  	var core = jsonResponse["core"];
  	var cmd = jsonResponse["cmd"];
  	var box = jsonResponse["box"];
  	var status = document.getElementById(box);
  	switch(core) {
  		case CORE_TOCHILD://data from server to the child javascript
  			{
	  			var msg = document.getElementById("msg");//comment this out later
	  			msg.innerHTML = JSON.stringify(jsonResponse);//comment this out later
				if (serverResponseHandler!=null) {//serverResponseHandler method can be defined in the child's javascript
					serverResponseHandler(jsonResponse);//pass the jsonResponse to the child's javascript
				}
	  			break;
  			}
  		case CORE_START_HEARTBEAT: 
  			{
	  			wsHeartbeat();
//	  			document.getElementById("msg").innerHTML = "Please refresh page or wait for READY status.";
	  	        document.getElementById("status").innerHTML = "Device Reboot";
	  			break;
	  		}
  		case CORE_VALUERESPONSE://value response from server
  			{
  			switch(cmd) {
  		  	 case CMD_VALUES:
  		   		//response from the VALUES
  		//alert(JSON.stringify(jsonResponse));
//				name = document.getElementById("theName").innerHTML;
  		  		name = document.getElementById("hiddenName").value;
//alert("element: name " + name +" mac " +mac +" from json: name_mac " + jsonResponse["name_mac"]);
  		   		if (jsonResponse["name_mac"] == name+'_'+mac) {
//alert(" jsonResponse.data " + jsonResponse.data);
  		   			for (i in jsonResponse.data){
  						var input =  document.getElementById(jsonResponse.data[i].id);
//alert(" id start " + jsonResponse.data[i].id);
  						if (input.type == "checkbox" || input.type == "radio") {
  			            	if (jsonResponse.data[i].val == 1)
  			    				input.checked=true;	
  			            	else
  			            		input.checked=false;
  			            }
//alert(" id end " + jsonResponse.data[i].id);
  		   			}
  		   		}
  		  		 break;
  		  	 case CMD_CLIENT://data forwarded by the server from a client.
  		//alert(JSON.stringify(jsonResponse));
  				if (jsonResponse.mac == mac && !(jsonResponse.cid == cid)) {//update input if cid is not our cid
  					var input =  document.getElementById(jsonResponse.ssid);
  					if (input.type == "checkbox" || input.type == "radio") {
  		            	if (jsonResponse.val == 1)
  		    				input.checked=true;	
  		            	else
  		            		input.checked=false;
  		            }
  				}
  		  		 break;
  		  	 default:
  		  	 }
  			break;
  		}
  	}
  	if (status!=null) {
  		if (jsonResponse.msg!=null)
  			status.innerHTML = jsonResponse.msg;
  	}
  	
  /**
   * 
   * code below is deprecated	
   if (jsonResponse["msg"] != "Connected") {
	   if ( core != CORE_TOCHILD ) {
		  	 //evt.data is of the form: {"cmd":1,"data",{data}}
		  	 switch(cmd) {
		  	 case CMD_INIT:
		  	 	//response from the INIT
		//alert("symphony.js response from the INIT   " + JSON.stringify(jsonResponse));
		  		var hiddenName = document.getElementById("hiddenName");		
		  		cid = jsonResponse["cid"];
		  		hiddenName.value = jsonResponse["name"];
		  		
		  		var header = document.getElementById("theName");  		
		  		header.innerHTML = jsonResponse["name"] + " v"+jsonResponse["ver"];
		  		mac = jsonResponse["mac"];
		  		var msg = document.getElementById("msg");
		  		msg.innerHTML = "Synchronized";
		  	 	break;
		  	 default:   
	   }
  	 }
   } else {
		clearInterval(updateDone);
		status.innerHTML= "Synchronized! Ready for command.";
		websocket.send('{"core":1,"data":"INIT"}');//we send an INIT command to get the deviceName and mac
		if (isUpdateFW) {
			status.innerHTML= "Reboot successful.";
			isUpdateFW = false;
		}
   }*/
};

/**
 * handles the device info response
 * @param xhttp - the response xhttp object
 * @returns
 */
function getDeviceInfoHandler(xhttp) {
	document.getElementById("msg").innerHTML = xhttp.responseText;
	var jsonResponse = JSON.parse(xhttp.responseText);
	document.getElementById("pName").value = jsonResponse.name;
	document.getElementById("pSSID").value = jsonResponse.ssid;
	document.getElementById("pPass").value = jsonResponse.pwd;
	if (jsonResponse.mqttEnabled == 1)
		document.getElementById("mqttEnabled").checked=true;
	else
		document.getElementById("mqttEnabled").checked=false;
	document.getElementById("mqttIp").value = jsonResponse.mqttIp;
	document.getElementById("mqttPort").value = jsonResponse.mqttPort;
	toggleMqtt();
}

/**
 * Gets the info of the device
 * 		- Device Name
 * 		- Wifi AP
 * 		- Passkey
 * 		
 * @returns
 */
function getDeviceInfo() {
	//we send an INIT command to get the deviceName and mac
//	websocket.send('{"core":4,"data":"INF"}'); deperecated, we should use AJAX
	sendToServer('GET', '/devInfo', getDeviceInfoHandler);
}
/**
 * Gets the mqtt details
 * 		- ip
 * 		- port
 * 		
 * @returns
 */
function getMqttInfo() {
	sendToServer('GET', '/mqttInfo', getDeviceInfoHandler);
}
/**
 * handles the firmware version response
 * @param xhttp - the response xhttp object
 * @returns
 */
function getFirmwareVersionHandler(xhttp) {
	document.getElementById("theName").innerHTML = document.getElementById("theName").innerHTML + "v" + xhttp.responseText
}

/**
 * Gets the firmware version
 * 		
 * @returns
 */
function getFirmwareVersion() {
//alert("getFirmwareVersion")
	sendToServer('GET', '/fwVersion', getFirmwareVersionHandler);
}
/**
 * handles the response of device for the /setMqttConfig url
 * @param xhttp - the response xhttp object
 * @returns
 */
function setMqttHandler(xhttp) {
	alert (xhttp.responseText)
}

function closeIt(){
  document.getElementById("popup").remove();
}
//////////////////////////////////////////////////////////////////////////////
//test functions
//////////////////////////////////////////////////////////////////////////////
function test() {
	var o = new Object();
	o.responseText="{typ:'Rad',lbl:'RED',val:'0007', grp:'g2'},{typ:'Rad',lbl:'GREEN',val:'0007', grp:'g2'},{typ:'Rad',lbl:'BLUE',val:'0007', grp:'g2'},{typ:'Btn',lbl:'STOP',val:'0009', grp:'g1'},{typ:'Rng',lbl:'Hue',val:'0011',min:'0',max:'360',grp:'g1'}, {typ:'Rng',lbl:'Saturation',val:'0012',min:'0',max:'360',grp:'g1'},{typ:'Rad',lbl:'H',val:'0007', grp:'g3'},{typ:'Rad',lbl:'S',val:'0007', grp:'g3'},{typ:'Rad',lbl:'V',val:'0007', grp:'g2'}"
	renderPage(o)
}
function showHTML() {
	var show = document.getElementById("show");
	var mainDiv = document.getElementById("control");
	show.value = mainDiv.innerHTML;
}