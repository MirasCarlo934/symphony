const CMD_INIT = 1
const CMD_VALUES = 2
const CMD_CLIENT = 10
const CORE_GETDEVICEINFO = 4;
const CORE_TO_CALLBACK = 7;
const CONTROL_DEVICE = 7;
const CORE_START_HEARTBEAT = 8;
const CORE_DO_DISPLAY = 20
const CMD_DEVICE_PIN_CONTROL = 10


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
/*
 * Function that sends reboot command to the device
 */
function rebootDevice() {
	var obj = { core: 30};
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
 * RADIO_CTL 10		//displays a radio button, enabled
 * BUTTON_CTL 20		//displays a checkbox button, enabled
 * SLIDER_CTL 30		//displays a slider button that opens a slider element, enabled
 * RADIO_SNSR 50		//displays a radio button, disabled
 * BUTTON_SNSR 60		//displays a checkbox button, disabled
 * SLIDER_SNSR 70		//displays a slider button that opens a slider element, disabled
 * RADIO_SUBMIT 80		//displays a radio button that (does not immediately send WS data), enabled
 * BUTTON_SUBMIT 81		//displays a checkbox button (does not immediately send WS data), enabled
 * SLIDER_SUBMIT 82		//displays a slider button that opens a slider element (does not immediately send WS data), enabled
 * BUTTON_SUBMIT 83		//displays a true button (submits data from all the _SUBMIT elements), enabled
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
//	alert(xhttp.responseText);
    jsonResponse = JSON.parse(xhttp.responseText);
    itm = jsonResponse["data"];
    name = jsonResponse["name_mac"];
    var splitStr = name.split("_");
    var hiddenName = document.getElementById("hiddenName");
	hiddenName.value = splitStr[0];
	mac = splitStr[1];
	document.getElementById("theName").innerHTML = jsonResponse["name_mac"];
	document.getElementById("title").innerHTML = hiddenName.value +" - Home Symphony";
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
    	fs.setAttribute('id', "fs_" + k);
    	var legend = document.createElement('legend');
    	legend.innerHTML = k;
    	fs.appendChild(legend);
    	
    	cntr.appendChild(fs);
    	for (var j = 0; j < grp[k].ctr; j++) {
    		var p = grp[k].properties[j];
    		if (p.typ == 83) {  //BUTTON_SUBMIT 83
    			//<label class="truebutton">Button1</label><br>
    			var btn = document.createElement('label');
    			btn.setAttribute('class', "truebutton");
    			btn.innerHTML = p.lbl;
    			btn.setAttribute('onclick',"sendSubmit('fs_"+k+"')");
    			fs.appendChild(btn);
    		} else {
    			var lbl = document.createElement('label');
        		lbl.setAttribute('class', "bar barrng");
        		fs.appendChild(lbl);
        		var div1 = document.createElement('div');
        		div1.setAttribute('class', "lbl");
        		div1.innerHTML = p.lbl;
        		var div2 = document.createElement('div');
        		div2.setAttribute('class', "btn btntxt");
        		var input = document.createElement('input');
        		if (p.typ == 10 || p.typ == 20 || p.typ == 50 || p.typ == 60) { //RADIO_CTL = 10, RADIO_SNSR = 50, BUTTON_CTL = 20, BUTTON_SNSR = 60
        			//we only set a send to WS if the element is not part of a SUBMIT group
        			input.setAttribute('onclick',"sendOnOffWs(this)");
        		}
        		input.setAttribute('lbl',p.lbl);
        		console.log("input.setAttribute('lbl',"+input.getAttribute("lbl")+");" +" id="+p.id)
//        		input.setAttribute('id',name+":"+p.val);
        		input.setAttribute('id',p.id);
        		input.setAttribute('hasPin',p.hasPin);
        		input.setAttribute('name', k);
        		if (p.typ == 10 || p.typ == 50 || p.typ == 80) { //RADIO_CTL = 10, RADIO_SNSR = 50, RADIO_SUBMIT = 80   			
        			input.setAttribute('type',"radio");
        			if (p.val == 1)
        				input.checked=true;
        		} else if (p.typ == 30 || p.typ == 70 || p.typ == 82) { //SLIDER_CTL = 30 , SLIDER_SNSR = 70, SLIDER_SUBMIT = 82
            		input.setAttribute('type',"text");
            		input.setAttribute('min',p.min);
            		input.setAttribute('max',p.max);
            		if (p.typ == 82)
            			input.setAttribute('isSubmit', 1);
            		else
            			input.setAttribute('isSubmit', 0);
            		input.setAttribute('onclick',"getRange(this)");
            		input.value = p.val;
            		div2.setAttribute('class', "txt btntxt");
            		div2.setAttribute('id', "lbl_"+p.id+":rng");
            		div2.innerHTML = p.val;
        		} else if (p.typ == 20 || p.typ == 60 || p.typ == 81 || p.typ == 99) {  //BUTTON_CTL = 20, BUTTON_SNSR = 60,  BUTTON_SUBMIT = 81, UNDEF = 99
        			lbl.setAttribute('class', "bar barchkbox");
        			input.setAttribute('type',"checkbox");
        			if (p.val==1)
    	              input.checked=true;
    	            else
    	              input.checked=false;
        		}
        		if (p.typ == 50 || p.typ == 60 || p.typ == 70 ) {	//this is a sensor property
        			input.disabled = true;
        			div2.setAttribute('style','cursor:not-allowed;');
        			lbl.setAttribute('style','opacity:.7;cursor:not-allowed;');
        		}
        		lbl.appendChild(div1);
        		lbl.appendChild(input);
        		lbl.appendChild(div2);	
    		}
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
  var theParent = tdiv.parentNode;
  var rect = theParent.getBoundingClientRect();
  var rangeVal = 0;
  if (tdiv.textContent.length>0) {
    rangeVal = tdiv.textContent;
  }
  var popup = document.getElementById("popup");
  if (popup!=null) {
    popup.remove()
  }
  var divRange = document.createElement('div');
  divRange.style.left = Math.round(rect.right)+"px";
  divRange.style.top = Math.round(rect.top)+"px";
  divRange.setAttribute('class', "init popup");
  divRange.setAttribute('id', "popup");
  divRange.setAttribute('onclick','closeIt()');
  var rngContainer1 = document.createElement('label');
  rngContainer1.setAttribute('class', "rng barrng");
  var rngInput = document.createElement('input');
  rngInput.setAttribute('type',"range");
  rngInput.setAttribute('id',e.id+ "_" +"rng");
  rngInput.setAttribute('parent',e.id);
  rngInput.setAttribute('min',e.getAttribute("min"));
  rngInput.setAttribute('max',e.getAttribute("max"));
  if (!parseInt(e.getAttribute("isSubmit")))		//we do not set a send to WS if the element is part of a SUBMIT group
	  rngInput.setAttribute('onchange',"sendRangeWs(this)");
  rngInput.value = rangeVal;
  rngInput.addEventListener("input", 
	function(e){
	  showRangeValue(rngInput);
	});
  divRange.appendChild(rngContainer1);
  rngContainer1.appendChild(rngInput);
  document.body.appendChild(divRange);
}
/**
 * Shows the value of the slider in the parent label element
 * @param slider
 * @returns
 */
function showRangeValue(slider) {
	var tdiv = document.getElementById("lbl_"+slider.getAttribute("parent")+":rng");
  	tdiv.textContent = slider.value;
  	var input = document.getElementById(slider.getAttribute("parent"));
  	input.value = slider.value;
//  	console.log("input value="+input.value);
}

/**
 * Generic function to send data to server using websocket
 * @param is the command to be sent to the server
 * @param formId is the id of the container form
 * @returns
 */
function sendWSRequestToServer(cmd, formId) {
	var count = document.getElementById(formId).elements.length;
	var jsonRequest = {"core":CONTROL_DEVICE, "cmd":cmd};//we are sending a cmd to the device
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
/**
 * Submits the values of the _SUBMIT elements in the fieldset where the submit button belongs
 * @param e
 * @returns
 */
function sendSubmit(fsId) {
	var jsonResponse = {"core":CONTROL_DEVICE, "cmd":3};// core:7 - this transaction is to control the device	
	jsonResponse["mac"] = mac;
	jsonResponse["cid"] = cid;
	jsonResponse["data"] = [];
	var fs = document.getElementById(fsId);
	var input = fs.getElementsByTagName("input");
	for(i=0;i<input.length;i++){
		jsonResponse["data"][i] = {};
		jsonResponse["data"][i]["ssid"] = input[i].id;
		jsonResponse["data"][i]["lbl"] = input[i].getAttribute("lbl");
		if (input[i].type == "checkbox" || input[i].type == "radio") {
			console.log("id="+ input[i].id + " value="+input[i].checked +" type=" +input[i].type +" lbl=" +input[i].getAttribute("lbl")) ;
			if (input[i].checked) {
				jsonResponse["data"][i]["val"] = 1;
			} else {
				jsonResponse["data"][i]["val"] = 0;
			}			  
		} else {
			console.log("id="+ input[i].id + " value="+input[i].value +" type=" +input[i].type +" lbl=" +input[i].getAttribute("lbl")) ;
			jsonResponse["data"][i]["val"] = input[i].value;
		}
	}
	console.log(JSON.stringify(jsonResponse));
	websocket.send(JSON.stringify(jsonResponse));
	
}
/**
 * Sends the state of the on/off element
 * @param e
 * @returns
 */
function sendOnOffWs(e) {
	var jsonResponse = {"core":CONTROL_DEVICE, "cmd":CMD_DEVICE_PIN_CONTROL};// core:7 - this transaction is to control the device
	
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
/**
 * Sends the value of the slider element
 * @param e
 * @returns
 */
function sendRangeWs(e) {
//  var txt = document.getElementById("txt1");
//  txt.value="range " +e.id+ "=" +e.value;
//	document.getElementById("popup").remove();
  	var tdiv = document.getElementById("lbl_"+e.getAttribute("parent")+":rng");
  	tdiv.textContent = e.value;
  	var jsonResponse = {"core":CONTROL_DEVICE, "cmd":CMD_DEVICE_PIN_CONTROL};// core:7 - this transaction is to control the device
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
  		case CORE_TO_CALLBACK://data from server to the child javascript
  			{
//	  			var msg = document.getElementById("msg");//comment this out later
//	  			msg.innerHTML = JSON.stringify(jsonResponse);//comment this out later
				if (serverResponseHandler!=null) {//serverResponseHandler method can be defined in the child's javascript
					serverResponseHandler(jsonResponse);//pass the jsonResponse to the child's javascript
				}
	  			break;
  			}
  		case CORE_START_HEARTBEAT: 
  			{
// alert("received heartbeat");
	  			wsHeartbeat();
//	  			document.getElementById("msg").innerHTML = "Please refresh page or wait for READY status.";
	  	        document.getElementById("status").innerHTML = "Device Reboot";
	  			break;
	  		}
  		case CORE_DO_DISPLAY://data from device to be displayed in the elements
  			{
  			switch(cmd) {
  		  	 case CMD_VALUES:
  		   		//response from the VALUES
  		//alert(JSON.stringify(jsonResponse));
//				name = document.getElementById("theName").innerHTML;
  		  		name = document.getElementById("hiddenName").value;
//alert("element: name " + name +" mac " +mac +" from json: name_mac " + jsonResponse["name_mac"]);
  		   		if (jsonResponse["name_mac"] == name+'_'+mac) {
  		   			cid = jsonResponse["cid"];
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
	   if ( core != CORE_TO_CALLBACK ) {
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
	//document.getElementById("msg").innerHTML = xhttp.responseText;
	var jsonResponse = JSON.parse(xhttp.responseText);
	document.getElementById("pName").value = jsonResponse.name;
	document.getElementById("pSSID").value = jsonResponse.ssid;
	document.getElementById("pPass").value = jsonResponse.pwd;
	if (jsonResponse.mqttEnabled == 1)
		document.getElementById("mqttEnabled").checked=true;
	else
		document.getElementById("mqttEnabled").checked=false;
	lgnd = document.getElementById("mqttLegend");
	if (jsonResponse.mqttConn == 1) {
		lgnd.innerHTML = "Connected";
		lgnd.setAttribute('style', 'background-color:lightgreen;')		
	} else {
		lgnd.innerHTML = "Disconnected";
		lgnd.setAttribute('style', 'background-color:white;')
	}
	document.getElementById("mqttIp").value = jsonResponse.mqttIp;
	document.getElementById("mqttPort").value = jsonResponse.mqttPort;
	document.getElementById("sTopic").value = jsonResponse.sTopic;
	document.getElementById("pTopic").value = jsonResponse.pTopic;
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