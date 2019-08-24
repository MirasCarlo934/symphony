var prevMenuId;
var deltaLeft=0;
var deltaTop=0;
var ruleCount=0;
var rules = new Object();  //a vector object where the key is the ruleId.  Ex if ruleId="rule0", we can get the rule object by rules["rule0"]
//var rules = [];//contains [argDevices[], ctrlDevices[]]

//var ajax = new XMLHttpRequest();
//ajax.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
//ajax.onreadystatechange = function() {
//    if (ajax.readyState == 4) {
//    	alert(ajax.responseText);
//        document.getElementById('response').innerHTML = ajax.responseText;        
//    }
//}
function msg(ito) {
	alert(ito.id+ " clicked parent:" +ito.parentElement.id)
}

function test1() {
	var rule = rules["rule0"];
	var ruleDevices = rule.devices;
	var theDevice = ruleDevices["rule0_arg_SMVX"];
alert(theDevice.name);
	theDevice.name = "Changed";
	theDevice.properties[0].included = true;
alert(theDevice.name);
}
function test2() {
//	alert("test2")
	alert(testMsg())
}
function testMsg() {
	return "message"
}
function test3() {
	
	for (var key in rules) {
		var rule = rules[key];
//alert("1 rule:"+key+" name:"+rule.name);
		var ruleDevices = rule.devices;
alert(rule.getString);
		for (var devKey in ruleDevices) {
//alert("2 rule:"+key+" name:"+rule.name+ " device:"+devKey);
			var theDevice = ruleDevices[devKey];
//			alert(theDevice.getString);
//alert("3 rule:"+key+" name:"+rule.name+ " device:"+devKey+ " name:"+theDevice.name)
//			prop = theDevice.properties;
			for(var i = 0; i < prop.length; i++){				
//				alert("rule:"+key+" name:"+rule.name+ "\n device:"+devKey+ " name:"+theDevice.name+"\n prop id:"+prop[i].id+" "+prop[i].label + " "+prop[i].included);
//				alert(testMsg())
//alert(prop[i].getString);
				/*if (prop[i].included == undefined) {
					alert("undefined");
					prop[i].included = true;
				} else
					alert("OK")*/
			}
		}
	}
}
function hoverSubMenu(ito) {
	if (prevMenuId!=null) {
		var prevDiv = document.getElementById(prevMenuId);
//		alert("prevDiv:"+prevDiv.id);
		prevDiv.style.height = "31px";
		prevDiv.style.backgroundColor = "gray";
	}		
	//get the parent <div> element
	var div = ito.parentElement;
	//set the prevMenuId as myself, this will be removed by the other menu later
	prevMenuId = div.id;
	//get all the <a> elements of the parent, we will compute the height of the div based on these
	var a = div.getElementsByTagName("A");
	var height = 0;
	for(var i = 0; i < a.length; i++){
		var rect = a[i].getBoundingClientRect();
//		alert("height:" + rect.height + " style.Height:"+a[i].style.height);
		height = height + Math.round(rect.height) + 1;
	}
	//alert("total height:" + height);
	div.style.height = 2+height+"px";
	//div.style.backgroundColor = "white";
}
/*
 * Arranges the device properties
 * 	isCollapsed=true = will display first the included 
 */
function arrangeProps(ito, props) {
	row = 0;
	for(var i = 0; i < props.length; i++){
		//we will first loop on the included properties		
		var p = document.getElementById(ito.id+"_"+props[i].id);
//alert("include prop.id:"+ito.id+"_"+props[i].id+" label:"+props[i].label+" row:"+row+ "\n "+p.included);
		if (p.included==true) {
			//p.setAttribute("style","display:block");
			var rect = p.getBoundingClientRect();
			theTop = Math.round(rect.height)*row+ parseInt(ito.getAttribute('headerHeight'));
			p.style.top = theTop+"px";
			row++;
			//p.setAttribute("style","visibility:visible");	
		}
	}
	for(var i = 0; i < props.length; i++){
		//we will first loop on the included properties		
		var p = document.getElementById(ito.id+"_"+props[i].id);
		if (p.included!=true) {
//alert("exclude prop.id:"+ito.id+"_"+props[i].id+" label:"+props[i].label+" row:"+row);
			//p.setAttribute("style","display:block");
			var rect = p.getBoundingClientRect();
			theTop = Math.round(rect.height)*row+ parseInt(ito.getAttribute('headerHeight'));
			p.style.top = theTop+"px";
			row++;
			//p.setAttribute("style","visibility:visible");	
		}
	}
}
function hoverDevice(ito) {
	unhoverDevicePanel(ito);
	ito.style.height = ito.getAttribute('maxHeight');
	var split = ito.id.split("_");
	var ruleId = split[0];
//alert(" ruleId:"+ruleId+" deviceId:"+ito.id)
	var device = rules[ruleId].devices[ito.id];
	var prop = device.properties;
//alert(" ruleId:"+ruleId+" deviceId:"+ito.id+" length:"+prop.length)
	arrangeProps(ito, prop);
}
function hoverDevice_old(ito) {
	
	ito.style.height = ito.getAttribute('maxHeight');
	var split = ito.id.split("_");
	var ruleId = split[0];
//alert(" ruleId:"+ruleId+" deviceId:"+ito.id)
	var device = rules[ruleId].devices[ito.id];
	var prop = device.properties;
//alert(" ruleId:"+ruleId+" deviceId:"+ito.id+" length:"+prop.length)
var ta = document.getElementById("ta");
var str = "";
	row = 0;
	for(var i = 0; i < prop.length; i++){
		//we will first loop on the included properties		
		var p = document.getElementById(ito.id+"_"+prop[i].id);
		if (prop[i].included==true) {
			//p.setAttribute("style","display:block");
			var rect = p.getBoundingClientRect();
			theTop = Math.round(rect.height)*row+ parseInt(ito.getAttribute('headerHeight'));
			str = str + "row:"+row+" theTop:"+theTop+"px\n"
			p.style.top = theTop+"px";
			row++;
			//p.setAttribute("style","visibility:visible");	
		}
	}
	for(var i = 0; i < prop.length; i++){
		//we will first loop on the included properties		
		var p = document.getElementById(ito.id+"_"+prop[i].id);
		if (prop[i].included!=true) {
			//p.setAttribute("style","display:block");
			var rect = p.getBoundingClientRect();
			theTop = Math.round(rect.height)*row+ parseInt(ito.getAttribute('headerHeight'));
			str = str + "row:"+row+" theTop:"+theTop+"px\n"
			p.style.top = theTop+"px";
			row++;
			//p.setAttribute("style","visibility:visible");	
		}
	}
//	ta.innerHTML = str;
}
function unhoverDevice(ito) {
	var ta = document.getElementById("ta");
	var str = "";
		var split = ito.id.split("_");
		var ruleId = split[0];
	str = str + "1 ruleId:"+ ruleId;
		var device = rules[ruleId].devices[ito.id];
	str = str + "\n2 deviceId:"+ ito.id;
		var prop = device.properties;
	str = str + "\n3 "+ ito.id;
		var height= 0;
		var row = 0;
		for(var i = 0; i < prop.length; i++){
			str = str+"<br>prop.id"+prop[i].id+" label:"+prop[i].label+" include:"+prop[i].included
//alert(ito.id+"_"+prop[i].id)
			var p = document.getElementById(ito.id+"_"+prop[i].id);
//alert(p.innerHTML)
			var rect = p.getBoundingClientRect();
			if (p.included==true) {
				str = str+" true";
				height = height + Math.round(rect.height);
				p.style.top = Math.round(rect.height)*row+parseInt(ito.getAttribute('headerHeight'))+"px";
				row++;
			} else {
				str = str+" hidden";
				//p.setAttribute("style","display:none");
				var rect = ito.getBoundingClientRect();
				p.style.top = rect.height+"px";
				//p.setAttribute("style","visibility:hidden");
				//p.style.height = "0px";
				//p.style.width = "1px";
			}
		}		
		var hSplit = ito.getAttribute('collapsedHeight').split("p")[0];
		height =  parseInt(hSplit) + height;
		ito.style.height = height+"px";
		ito.style.backgroundColor = "gray";
//	ta.innerHTML=str+ "\n4 unhoverDevice> ito:"+ito.id+" rule:"+ruleId+" device:"+device.name +" height:"+height
	}
function unhoverDevicePanel(ito) {
	var ta = document.getElementById("ta");
	var str = "ito:" + ito.id;
//	ta.innerHTML = str;
	ruleId = ito.id.split("_");
	str = str+ "\nrule:" + ruleId[0];
//	ta.innerHTML = str;
	var devices = rules[ruleId[0]].devices;
//	ta.innerHTML = str+"\n"+rules[ruleId[0]].devices;
	var arrayLength =  devices;
	for (var key in devices) {
		str = str+"\n\t"+devices[key].id+" "+devices[key].name;
//alert("key:"+key+" device:"+devices[key].name)
//		ta.innerHTML = str;
		var div = document.getElementById(devices[key].id);
	}
}
function unhoverSubMenu(ito) {	
	var div = ito.parentElement;
	div.style.height = "31px";
	div.style.backgroundColor = "gray";
}
function unhoverDivMenu(ito) {
	ito.style.height = "31px";
	ito.style.backgroundColor = "gray";
}

/************************* below are for the device functions *****************************
 * 
 */

/**
The Device object
  creates a <div> to represent this device
  id is the id of this element
  name is the display name
  proerties is an array of properties for this device
  
*/
function Device(id, name, properties) {
  this.id = id;
  this.name = name;
  this.propertyCount = 1;
  this.properties = [];
  var prop = properties;
  for(var i = 0; i < prop.length; i++){
	  //we are doing this because the parameter properties is passed by reference, we want a new instance for each new device
	  var property = new Object();
	  property.id = prop[i].id;
	  property.label = prop[i].label;
	  property.component = this.id;
	  property.value = "1";
	  property.operator = "=";
  	  Object.defineProperties(property, {
	    'getString': { get: propGetter }
  	  });
	  this.properties[i]=property;
  }  
  this.height = 0;
  this.container = ""; //will be set during the create function
  this.rule = "";//will be set during the create function

  this.create = function(index, containerId) {
	  	this.container = containerId;
    	var composer=document.getElementById("composer");
    	var container=document.getElementById(containerId);
    	this.rule = container.parentElement.id; 
//alert("container:"+container.id + " this.id:"+this.id);
    	var div=document.getElementById(this.id);
    	if (div==null) {
    		div = document.createElement('div');
        	div.setAttribute('class', "device");
        	div.setAttribute('id', this.id);
        	div.setAttribute('dIndex', index);
//        	div.setAttribute('draggable', "true");
//        	div.setAttribute('ondragstart',"drag(event)");
        	div.setAttribute('category', "device");
        	var p = document.createElement('p');
        	p.setAttribute('class', "pHeader");
        	p.setAttribute('id', "header");
        	p.innerHTML="<center>"+name+"</center>";
        	div.setAttribute("onmouseover","hoverDevice(this)");
        	div.setAttribute("onmouseout","unhoverDevice(this)");
      	
        	div.appendChild(p);
        	container.append(div);
	        var rect = p.getBoundingClientRect();
			if(containerId!="composer") {
				div.style.width="100%";
			  	var initHeight = this.height + Math.round(rect.height) + 1;
				div.style.height = initHeight +"px";
			}
			this.height = this.height + Math.round(rect.height) + 1;
			div.setAttribute('collapsedHeight', this.height+"px");
			div.setAttribute('headerHeight', Math.round(rect.height)+1);
    	} else {//do nothing 
    		//alert("found "+this.id)
    	}
    	      	
  }
  this.addProperty = function(index, prop) {
	    var div=document.getElementById(this.id);
	    var p = document.getElementById(this.id+"."+prop.id);
	    if (p==null) {
//alert("prop:"+prop.id + " this.id:"+this.id);
	    	p = document.createElement('p');
//	    	p.setAttribute('class', "pHover pCommon");
	    	p.setAttribute('class', "pBkGrnd pCommon");
	    	p.setAttribute('id', this.id+"_"+prop.id);
//alert(this.id+"_"+prop.id)
	    	p.setAttribute('label', prop.label);
	    	p.included = false;
	    	p.index=index;
	    	p.setAttribute('style', 'width:95%');
	    	tdBtn2 = document.createElement('td');
	    	tdBtn2.setAttribute('id', p.id+"_1");
	    	tdBtn2.setAttribute('class', "pHover btn");
	    	tdBtn2.innerHTML = "+";
	    	tdBtn2.index=index;
	    	//tdBtn2.setAttribute('onclick', "markProp(this,"+index+",'"+this.rule+"','"+this.id+"','"+p.id+"')");
	    	tdBtn2.setAttribute('onclick', "markProp(this, false);var input = document.getElementById(propId+'_input');input.select();");
	    	tdLbl = document.createElement('td');
	    	tdLbl.setAttribute('class', "pBkGrnd");
	    	tdLbl.setAttribute('style', "border-radius: 10px;");	    	
//	    	tdLbl.setAttribute('onclick', "clickArg('"+p.id+"', this)");
	    	tdLbl.innerHTML = prop.label
	    	tdLbl.lbl = prop.label
	    	tdLbl.setAttribute('id', p.id+"_lbl");
	    	tdOpt = document.createElement('td');	    	
	    	tdOpt.setAttribute('id', p.id+"_cond");
	    	tdOpt.style.width="1em";
	    	tdOpt.innerHTML = "=";
	    	this.properties[index].operator="=";
	    	var thePanel = this.id.split("_");	    	
	    	if (thePanel[1] == "arg") {
	    		tdOpt.style.cursor="pointer";
		    	tdOpt.setAttribute("onclick","clickOpt(this)");
		    	tdOpt.setAttribute('class', "inputTheme");
	    	} else {
		    	
	    	}
	    	tdValue = document.createElement('td');
	    	tdValue.setAttribute('class', "inputTheme");
//	    	tdValue.setAttribute('contenteditable', "true");	    	
	    	tdValue.setAttribute('id', p.id+"_val");
	    	tdValue.style.width="3em";
	    	var input = document.createElement('input');
	    	input.setAttribute('id',p.id+"_input");
	    	input.setAttribute('type',"text");
	    	input.setAttribute('style',"width:2em;height:1em");
	    	input.setAttribute('onmouseover',"toggleDrag(this,false)");//this will disable the drag for this element to enable input selection
	    	input.setAttribute('onmouseout',"toggleDrag(this,true)");//this will enable again the drag for this element
	    	input.setAttribute('oninput',"includeProperty(this)");
	    	input.setAttribute('value',"1");
	    	tdValue.append(input);
	    	this.properties[index].value="0";
	    	tr = document.createElement('tr');
	    	tbl = document.createElement('table');
	    	//tbl.setAttribute('class', "pHover pCommon");
	    	tbl.setAttribute('style', 'width:100%');
//20170719	    	tr.append(tdBtn1);
	    	tr.append(tdLbl);
	    	tr.append(tdOpt);
	    	tr.append(tdValue);
	    	tr.append(tdBtn2);
	    	tbl.append(tr);	
	    	p.append(tbl);
	    	div.appendChild(p);
	    	this.properties[index].element=p;
	    	this.propertyCount++;
	    	var rect = p.getBoundingClientRect();
	    	this.height = this.height + Math.round(rect.height) + 1;
	    } else {//do nothing 
	  	  //alert("exists:"+ this.id + "." + prop.id)
	    }
  }
  this.drawProperties = function() {
    var arrayLength = properties.length;    
    for (var i = 0; i < arrayLength; i++) {
//alert("Device.drawProperties> id:"+properties[i].id+" label:"+properties[i].label);
    	this.addProperty(i, properties[i]);
    }
    document.getElementById(this.id).setAttribute('maxHeight', this.height+"px");
    /*
    if (this.height > 100) {
    	document.getElementById(this.id).style.height = this.height+"px";
    }*/
  }
  this.showAllProperties = function() {
    var div=document.getElementById(this.id);
    
  }
  this.showSelectedProperties = function() {
  }

}

function  propGetter() {
	//<component id="SMVX">
	//  <property id="0006" value="1" operator="="/>
	//</component>
	var str = "";
	if (this.included) {
		var container = this.component.split("_");
		var str = '\n      <component id="'+container[2]+'">';
		str = str + '\n        <property id="' + this.id + ' value="' + this.value;
		if (container[1]=="arg") {
			str = str + '" operator="' + this.operator;
			str = str.replace(/&gt;/g, '>');
			str = str.replace(/&lt;/g, '<');
		}
		str = str + '"/>\n      </component>';	
	}
	return  str;
}
function  deviceGetter() {
	//<arguments>
	// properties.getString
	//</arguments>
	var container = this.container.split("_");
	var tag = "arguments";
	if (container[1] == "ctrl") {
		tag = "execution";	
	}
	var str = '    <'+tag+'>';
	for(var i = 0; i < this.properties.length; i++) {
		
		str = str + this.properties[i].getString;
	}
	str = str + '\n    </'+tag+'>\n';		
	return  str;
}
/*
  The Rule object
*/
function Rule(id, name) {
  this.id = id;
  this.name = name;
  this.deviceArgs = new Object();
  this.deviceCtrls = new Object();
  this.devices = new Object();
  this.create = function() {
	var composer=document.getElementById("composer");
	var div = document.createElement('div');
	div.setAttribute('draggable', "true");
	div.setAttribute('ondragstart',"drag(event)");
	//div.setAttribute('title',name);
	div.setAttribute('condition',"IF");
	div.setAttribute('class', "rule");
	div.setAttribute('id', id);
	div.setAttribute('category', "RULE");
	div.setAttribute('ruleCount', ruleCount);
//alert("name:"+name+" count:"+ruleCount)
	var p = document.createElement('p');
	p.setAttribute('class', "pHeader");
	p.setAttribute('id', "header");
	p.setAttribute('onclick',"clickRuleName(event)");
	p.innerHTML="<center>"+id+"</center>";
	p.style.cursor = "pointer";
	div.append(p);
	var divArg = document.createElement('div');
	divArg.setAttribute('id', id+"_arg");
	divArg.setAttribute('class', "innerArg");
	divArg.setAttribute('style', "float:left");
	divArg.innerHTML = "<center>Argument</center>";
	divArg.setAttribute('ondragover', "allowDrop(event)");
	divArg.setAttribute('ondrop', "dropArgument(event)");
	divArg.setAttribute('category', "ARG");
//	divArg.setAttribute("onmouseout","unhoverDevicePanel(this)");
	div.append(divArg);
	var divCtrl = document.createElement('div');
	divCtrl.setAttribute('id', id+"_ctrl");
	divCtrl.setAttribute('class', "innerArg");
	divCtrl.setAttribute('style', "float:right");
	divCtrl.innerHTML = "<center>Control<center>";
	divCtrl.setAttribute('ondragover', "allowDrop(event)");
	divCtrl.setAttribute('ondrop', "dropExecution(event)");
	divCtrl.setAttribute('category', "CTRL");
	div.append(divCtrl);
	div.style.left = "250px"; //we place the Rule element always at this fixed location
	composer.append(div);
  }
  this.addDevice = function(d) {
	  Object.defineProperties(d, {
			'getString': { get: deviceGetter }
	  });
	  this.devices[d.id] = d;
  }
}
function  ruleGetter() {
//  <rule name="PIR Switch ON 1" condition="IF">
//	device.getString
//  </rule>
	var str = '  <rule name="'+this.name+'" condition="IF">\n';
	for (var devKey in this.devices) {
		var theDevice = this.devices[devKey];
		str = str + theDevice.getString;
	}
	str = str + "  </rule>";
	return  str;
//	return "rule"
}
function toggleDrag(ito, isEnabled) {
	var ta = document.getElementById("ta");
	ta.innerHTML = "id:" + ito.id +" isEnabled:"+ isEnabled;
	var eId = ito.id.split("_");
	var e = document.getElementById(eId[0])
//	ta.innerHTML = ta.innerHTML + "\n" + e.getAttribute("draggable")
	e.setAttribute("draggable", isEnabled);
}
function drag(e) {
    e.dataTransfer.setData("text", e.target.id);
    var pSrc = e.target;    
    var rectSrc = pSrc.getBoundingClientRect();
    deltaLeft = e.clientX - rectSrc.left;
    //var ta = document.getElementById("composer");
    //ta.innerHTML="id="+pSrc.id+" x:" +e.clientX+ " deltaleft="+deltaLeft+" y:" +e.clientY+" deltaTop="+deltaTop + " rect.left:" + rectSrc.left
    
}

function allowDrop(evt) {
	evt.preventDefault();
}
/*
 * Function called when a device is dropped into a rule pane
 */
function showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, isNewRule) {
//alert("showDroppedDevice> rule.id:"+rule.id)
	if (isNewRule) 
		var str = "_arg_";
	else {
		if (tCategory == "ARG") {
			var str = "_arg_";
		}else if (tCategory == "CTRL") {
			var str = "_ctrl_";
		}
	}	

	if (category=="snsrMenuItem") {
		var device = new Device(rule.id+str+sensors[i].id,sensors[i].name,sensors[i].properties);
//		Object.defineProperties(device, {
//			'getString': { get: deviceGetter }
//		});
	}
	if (category=="ctrlMenuItem") {
		var device = new Device(rule.id+str+controllers[i].id,controllers[i].name,controllers[i].properties);
//		Object.defineProperties(device, {
//			'getString': { get: deviceGetter }
//		});
	}	
	rule.addDevice(device);
//alert("showDroppedDevice> adding deviceId:"+device.id+" to ruleId:"+rule.id+" in Vector");
	if (isNewRule) {
		var ruleObj = document.getElementById(rule.id);
		ruleObj.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
		ruleObj.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
		//rule.addArgDevice(device);		
		device.create(i, rule.id+"_arg");		
//alert("showDroppedDevice> new ruleObj.id:"+ruleObj.id)
	} else {
//alert("showDroppedDevice> existing rule.id:"+rule.id+" tCategory:"+tCategory);

		if (tCategory == "ARG") {
			//rule.addArgDevice(device);
			device.create(i, rule.id+"_arg");
		}else if (tCategory == "CTRL") {
			device.create(i, rule.id+"_ctrl");
		}
		var ruleObj = document.getElementById(rule.id).parentElement;
//alert("existing ruleObj.id:"+ruleObj.id + " ruleCount:"+ruleObj.getAttribute("ruleCount"))		
	}
	device.drawProperties();
}
/*
 * Function called when an item is dropped
 */
function dropDevice(evt) {
	evt.preventDefault();
	var targetId = evt.target.id
	var target = document.getElementById(evt.target.id);
	var tCategory = target.getAttribute("category")
	var data = evt.dataTransfer.getData("text");
	var dropped = document.getElementById(data);
	var category = dropped.getAttribute("category");
	var i = dropped.getAttribute("dIndex");
	var rectTarget = document.getElementById("composer").getBoundingClientRect();
//alert("dropDevice> targetId:"+targetId+" target.category:"+ tCategory+" category:"+category);
	if (evt.target.getAttribute("category")=="CMPSR"&&(category=="snsrMenuItem"||category=="ctrlMenuItem")) {
		//we will only create a new rule if the target is the "compose" pane and item dropped is ctrlMenuItem or snsrMenuItem
		var rule = createRule();
		
		showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, true);
	} else {
		//device is dropped into an existing rule
		
		if (category=="ctrlMenuItem"||category=="snsrMenuItem") {
			//ctrlMenuItem or snsrMenuItem is dropped into the existing rule
			
//alert("dropDevice> dropped.id: "+dropped.id+ "category:"+category +" into:" + evt.target.id);
			//showDroppedDevice(category, i, evt.target.id, evt, rectTarget, false);
//alert("dropDevice> evt.target.parentElement.id:"+evt.target.parentElement.id)
			var rule = rules[evt.target.parentElement.id];
//alert("dropDevice> rule.id:"+rule.id)
			showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, false);
		} else {
			
		}
		if (category=="device"||category=="RULE") {
			//var device = controllers;
			var rectTarget = document.getElementById("composer").getBoundingClientRect();
			dropped.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
			dropped.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
		}
	}

//alert("index:" + i+"\ntarget:\n\tid:"+targetId+"\n\tcategory:"+evt.target.getAttribute("category")+"\ndropped:\n\tid:"+data + "\n\tnodeName:" + dropped.nodeName + "\n\tcategory:" + category)
//    alert("target.id:"+evt.target.id + " top:" +rectTarget.top +" left:" +rectTarget.left)
}
function createRule() {
//alert("creating rule")
	var rule = new Rule('rule'+ruleCount,'Rule'+ruleCount);
	  Object.defineProperties(rule, {
			'getString': { get: ruleGetter }
	  });
	rules[rule.id] = rule;
	rule.create();
	ruleCount++;
	return rule;
}


/*
 * Includes the property in the rule evaluation
 */
function markProp(ito, noToggle) {
	var str = ito.id.split("_");
	ruleId=str[0]
	deviceId=str[0]+"_"+str[1]+"_"+str[2]
	propId = deviceId+"_"+str[3]
//alert(" ruleId:"+ruleId+ "\n deviceId:"+deviceId+"\n propId:"+propId+"\n ito.id:"+ito.id);
//alert("markProp> ruleId:"+ruleId);
	var rule = rules[ruleId];
//alert("markProp> ruleId:"+rule.id+ " deviceId:"+deviceId);
	var ruleDevices = rule.devices;
	var device = ruleDevices[deviceId];
	var p = document.getElementById(propId);
	pObj = device.properties[p.index];
//alert("markProp> Device is "+ device.name+ " pId:"+p.id+ " index:"+p.index+" pObj.Label:"+pObj.label);
	pObj.value = document.getElementById(propId+"_input").value;
	pObj.operator = document.getElementById(propId+"_cond").innerHTML;
//alert("pObj.value:"+pObj.value+"pObj.cond:"+pObj.operator)
//alert("markProp> propId:"+ propId+" label:" + device.properties[index].label+ " of device "+deviceId+ " "+ device.properties[index].included);
	var toggle = true;
	if (p.included==true)
		toggle = false;
	if (noToggle)
		toggle = true;
	if (toggle==true) {
//alert("markProp> Include");
		p.included = true;
		pObj.included = true;
//alert("markProp> p:"+p+" p.id:"+p.id+"\n"+p.innerHTML);
		ito.setAttribute("style","background-color:green");		
//		var btn0=document.getElementById(propId+"_0");
//		btn0.removeAttribute("style");
	} else {
//alert("markProp> Remove");
		p.included = false;
		pObj.included = false;
		ito.removeAttribute("style");
//		var btn1=document.getElementById(propId+"_1");
//		btn1.removeAttribute("style");
	}	
	
}

function init() {
    var arrayLength = sensors.length;
    var menu=document.getElementById("sensorsMenuId");
    for (var i = 0; i < arrayLength; i++) {
      var a = document.createElement('a');
      a.setAttribute('draggable', "true");
      a.setAttribute('ondragstart',"drag(event)");
      a.setAttribute('dIndex',i);
      a.setAttribute('category',"snsrMenuItem");
      a.setAttribute('id',"sensorMenu"+i);
      a.setAttribute('class',"submenu");      
      //a.style.cursor = "pointer";
      a.innerHTML=sensors[i].name;
      menu.appendChild(a);
    }
    var arrayLength = controllers.length;
    var menu=document.getElementById("ctrlsMenuId");
    for (var i = 0; i < arrayLength; i++) {
      var a = document.createElement('a');
      a.setAttribute('draggable', "true");
      a.setAttribute('ondragstart',"drag(event)");
      a.setAttribute('dIndex',i);
      a.setAttribute('category',"ctrlMenuItem");
      a.setAttribute('id',"ctrlMenu"+i);
      a.setAttribute('class',"submenu");
      a.innerHTML=controllers[i].name;
      menu.appendChild(a);
    }
  }

function clickOpt(ito) {
	  var div = document.getElementById("optCond");
	  //var td = document.getElementById(target.id+"_lbl");
	  var device = ito.id.split("_");
	  var deviceId = device[0]+"_"+device[1]+"_"+device[2];
	  var divDevice = document.getElementById(deviceId);
//alert(" parentId:"+divDevice.id);
	  divDevice.appendChild(div);
	  div.style.display = "block";
	  var rect1 = ito.getBoundingClientRect();
	  var rect2 = divDevice.getBoundingClientRect();
//	  if (td.getAttribute('operator')!=null)
//		  document.getElementById("operator").value = td.getAttribute('operator');
	  div.setAttribute('targetId', ito.id); //we are setting this so that the setOperator function can set the innerHTML of the clicked <p> element
//alert("targetId:"+ito.id)
	  div.style.left = Math.round(rect1.right)-Math.round(rect2.left)+"px";
	  div.style.top = 10+Math.round(rect1.top)-Math.round(rect2.top)-Math.round(rect1.height)+"px";
	  var input = document.getElementById(propId+"_input");
	  input.select();
}
function setOperator(ito){
	var div = document.getElementById("optCond");
	var targetId = div.getAttribute('targetId');
	var td = document.getElementById(targetId);
//alert("targetId:"+td.id)
	var device = targetId.split("_");
	var btnId = device[0]+"_"+device[1]+"_"+device[2] + "_" + device[3]+"_1";
	td.innerHTML = ito.innerHTML;
	td.operator = ito.innerHTML;
	div.style.display = "none";
	markProp(document.getElementById(btnId), true);
	var valId = device[0]+"_"+device[1]+"_"+device[2] + "_" + device[3]+"_val";
	var val = document.getElementById(valId);
	var input = document.getElementById(propId+"_input");
	input.select();
//alert(val.innerHTML)
}

function includeProperty(ito) {
	var device = ito.id.split("_");
	var btnId = device[0]+"_"+device[1]+"_"+device[2] + "_" + device[3]+"_1";
	markProp(document.getElementById(btnId), true);
}

function compose() {
	var str = "<rules>\n";
	for (var key in rules) {
		var rule = rules[key];
		var ruleDevices = rule.devices;
		str = str + rule.getString;
	}
	str = str +"\n</rules>";
	alert(str);
	$.get("/devices/composeCIR", "cir="+str, function(data, status) {
		$("#composer").html(data);
	});
}
