var prevMenuId;
var deltaLeft=0;
var deltaTop=0;
var ruleCount=0;
var rules = new Object();  //a vector object where the key is the ruleId.  Ex if ruleId="rule0", we can get the rule object by rules["rule0"]
//var rules = [];//contains [argDevices[], ctrlDevices[]]

function includeProp(propId,deviceId,isInclude) {
	var deviceObj = document.getElementById(deviceId);
	var parentObj = document.getElementById(propId).parentNode;
	var ruleObj = document.getElementById(parentObj.id);
alert("parentId:"+parentObj.id+ " ruleId:"+ruleObj.id+" ruleCount:"+ruleObj.getAttribute("ruleCount"));
ruleArgDevices = rules[ruleObj.getAttribute("ruleCount")].deviceArgs;
	var device = devices[deviceObj.getAttribute("dIndex")];
alert("Device is "+ device.name);
	if (isInclude) {
alert("Include "+ propId+" of device "+deviceId);
	} else {
alert("Remove "+ propId+" of device "+deviceId);
	}
}
function msg(ito) {
	alert(ito.id+ " clicked parent:" +ito.parentElement.id)
}
function test1() {
	var rule = new Rule('rule'+ruleCount,'Rule'+ruleCount);
    	rule.create();
    	ruleCount++;	
}
function test2() {
	i = 0
	sensors[i].create(0,"composer");
	//sensors[i].addProperty(1,sensors[i].properties[0])
	sensors[i].drawProperties();
}
function test3() {
	/*var rules = new Object();
	//var d1 = new Device("SMVX","Device1",[{id:"0006",label:"On"},{id:"0007",label:"Off"}]);
	//var d2 = new Device("ABCD","Device2",[{id:"0006",label:"On"},{id:"0007",label:"Off"}]);
	var rule0 = new Rule("rule0", "name of rule0");
	//rule0.deviceArgs = new Object();
	rule0.addArgDevice(d1);
	rule0.addArgDevice(d2);
	//rule0.deviceCtrls = new Object();
	rule0.addCtrlDevice(d3);
	rule0.addCtrlDevice(d4);
	rules["rule0"] = rule0;
	var rule1 = new Rule("rule1", "name of rule1");
	//rule1.deviceArgs = new Object();
	rule1.addArgDevice(d5);
	rule1.addArgDevice(d6);
	//rule1.deviceCtrls = new Object();
	rule1.addCtrlDevice(d7);
	rules["rule1"] = rule1;*/
	
	for (var key in rules) {
		var rule = rules[key];
//alert("1 rule:"+key+" name:"+rule.name);
		deviceArgs = rule.deviceArgs;
		for (var devKey in deviceArgs) {
//alert("2 rule:"+key+" name:"+rule.name+ " device:"+devKey);
			var theDevice = deviceArgs[devKey];
//alert("3 rule:"+key+" name:"+rule.name+ " device:"+devKey+ " name:"+theDevice.name)
			prop = theDevice.properties;
			for(var i = 0; i < prop.length; i++){				
				alert("rule:"+key+" name:"+rule.name+ " device:"+devKey+ " name:"+theDevice.name+"\nprop id:"+prop[i].id+" "+prop[i].label + " "+prop[i].included);
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
function hoverDevice(ito) {
	//alert(" id:"+ito.id + " parent:"+ito.parentElement.id);
	var div = ito.parentElement;
	//div.style.backgroundColor = "red";
	div.style.height = div.getAttribute('maxHeight');
}
function unhoverDevice(ito) {	
	var div = ito;
	div.style.height = "31px";
	div.style.backgroundColor = "gray";
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
  this.properties = properties;
  this.height = 0;

  this.create = function(index, containerId) {   	
    	var composer=document.getElementById("composer");
	var container=document.getElementById(containerId);
//alert("container:"+container.id + " this.id:"+this.id);
    	var div=document.getElementById(this.id);
    	if (div==null) {
    		div = document.createElement('div');
        	div.setAttribute('class', "device");
        	div.setAttribute('id', this.id);
        	div.setAttribute('dIndex', index);
        	div.setAttribute('draggable', "true");
        	div.setAttribute('category', "device");
        	div.setAttribute('ondragstart',"drag(event)");
        	var p = document.createElement('p');
        	p.setAttribute('class', "pHeader");
        	p.setAttribute('id', "header");
        	p.innerHTML="<center>"+name+"</center>";
        	p.setAttribute("onmouseover","hoverDevice(this)");
        	//div.setAttribute("onmouseout","unhoverDevice(this)");        	
        	div.appendChild(p);
        	container.append(div);
	        var rect = p.getBoundingClientRect();
		if(containerId!="composer") {
			div.style.width="100%";
		  	var initHeight = this.height + Math.round(rect.height) + 1;
			div.style.height = initHeight +"px";
		}
		this.height = this.height + Math.round(rect.height) + 1;
    	} else {//do nothing 
    		//alert("found "+this.id)
    	}
    	      	
  }
  this.addProperty = function(prop) {
	    var div=document.getElementById(this.id);
	    var p = document.getElementById(this.id+"."+prop.id);
	    if (p==null) {
//alert("prop:"+prop.id + " this.id:"+this.id);
	    	p = document.createElement('p');
	    	p.setAttribute('class', "pHover pCommon");
	    	p.setAttribute('id', this.id+"."+prop.id);
	    	p.setAttribute('label', prop.label);	    	
	    	//p.setAttribute('draggable', "true");
	    	//p.setAttribute('ondragstart',"drag(event)");
	    	//p.style.cursor = "pointer";
	    	//p.innerHTML="<table class='pCommon pHover' style='width:100%'><tr><td class='pHover btn'>-</td><td>" +prop.label +"</td><td class='pHover btn'>+</td></tr></table>";
	    	tdBtn1 = document.createElement('td');
	    	tdBtn1.setAttribute('class', "pHover btn");
	    	tdBtn1.setAttribute('onclick', "includeProp('"+p.id+"','"+this.id+"',false)");
	    	tdBtn1.innerHTML = "-";
	    	tdBtn2 = document.createElement('td');
	    	tdBtn2.setAttribute('class', "pHover btn");
	    	tdBtn2.innerHTML = "+";
	    	tdBtn2.setAttribute('onclick', "includeProp('"+p.id+"','"+this.id+"',true)");
	    	tdLbl = document.createElement('td');
	    	tdLbl.innerHTML = prop.label;
	    	tr = document.createElement('tr');
	    	tbl = document.createElement('table');
	    	tbl.setAttribute('class', "pHover pCommon");
	    	tbl.setAttribute('style', 'width:100%');
	    	tr.append(tdBtn1);
	    	tr.append(tdLbl);
	    	tr.append(tdBtn2);
	    	tbl.append(tr);	
	    	p.append(tbl);
	    	div.appendChild(p);
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
//alert("id:"+properties[i].id+" label:"+properties[i].label);
    	this.addProperty(properties[i]);      	
    }
    document.getElementById(this.id).setAttribute('maxHeight', this.height+"px");
    /*
    if (this.height > 100) {
    	document.getElementById(this.id).style.height = this.height+"px";
    }*/
  }
}
/*
  The Rule object
*/
function Rule(id, name) {
  this.id = id;
  this.name = name;
  this.deviceArgs = new Object();
  this.deviceCtrls = new Object();
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
	div.append(p);
	var divArg = document.createElement('div');
	divArg.setAttribute('id', id+".arg");
	divArg.setAttribute('class', "innerArg");
	divArg.setAttribute('style', "float:left");
	divArg.innerHTML = "<center>Argument</center>";
	divArg.setAttribute('ondragover', "allowDrop(event)");
	divArg.setAttribute('ondrop', "dropArgument(event)");
	divArg.setAttribute('category', "ARG");
	div.append(divArg);
	var divCtrl = document.createElement('div');
	divCtrl.setAttribute('id', id+".ctrl");
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
  this.addArgDevice = function(device) {
	  this.deviceArgs[device.id] = device;
  }
  this.addCtrlDevice = function(device) {
	  this.deviceCtrls[device.id] = device;
  }
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
function showDroppedDevice(category, i, ruleid, evt, rectTarget, isNewRule) {
	if (isNewRule) 
		var str = ".arg.";
	else
		var str = ".";
	if (category=="snsrMenuItem") {
		var device = new Device(ruleid+str+sensors[i].id,sensors[i].name,sensors[i].properties);
	}
	if (category=="ctrlMenuItem") {
		var device = new Device(ruleid+str+controllers[i].id,controllers[i].name,controllers[i].properties);
	}
	if (isNewRule) {
		var ruleObj = document.getElementById(ruleid);
		ruleObj.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
		ruleObj.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
		device.create(i, ruleid+".arg"); 
		ruleArgDevices = rules[ruleObj.getAttribute("ruleCount")].deviceArgs;
		var index = ruleArgDevices.length;
		ruleArgDevices[index] = device;
		rules[ruleObj.getAttribute("ruleCount")].deviceArgs = ruleArgDevices; 
//alert("ruleObj.id:"+ruleObj.id)
	} else {
		device.create(i, ruleid);
		var ruleObj = document.getElementById(ruleid).parentElement;
//alert("ruleObj.id:"+ruleObj.id + " ruleCount:"+ruleObj.getAttribute("ruleCount"))
//alert(" rule.name:"+rules[ruleObj.getAttribute("ruleCount")].name)
		ruleArgDevices = rules[ruleObj.getAttribute("ruleCount")].deviceArgs;
//alert("deviceArgs.length:"+ruleArgDevices.length)
		var index = ruleArgDevices.length;
		ruleArgDevices[index] = device;
		rules[ruleObj.getAttribute("ruleCount")].deviceArgs = ruleArgDevices;
		for(var j = 0; j < ruleArgDevices.length; j++){
//alert("device name:" + ruleArgDevices[j].name + " id:" + ruleArgDevices[j].id)
			var r = document.getElementById(ruleArgDevices[j].id);
		}
	}
	device.drawProperties();
	//var div = document.getElementById(device.id);
	//div.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
	//div.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
}
/*
 * Function called when an item is dropped
 */
function dropDevice(evt) {
	evt.preventDefault();
	var targetId = evt.target.id
	var data = evt.dataTransfer.getData("text");
	var dropped = document.getElementById(data);
	var category = dropped.getAttribute("category");
	var i = dropped.getAttribute("dIndex");
	var rectTarget = document.getElementById("composer").getBoundingClientRect();

	if (evt.target.getAttribute("category")=="CMPSR"&&(category=="snsrMenuItem"||category=="ctrlMenuItem")) {
		//we will only create a new rule if the target is the "compose" pane and item dropped is ctrlMenuItem or snsrMenuItem
		var rule = createRule();
		
		showDroppedDevice(category, i, rule.id, evt, rectTarget, true);
	} else {
		if (category=="ctrlMenuItem"||category=="snsrMenuItem") {
			//ctrlMenuItem or snsrMenuItem is dropped into the existing rule
alert("dropped.id: "+dropped.id+ "category:"+category +" into:" + evt.target.id);
			showDroppedDevice(category, i, evt.target.id, evt, rectTarget, false);
		} else {
			
		}
		if (category=="device"||category=="RULE") {
			var device = controllers;
			var rectTarget = document.getElementById("composer").getBoundingClientRect();
			dropped.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
			dropped.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
		}
	}

//alert("index:" + i+"\ntarget:\n\tid:"+targetId+"\n\tcategory:"+evt.target.getAttribute("category")+"\ndropped:\n\tid:"+data + "\n\tnodeName:" + dropped.nodeName + "\n\tcategory:" + category)
//    alert("target.id:"+evt.target.id + " top:" +rectTarget.top +" left:" +rectTarget.left)
}
function createRule() {
	var rule = new Rule('rule'+ruleCount,'Rule'+ruleCount);
	rules[rule.id] = rule;
	rule.create();
	ruleCount++;
	return rule;
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