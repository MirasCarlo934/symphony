var prevMenuId;
var deltaLeft=0;
var deltaTop=0;
var ruleCount=0;
function msg(ito) {
	alert(ito.id+ " clicked")
}
function test1() {
	var rule = new Rule('rule'+ruleCount,'Rule'+ruleCount);
    	rule.create();
    	ruleCount++;	
}
function test2() {
	i = 0
	sensors[i].create(0,"composer");
	sensors[i].addProperty(1,sensors[i].properties[0])
	//sensors[i].drawProperties();
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
	alert(" id:"+ito.id);
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
		div.setAttribute("onmouseover","hoverDevice(this)");
        	div.append(p);
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
alert("prop:"+prop.id + " this.id:"+this.id);
	    	p = document.createElement('p');
	    	p.setAttribute('class', "pHover pCommon");
	    	p.setAttribute('id', this.id+"."+prop.id);
	    	p.setAttribute('label', prop.label);
	    	p.setAttribute('onclick', "msg(this)");
	    	//p.setAttribute('draggable', "true");
	    	//p.setAttribute('ondragstart',"drag(event)");
	    	p.style.cursor = "pointer";
	    	p.innerHTML=prop.label;
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
alert("height:"+this.height);
    if (this.height > 100) {
	alert("will adjust height of "+this.id);
    	document.getElementById(this.id).style.height = this.height+"px";
    }
  }
}
/*
  The Rule object
*/
function Rule(id, name) {
  this.id = id;
  this.name = name;
  this.create = function() {
	var composer=document.getElementById("composer");
	var div = document.createElement('div');
	div.setAttribute('draggable', "true");
	div.setAttribute('ondragstart',"drag(event)");
	div.setAttribute('title',name);
	div.setAttribute('condition',"IF");
	div.setAttribute('class', "rule");
	div.setAttribute('id', id);
	div.setAttribute('category', "RULE");
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
	divArg.innerHTML = "Argument";
	divArg.setAttribute('ondragover', "allowDrop(event)");
	divArg.setAttribute('ondrop', "dropArgument(event)");
	divArg.setAttribute('category', "ARG");
	div.append(divArg);
	var divCtrl = document.createElement('div');
	divCtrl.setAttribute('id', id+".ctrl");
	divCtrl.setAttribute('class', "innerArg");
	divCtrl.setAttribute('style', "float:right");
	divCtrl.innerHTML = "Control";
	divCtrl.setAttribute('ondragover', "allowDrop(event)");
	divCtrl.setAttribute('ondrop', "dropExecution(event)");
	divCtrl.setAttribute('category', "CTRL");
	div.append(divCtrl);
	div.style.left = "250px"; //we place the Rule element always at this fixed location
	composer.append(div);
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

function dropDevice(evt) {
	evt.preventDefault();
	var targetId = evt.target.id
	var data = evt.dataTransfer.getData("text");
	var dropped = document.getElementById(data);
	var category = dropped.getAttribute("category");
	var i = dropped.getAttribute("dIndex");
	var rectTarget = document.getElementById("composer").getBoundingClientRect();

	//we will only create a new rule if the target is the "compose" pane and item dropped is ctrlMenuItem or snsrMenuItem
	if (evt.target.getAttribute("category")=="CMPSR"&&(category=="snsrMenuItem"||category=="ctrlMenuItem")) {
		var ruleid = createRule();
		if (category=="snsrMenuItem") {
			//var device = sensors;
			var device = new Device(ruleid+".arg."+sensors[i].id,sensors[i].name,sensors[i].properties);

		}
		if (category=="ctrlMenuItem") {
			//var device = controllers;
			var device = new Device(ruleid+".arg."+controllers[i].id,controllers[i].name,controllers[i].properties);
		}
		var ruleObj = document.getElementById(ruleid);
		ruleObj.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
		ruleObj.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
		device.create(i, ruleid+".arg");
		device.drawProperties();
		//var div = document.getElementById(device.id);
		//div.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
		//div.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";

	} else {
		if (category=="device"||category=="RULE") {
			var device = controllers;
			var rectTarget = document.getElementById("composer").getBoundingClientRect();
			dropped.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
			dropped.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
//alert("changing top and left of "+dropped.id);
		}
	}

//alert("index:" + i+"\ntarget:\n\tid:"+targetId+"\n\tcategory:"+evt.target.getAttribute("category")+"\ndropped:\n\tid:"+data + "\n\tnodeName:" + dropped.nodeName + "\n\tcategory:" + category)
//    alert("target.id:"+evt.target.id + " top:" +rectTarget.top +" left:" +rectTarget.left)
}
function createRule() {
	var rule = new Rule('rule'+ruleCount,'Rule'+ruleCount);
    	rule.create();
    	ruleCount++;
	return rule.id;	
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