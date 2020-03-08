var prevMenuId;
var deltaLeft=0;
var deltaTop=0;
var ruleCount=0;
var selectedRule = "";//the id of the selected rule, this is used to change the z-index of the rules
var rulesForEdit = new Object();  //a vector object where the key is the ruleId.  Ex if ruleId="rule0", we can get the rule object by rules["rule0"]
//var rulesForEdit = [];//contains [argDevices[], ctrlDevices[]]

var ajax = new XMLHttpRequest();
ajax.onreadystatechange = function() {
    if (ajax.readyState == 4) {
    	alert("response from server\n\n"+ajax.responseText);
//        document.getElementById('response').innerHTML = ajax.responseText;
    }
}

function msg(ito) {
	alert(ito.id+ "\n" +ito.innerHTML);
//	var rule  = rulesForEdit[ito.id.split("|")[0]];
//	var device = rule.devices[ito.id];
//	alert(" rule:"+rule.id+" device:"+device.id+" props.length:"+device.properties.length)
}

function test2() {
	
	for (var k in rulesForEdit) {
		var rule = rulesForEdit[k];		
		var deviceId ="rule2_arg_II81"
		var ruleId = deviceId.split("|")[0];
		var ruleToDelete = rulesForEdit[ruleId];
		var device = ruleToDelete.devices[deviceId];
		alert(k+" "+document.getElementById(k).style.zIndex)
//		alert("device.name to delete:"+device.name+" from rule:"+ruleToDelete.name)
//		for (var key in rule.devices) {
//    		if (key!=deviceId) {
//    			var name = rule.devices[key].name
//    			alert("rule:"+rule.name+" device:"+key+" name:"+name+" ADD");	
//    		}    			
//    		else {
//    			var name = rule.devices[key].name
//    			alert("rule:"+rule.name+" device:"+key+" name:"+name+" NO ADD");	
//    		}
//    	}
	}
	
}
function test1() {
	//test jQuery
	$( "#composer" ).hide()
	for (var k=0; k < rules.length;k++) {
		var args = rules[k].args
		for (var j=0; j< args.length;j++) {
			var id = "rule0_arg_"+args[j].id;
			var html = document.getElementById(id).innerHTML;
			alert(rules[k].name+" device.id:"+id+"\n"+html)
			alert($( "#"+id ).html)
			alert("done")
		}
	}
	
}
function test2LoadCIR() {
	var str = "<rules>";
	for (var k=0; k < rules.length;k++) {//rules is set in the html file via thymeleaf		
		var args = rules[k].args
		str = str + "\n"+rules[k].name+" args:"+args.length;
		for (var j=0; j< args.length;j++) {
			str = str + "\narg j:"+j+" arg.id:"+args[j].id;		
			drawRule(args[j].id, k, "sensorMenu");
			var props = args[j].properties;
			for (var i = 0; i < props.length; i++) {
				str = str + "\n          props.i:"+i+" "+props[i].id+" "+props[i].operator+" "+props[i].value;
			}
		}
		var exec = rules[k].execs
		str = str + "\n"+rules[k].name+" exec:"+exec.length;
		for (var j=0; j< exec.length;j++) {
			str = str + "\nexec j:"+j+" id:"+exec[j].id;
			drawRule(exec[j].id, k, "ctrlMenu");
			var props = exec[j].properties;
			for (var i = 0; i < props.length; i++) {
				str = str + "\n          props.i:"+i+" "+props[i].id+" "+props[i].operator+" "+props[i].value;
			}
		}
	}
	str = str +"\n</rules>";
//	alert(str)
	var ta = document.getElementById("ta");
	ta.value = str;
}
function testDOM(str) {
	alert(str);
	let frag = document.createRange().createContextualFragment(str);
	var mainRule = frag.querySelectorAll("rules");
	alert("mainRule."+mainRule.length)
	for (var i = 0; i < mainRule.length; i++) {
		alert("i:"+i+" "+mainRule[i].tagName)
		var rulesList = mainRule[i].childNodes;
		alert("rulesList:"+rulesList.length)
		for (var j = 0; j < rulesList.length; j++) {			
			if (rulesList[j].tagName=="RULE") {
				alert("i:"+i+" j:"+j+" tag:"+rulesList[j].tagName+" name:"+rulesList[j].getAttribute("name")+"\n"+rulesList[j].innerHTML)
				var theRule = rulesList[j].childNodes;
				for (var k = 0; k < theRule.length; k++) {
					if (theRule[k].tagName=="ARGUMENTS"||theRule[k].tagName=="EXECUTION") {
						alert("i:"+i+" j:"+j+" k:"+k+" tag:"+theRule[k].tagName+"\n"+theRule[k].innerHTML)
						var device = theRule[k].childNodes;
						for (var l = 0; l < device.length; l++) {
							alert("i:"+i+" j:"+j+" k:"+k+" l:" +l+ " tag:"+device[l].tagName+"\n"+device[l].innerHTML)
						}
					}					
				}
			}
		}
	}
}
function test3() {
	var ta = document.getElementById("ta");
	ta.value = document.getElementById("rule0_header_edit").innerHTML;

	for (var key in rulesForEdit) {
		var rule = rulesForEdit[key];
//alert("1 rule:"+key+" name:"+rule.name);
		var ruleDevices = rule.devices;
//alert(rule.getString);
		for (var devKey in ruleDevices) {
//alert("2 rule:"+key+" name:"+rule.name+ " device:"+devKey);
			var theDevice = ruleDevices[devKey];
			theDevice.drawProperties();
//			alert(theDevice.getString);
//alert("3 rule:"+key+" name:"+rule.name+ "\n\tdeviceId:"+devKey+ " name:"+theDevice.name)
			prop = theDevice.properties;
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
/*
 * Initializes the menu
 */
function init() {
	var arrayLength = deviceList.length;
    for (var i = 0; i < arrayLength; i++) {
  	  var isSensor = false;
	  var isController = false;
      var props = deviceList[i].properties;
      for (var j=0; j<props.length;j++){
    	  if (props[j].io=="I" && !isSensor) {
    		  isSensor = true;
    	  }
    	  if (props[j].io=="O" && !isController) {
    		  isController = true;
    		  isSensor = true;
    	  }
      }
      var a = document.createElement('a');
      a.setAttribute('draggable', "true");
      a.setAttribute('ondragstart',"drag(event)");
      a.setAttribute('onclick',"msg(this)");
      a.setAttribute('dIndex',i);
      a.setAttribute('class',"submenu");
      a.innerHTML=deviceList[i].name;
      var aCtrl = a.cloneNode(true);
      if (isSensor) {
          a.setAttribute('category',"snsrMenuItem");
          a.setAttribute('id',"sensorMenu"+i);
          $( "#sensorsMenuId" ).append(a);
      }
      if (isController) {
    	  aCtrl.setAttribute('category',"ctrlMenuItem");
    	  aCtrl.setAttribute('id',"ctrlMenu"+i);
    	  $( "#ctrlsMenuId" ).append(aCtrl);
      }      
    }
    loadCIR();
}

function loadCIR() {
var str = "<rules>";
	for (var k=0; k < rules.length;k++) {//rules is set in the html file via thymeleaf, these are the rules currently applied in the CIR
		var args = rules[k].args
str = str + "\nrule.name:"+rules[k].name+" id:"+rules[k].id+" args:"+args.length;
		for (var j=0; j< args.length;j++) {
str = str + "\narg j:"+j+" arg.id:"+args[j].id;		
			var rule = drawRule(args[j].id, k, "sensorMenu");
			var props = args[j].properties;
			for (var i = 0; i < props.length; i++) {
				var divId = rule.id + "|arg|" + args[j].id + "|" + props[i].id;
str = str + "\n          rule.id:"+rule.id+" props.i:"+i+" id:"+divId +" p.id:"+props[i].id+" "+props[i].operator+" "+props[i].value;
				var input = document.getElementById(divId+"|input");
				input.value = props[i].value;
				var btn = document.getElementById(divId+"|1");
				markProp(btn, true);
			}
			rules[k].id = rule.id;
			var theDevice = document.getElementById(rule.id + "|arg|" + args[j].id);
			unhoverDevice(theDevice);	//displays the selected properties
		}

		var exec = rules[k].execs
str = str + "\n"+rules[k].name+" exec:"+exec.length;
		for (var j=0; j< exec.length;j++) {
str = str + "\nexec j:"+j+" id:"+exec[j].id;
			drawRule(exec[j].id, k, "ctrlMenu");
			var props = exec[j].properties;
			for (var i = 0; i < props.length; i++) {
				var divId = rule.id + "|ctrl|" + exec[j].id + "|" + props[i].id;
str = str + "\n          props.i:"+i+" id:"+divId +" p.id:"+props[i].id+" "+props[i].operator+" "+props[i].value;
				var input = document.getElementById(divId+"|input");
				input.value = props[i].value;
				var btn = document.getElementById(divId+"|1");
				markProp(btn, true);
			}
			var theDevice = document.getElementById(rule.id + "|ctrl|" + exec[j].id);
			unhoverDevice(theDevice);	//displays the selected properties
		}
str = str + "\nrule.name:"+rules[k].name+" id:"+rules[k].id+" args:"+args.length+"\n";
		toggleRule(rules[k].id+"|header");  //we display the rules in minimized view
	}
	str = str +"\n</rules>";
//	alert(str)
	var ta = document.getElementById("ta");
	ta.value = str;
}
/*
 * Function called during loading of the cir rules from server
 * data : the device id to be created
 */
function drawRule(data, count, menu) {
	var eRule = document.getElementById("rule"+count);
	if(eRule==null) {
		var tCategory = "CMPSR";
	} else {
		if (menu=="ctrlMenu") {
			var tCategory = "CTRL";
		} else {
			var tCategory = "ARG";
		}
	}		
	for (var key=0; key < deviceList.length; key++) {
		if (data==deviceList[key].id) {
			//we found a device for this data
			var menuItem = menu + key;
			break;
		}
	}
	var dropped = document.getElementById(menuItem);
	if (dropped!=null) {
		var category = dropped.getAttribute("category");
		var i = dropped.getAttribute("dIndex");
		var rectTarget = document.getElementById("composer").getBoundingClientRect();
		var evt = {clientX:0,clientY:0};
		count++;
		evt.clientX = parseInt(rectTarget.left)+count*20;
		evt.clientY = parseInt(rectTarget.top)+count*50;
		if(eRule==null) {
			//no existing rule yet, we will create a new one
			var rule = createRule();
			rule.name=rules[count-1].name;
			$("#"+rule.id+"|header|txt").val(rules[count-1].name);//we are renaming the rule
			showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, true, true);
		} else {
			var rule = rulesForEdit[eRule.id];			
			showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, false, true);
		}
	} else {
		alert(data + " not found in " + menu)
	}
	return rule;
}
function hoverSubMenu(ito) {
	if (prevMenuId!=null) {
		var prevDiv = document.getElementById(prevMenuId);
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
		height = height + Math.round(rect.height) + 1;
	}
	div.style.height = 2+height+"px";
}
/*
 * Arranges the device properties
 * 	isCollapsed=true = will display first the included 
 */
function arrangeProps(ito, props) {
	row = 0;
	for(var i = 0; i < props.length; i++){
		//we will first loop on the included properties		
		var p = document.getElementById(ito.id+"|"+props[i].id);
		if (p.included==true) {
			//p.setAttribute("style","display:block");
			var rect = p.getBoundingClientRect();
			theTop = Math.round(rect.height)*row+ parseInt(ito.getAttribute('headerHeight'));
			p.style.top = theTop+"px";
//alert("ito.id:"+ito.id+" arrangeProps label:"+props[i].label+" top:"+p.style.top+" included:"+p.included+"\nito.id:"+ito.id+" p.id:"+p.id+" p.label:"+document.getElementById(p.id+"|lbl").innerHTML+" top:"+p.style.top+" included:"+p.included);
			row++;
		}
	}
	for(var i = 0; i < props.length; i++){
		//we will first loop on the included properties		
		var p = document.getElementById(ito.id+"|"+props[i].id);
		if (p.included!=true) {
			//p.setAttribute("style","display:block");
			var rect = p.getBoundingClientRect();
			theTop = Math.round(rect.height)*row+ parseInt(ito.getAttribute('headerHeight'));
			p.style.top = theTop+"px";
//alert("ito.id:"+ito.id+" arrangeProps label:"+props[i].label+" top:"+p.style.top+" included:"+p.included+"\nito.id:"+ito.id+" p.id:"+p.id+" p.label:"+document.getElementById(p.id+"|lbl").innerHTML+" top:"+p.style.top+" included:"+p.included);
			row++;
		}
	}
}
function hoverDevice(ito) {
	unhoverDevicePanel(ito);
	ito.style.height = ito.getAttribute('maxHeight');
	var split = ito.id.split("|");
	var ruleId = split[0];
	var device = rulesForEdit[ruleId].devices[ito.id];
	var prop = device.properties;
	arrangeProps(ito, prop);
}
/*
 * this is used to hide the unselected properties of a device
 */
function unhoverDevice(ito) {
		var split = ito.id.split("|");
		var ruleId = split[0];
		var device = rulesForEdit[ruleId].devices[ito.id];
		var prop = device.properties;
		var height= 0;
		var row = 0;
		for(var i = 0; i < prop.length; i++){
			var p = document.getElementById(ito.id+"|"+prop[i].id);
			var rect = p.getBoundingClientRect();
			if (p.included==true) {
				height = height + Math.round(rect.height);
				p.style.top = Math.round(rect.height)*row+parseInt(ito.getAttribute('headerHeight'))+"px";
				row++;
//alert("ito.id:"+ito.id+" p.id:"+p.id+" p.label:"+document.getElementById(p.id+"|lbl").innerHTML+" top:"+p.style.top+" included:"+p.included);
			} else {
				//p.setAttribute("style","display:none");
				var rect = ito.getBoundingClientRect();
//alert("before ito.id:"+ito.id+" d.h:"+device.height+" r.h:"+rect.height+" p.id:"+p.id+" p.label:"+document.getElementById(p.id+"|lbl").innerHTML+" top:"+p.style.top+" included:"+p.included);
//				p.style.top = rect.height+"px";
				p.style.top = (device.height+2)+"px";				
//alert("after ito.id:"+ito.id+" p.id:"+p.id+" p.label:"+document.getElementById(p.id+"|lbl").innerHTML+" top:"+p.style.top+" included:"+p.included);
			}
		}		
		var hSplit = ito.getAttribute('collapsedHeight').split("p")[0];
		height =  parseInt(hSplit) + height;
		ito.style.height = height+"px";
		ito.style.backgroundColor = "gray";
//alert(ito.id+" done unhover")
	}
function unhoverDevicePanel(ito) {
	ruleId = ito.id.split("|");
	var devices = rulesForEdit[ruleId[0]].devices;
	var arrayLength =  devices;
	for (var key in devices) {
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

/*
 * The Component Object
 * This is the Component container from the xml file
 */
function Component(id, properties) {
	this.id = id;
	this.properties = properties;
}
/*
 * The CIRRule Object
 * This is the Rule container from the xml file
 */
function CIRRule(name, args, execs) {
	this.name = name;
	this.args = args;
	this.execs = execs;
}

/**
The Device object
  creates a <div> to represent this device
  id is the id of this element
  name is the display name
  proerties is an array of properties for this device
  
*/
function Device(id, name, room, properties) {
  this.id = id;
  this.name = name;
  this.room = room;
  this.propertyCount = 1;
  this.properties = [];
  var prop = properties;
  var str =  this.id.split("|");
  var ctr = 0;
  for(var i = 0; i < prop.length; i++){
	  //we are doing this because the parameter properties is passed by reference, we want a new instance for each new device
	  if ((str[1]==undefined)||(prop[i].io=="I"&&str[1]=="arg")||(prop[i].io=="O")){
  
		  //we copy the property if str is undefined (this means device is created for the menu) 
		  //or when the io="I" and str="arg"
		  //or when the io="O"
		  var property = new Object();
		  property.id = prop[i].id;
		  var split = prop[i].id.split("_"); //we are splitting the id since it is of the form NAME_0, to get the index (needed by CIRManager of BM)
		  property.index = split[1];
		  property.label = prop[i].label;
		  property.min = prop[i].min;
		  property.io = prop[i].io;
		  property.max = prop[i].max;
		  property.component = this.id;
		  property.value = "1";
		  property.operator = "=";
	  	  Object.defineProperties(property, {
		    'getString': { get: propGetter }
	  	  });
		  this.properties[ctr]=property;
		  ctr++;
	  } else {
	  }
  }
  this.height = 0;
  this.container = ""; //will be set during the create function
  this.rule = "";//will be set during the create function

  this.create = function(index, containerId) {
	  	this.container = containerId;
    	var composer=document.getElementById("composer");
    	var container=document.getElementById(containerId);
    	this.rule = container.parentElement.id; 
    	var div=document.getElementById(this.id);
    	if (div==null) {
    		div = document.createElement('div');
        	div.setAttribute('class', "device");
        	div.setAttribute('id', this.id);
        	div.setAttribute('dIndex', index);
        	div.setAttribute('category', "device");
        	var p = document.createElement('p');
        	p.setAttribute('class', "pHeader");
        	p.setAttribute('id', "header");
//        	p.innerHTML="<center>"+name+"</center>";
        	
        	var tdLbl = document.createElement('td');
        	tdLbl.setAttribute('id', p.id+"|lbl");
        	tdLbl.setAttribute('style', 'text-align: center;');
        	tdLbl.innerHTML=name;
        	var tdBtnRemove = document.createElement('td');
        	tdBtnRemove.setAttribute('id', p.id+"|remove");
        	tdBtnRemove.innerHTML = '<img class="pHover minmax" src="/pics/remove.png" title="Delete device."/>';
        	tdBtnRemove.setAttribute('onclick',"removeDevice('"+id+"')");        	
        	tr = document.createElement('tr');
        	tbl = document.createElement('table');
        	tbl.setAttribute('style', 'width:100%;');
        	tbl.innerHTML = '<col style="width:calc(100% - 60px);"> <col style="width:17px;"> <col style="width:17px;">  <col style="width:17px;">';
        	tr.append(tdLbl);
        	tr.append(tdBtnRemove);        	
        	tbl.append(tr);
        	p.append(tbl);        	
        	
        	div.setAttribute("onmouseover","hoverDevice(this)");
        	div.setAttribute("onmouseout","unhoverDevice(this)");
        	div.setAttribute('draggable', "true");
        	div.setAttribute('ondragstart',"drag(event)");     	
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

    	}
    	      	
  }
  this.addProperty = function(index, prop) {
	    var div=document.getElementById(this.id);
	    var p = document.getElementById(this.id+"|"+prop.id);
	    if (p==null) {
	    	p = document.createElement('p');
	    	p.setAttribute('class', "pBkGrnd pCommon");
	    	p.setAttribute('id', this.id+"|"+prop.id);
	    	p.setAttribute('label', prop.label);
	    	p.included = false;
	    	p.index=index;
	    	p.setAttribute('style', 'width:95%');
	    	tdBtn = document.createElement('td');
	    	tdBtn.setAttribute('id', p.id+"|1");
	    	tdBtn.setAttribute('class', "btn");
	    	tdBtn.innerHTML = "+";
	    	tdBtn.index=index;
	    	tdBtn.setAttribute('onclick', "markProp(this, false);var input = document.getElementById(propId+'_input');input.select();");
//	    	tdBtn2 = document.createElement('label');
//	    	tdBtn2.setAttribute('id', p.id+"|1");
//	    	tdBtn2.setAttribute('class', "pHover btn");
//	    	tdBtn2.innerHTML = "+";
//	    	tdBtn2.index=index;
//	    	tdBtn2.setAttribute('onclick', "markProp(this, false);var input = document.getElementById(propId+'_input');input.select();");
//	    	tdBtn.append(tdBtn2);
	    	
	    	tdLbl = document.createElement('td');
	    	tdLbl.setAttribute('class', "pBkGrnd");
	    	tdLbl.setAttribute('style', "border-radius: 30px;");
	    	tdLbl.innerHTML = prop.label
	    	tdLbl.lbl = prop.label
	    	tdLbl.setAttribute('id', p.id+"|lbl");
	    	tdOpt = document.createElement('td');	    	
	    	tdOpt.setAttribute('id', p.id+"|cond");
	    	tdOpt.style.width="1em";
	    	tdOpt.innerHTML = "=";
	    	this.properties[index].operator="=";
	    	var thePanel = this.id.split("|");	    	
	    	if (thePanel[1] == "arg") {
	    		tdOpt.style.cursor="pointer";
		    	tdOpt.setAttribute("onclick","clickOpt(this)");
		    	tdOpt.setAttribute('class', "inputTheme");
	    	} else {
		    	
	    	}
	    	tdValue = document.createElement('td');
	    	tdValue.setAttribute('class', "inputTheme");
	    	tdValue.setAttribute('id', p.id+"|val");
	    	tdValue.style.width="5em";
	    	var input = document.createElement('input');
	    	input.setAttribute('id',p.id+"|input");
	    	if (this.properties[index].min==undefined || this.properties[index].max==undefined) {
	    		input.setAttribute('type',"text");
	    		input.setAttribute('title',"input text");
	    	} else {
		    	input.setAttribute('type',"number");
		    	input.setAttribute('min', this.properties[index].min);
		    	input.setAttribute('max', this.properties[index].max);
		    	input.setAttribute('title',"min="+this.properties[index].min+"\nmax="+this.properties[index].max);
		    	input.setAttribute('value',this.properties[index].value);
	    	}
	    	input.setAttribute('style',"width:4em;height:1em");
	    	input.setAttribute('onmouseover',"toggleDrag(this,false)");//this will disable the drag for this element to enable input selection
	    	input.setAttribute('onmouseout',"toggleDrag(this,true)");//this will enable again the drag for this element
	    	input.setAttribute('oninput',"includeProperty(this)");

	    	tdValue.append(input);
	    	tr = document.createElement('tr');
	    	tbl = document.createElement('table');
	    	//tbl.setAttribute('class', "pHover pCommon");
	    	tbl.setAttribute('style', 'width:100%;');
	    	tr.append(tdLbl);
	    	tr.append(tdOpt);
	    	tr.append(tdValue);
	    	tr.append(tdBtn);
	    	tbl.append(tr);	
	    	p.append(tbl);
	    	div.appendChild(p);
	    	this.properties[index].element=p;
	    	this.propertyCount++;
	    	var rect = p.getBoundingClientRect();
	    	this.height = this.height + Math.round(rect.height) + 1;
	    } else {//do nothing 

	    }
  }
  this.drawProperties = function() {
    var arrayLength = this.properties.length;
    for (var i = 0; i < arrayLength; i++) {
    	this.addProperty(i, this.properties[i]);
    }
    document.getElementById(this.id).setAttribute('maxHeight', this.height+"px");
  }
  this.showAllProperties = function() {
    var div=document.getElementById(this.id);
    
  }
  this.showSelectedProperties = function() {
  }

}

/*
  The Rule object
*/
function Rule(id, name) {
  this.id = id;
  this.name = name;
//  this.deviceArgs = new Object();
//  this.deviceCtrls = new Object();
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
	var p = document.createElement('p');
	p.setAttribute('class', "pHeader");
	p.setAttribute('id', this.id+"|header");
	p.setAttribute('onmouseover',"selectRuleObj(this)");
	p.style.cursor = "pointer";

	var tdLbl = document.createElement('td');
	tdLbl.setAttribute('id', p.id+"|lbl");
	tdLbl.setAttribute('style', 'text-align: center;');
	txt = document.createElement('input');
	tdLbl.innerHTML="<input type='text' style='width:100%;text-align: center; background-color: lightgray;	border: none;color: black;font-weight: bold;font-family:Verdana, Arial, Helvetica, sans-serif;' value='"+name+"' id='"+p.id+"|txt' disabled>";
	
	var tdEdit = document.createElement('td');
	tdEdit.setAttribute('id', p.id+"|edit");
	tdEdit.innerHTML = '<img class="pHover minmax" src="/pics/edit.svg" title="Edit rule name."/>';
	tdEdit.setAttribute('onclick',"editRule(this)");
	
	var tdBtnMin = document.createElement('td');
	tdBtnMin.setAttribute('id', p.id+"|min");
	tdBtnMin.innerHTML = '<img class="pHover minmax" src="/pics/min.png"  title="Minimize this rule."/>';
	tdBtnMin.setAttribute('onclick',"toggleRule('"+p.id+"')");
	
	var tdBtnRemove = document.createElement('td');
	tdBtnRemove.setAttribute('id', p.id+"|remove");
	tdBtnRemove.innerHTML = '<img class="pHover minmax" src="/pics/remove.png"  title="Delete this rule."/>';
	tdBtnRemove.setAttribute('onclick',"removeRule('"+id+"')");

	
	tr = document.createElement('tr');
	tbl = document.createElement('table');
	tbl.setAttribute('style', 'width:100%;');
	tbl.innerHTML = '<col style="width:calc(100% - 60px);"> <col style="width:17px;"> <col style="width:17px;">  <col style="width:17px;">';
	tr.append(tdLbl);
	tr.append(tdEdit);
	tr.append(tdBtnMin);
	tr.append(tdBtnRemove);
	
	tbl.append(tr);
	p.append(tbl);
	
	div.appendChild(p);
	var divArg = document.createElement('div');
	divArg.setAttribute('id', id+"|arg");
	divArg.setAttribute('class', "innerArg");
	divArg.setAttribute('style', "float:left;width:50%;");
	divArg.innerHTML = "<center>Trigger</center>";
	divArg.setAttribute('ondragover', "allowDrop(event)");
//	divArg.setAttribute('ondrop', "dropArgument(event)");  no need, this is already inherited
	divArg.setAttribute('category', "ARG");
	div.append(divArg);
	var divCtrl = document.createElement('div');
	divCtrl.setAttribute('id', id+"|ctrl");
	divCtrl.setAttribute('class', "innerArg");
	divCtrl.setAttribute('style', "float:right;width:49%;");
	divCtrl.innerHTML = "<center>Action<center>";
	divCtrl.setAttribute('ondragover', "allowDrop(event)");
//	divCtrl.setAttribute('ondrop', "dropExecution(event)");  no need, this is already inherited
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
function  propGetter() {
	//  <property id="0006" value="1" operator="="/>
	var str = "";
	if (this.included) {
		var container = this.component.split("|");
		str = str + '\n        <property id="' + this.id + '" value="' + this.value + '" label="' + this.label + '" index="' + this.index;
		if (container[1]=="arg") {
			str = str + '" operator="' + this.operator;
			str = str.replace(/&gt;/g, '>');
			str = str.replace(/&lt;/g, '<');
		}
		str = str + '"/>';		
	}
	return  str;
}
function  deviceGetter() {
	//<component id="SMVX">
	// properties.getString
	//</component>
	var strDevice = "";
	for(var i = 0; i < this.properties.length; i++) {
		if (this.properties[i].included == true) {
			var ruleId = this.id.split("|")[0];
			var deviceId = this.id.split("|")[2];
			var device = rulesForEdit[ruleId].devices[this.id];
			strDevice = strDevice + '\n      <component id="'+deviceId+'" name="'+device.name+'">';
			strDevice = strDevice + this.properties[i].getString;
			strDevice = strDevice + '\n      </component>';
		}
	}
	return  strDevice;
}
/*
 * Changes the z-order and style of the rule to highlight it
 */
function selectRuleObj(ito) {
	ruleId = ito.id.split("|");
	if (selectedRule.length!=0) {
		if($("#"+selectedRule).length) {//test if the selectedRule still exists, it might have already been removed
			//change the currently selected rule's z-index
			document.getElementById(selectedRule).style.zIndex="auto";
			document.getElementById(selectedRule+"|header").removeAttribute("style");
			document.getElementById(selectedRule).style.border="1px solid white";
			document.getElementById(selectedRule).style.background="darkgray";
		}
	}
//	ito.setAttribute("style","background-color:darkgray;color:white");
	document.getElementById(ruleId[0]).style.zIndex="99";
	document.getElementById(ruleId[0]).style.background="lightgreen";
	document.getElementById(ruleId[0]+"|header").style.border="1px solid green";
	selectedRule = ruleId[0];
	document.getElementById(ruleId[0]).style.border="1px solid lightgreen";
}
/*
 * Edits the rule name
 */
function editRule(editBtn){
	var str = editBtn.id.replace("|edit", "|txt");
	$("#"+str).prop('disabled', false);
	$("#"+str).focus();
	$("#"+str).select();
	var eRule = document.getElementById(str);  //we could have used jquery all the way below, but we didn't
	eRule.addEventListener('blur', function (evt) {
		$("#"+str).prop('disabled', true);
		eRule.removeEventListener('blur', evt.target);
		rulesForEdit[eRule.id.split("|")[0]].name = $("#"+str).val();
    });
}
/*
 * toggles the height of the rule element
 */
function toggleRule(ruleIdHeader){
	var str = ruleIdHeader.replace("|header", "");
	if ($("#"+str+"|arg").is(':visible')) {
		//the _arg element is visible
		$("#"+str).animate({height : $("#"+ruleIdHeader).outerHeight()+"px", width: "250px"}); 
		$("#"+str+"|arg").hide(400)
		$("#"+str+"|ctrl").hide(400)
		$("#"+ruleIdHeader+"|min").html('<img class="pHover minmax" src="/pics/max.png"/>');	
	} else {
		//we create $inspector to get the CSS height value
		var $inspector = $("<div>").css('display', 'none').addClass("rule");
		$("body").append($inspector);
		var cssHeight = $inspector.css("height");
		var cssWidth = $inspector.css("width");
		$inspector.remove();
		$("#"+str+"|arg").show()
		$("#"+str+"|ctrl").show()
		$("#"+str).animate({height:cssHeight, width:cssWidth});
		$("#"+ruleIdHeader+"|min").html('<img class="pHover minmax" src="/pics/min.png"/>');
	}
}
/*
 * removes the rule element from the editor
 * removes the rule from the array
 */
function removeRule(ruleId){
	var rule = rulesForEdit[ruleId];
	var proceed = confirm('Will delete Rule "'+rule.name+'"!\nOK to proceed?');
    if (proceed == true) {
    	//remove the rule from the rulesForEdit
    	var newRules = [];
    	for (var key in rulesForEdit) {
    		if (key!=ruleId)
    			newRules[key] = rulesForEdit[key];
    	}
    	rulesForEdit = newRules;
    	$("#"+ruleId).remove();
    } else {
//        alert("Delete aborted.");
    }
}
/*
 * removes the device element from the pane
 * removes the device from the rule array 
 */
function removeDevice(deviceId) {
	var ruleId = deviceId.split("|")[0];
	var rule = rulesForEdit[ruleId];
	var device = rule.devices[deviceId];
	var proceed = confirm('Will delete Device "'+device.name+'" from the Rule "'+rule.name+'"!\nOK to proceed?');
    if (proceed == true) {
    	//remove the rule from the rulesForEdit
    	var newDevices = [];
    	for (var key in rule.devices) {
    		if (key!=deviceId)
    			newDevices[key] = rule.devices[key];
    	}
    	rule.devices = newDevices;
    	$("#"+deviceId).remove();
    } else {
//        alert("Delete aborted.");
    }
}
function  ruleGetter() {
//  <rule name="PIR Switch ON 1" condition="IF">
//	device.getString
//  </rule>
	var str = '\n  <rule name="'+this.name+'" condition="IF">\n';
	str = str+'    <arguments>';
	for (var devKey in this.devices) {
		//loop for the arguments first
		var theDevice = this.devices[devKey];
		var container = devKey.split("|");
		if (container[1] == "arg") {
			str = str + theDevice.getString;
		}
	}
	str = str+'\n    </arguments>\n    <execution>';
	for (var devKey in this.devices) {
		//loop for the execution next
		var theDevice = this.devices[devKey];
		var container = devKey.split("|");
		if (container[1] == "ctrl") {
			str = str + theDevice.getString;
		}
	}
	str = str+'\n    </execution>';
	str = str + "\n  </rule>";
	return  str;
}
function toggleDrag(ito, isEnabled) {
	var eId = ito.id.split("|");
	var e = document.getElementById(eId[0])
	e.setAttribute("draggable", isEnabled);
	var containerId = eId[0] +"|"+ eId[1] + "|"+ eId[2];
	var container = document.getElementById(containerId)
	container.setAttribute("draggable", isEnabled);
}
function drag(e) {
    e.dataTransfer.setData("text", e.target.id);
    var pSrc = e.target;    
    var rectSrc = pSrc.getBoundingClientRect();
    deltaLeft = e.clientX - rectSrc.left;   
}

function allowDrop(evt) {
	evt.preventDefault();
}
/*
 * Function called when a device is dropped into a rule panel or the composer panel
 * if a device is dropped into an existing rule, it will only be allowed if:
 * 		1. targetPanel == sourcePanel
 */
function showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, isNewRule, isCIR) {
	if (isNewRule) {
		var suffix = "|arg";
		var str = "|arg|";	
		if (category=="ctrlMenuItem"){
			//this is from ctrlsMenuId
			suffix = "|ctrl";
			var str = "|ctrl|";
		}
	} else {
		if (tCategory == "ARG") {
			var str = "|arg|";
		}else if (tCategory == "CTRL") {
			var str = "|ctrl|";
		}
	}
	if (tCategory == "CMPSR" || (tCategory == "CTRL" && category=="ctrlMenuItem") || (tCategory == "ARG" && category=="snsrMenuItem")){
		var device = new Device(rule.id+str+deviceList[i].id,deviceList[i].name,deviceList[i].room, deviceList[i].properties);
		var deviceExists = false;
		for (var devKey in rule.devices) {
			//loop in all the rule.devices to get if the device already exists. If it exists, we need not create the device again   
			if (devKey == device.id) {
				deviceExists = true;
				break;
			}
		}
		if (deviceExists) {
			if (!isCIR)
				alert("Cannot add the device "+device.name+" as it already exists.");
		} else {
			rule.addDevice(device);
			if (isNewRule) {
				var ruleObj = document.getElementById(rule.id);
				ruleObj.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
				ruleObj.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
				device.create(i, rule.id+suffix);
			} else {
				if (tCategory == "ARG") {
					//rule.addArgDevice(device);
					device.create(i, rule.id+"|arg");
				}else if (tCategory == "CTRL") {
					device.create(i, rule.id+"|ctrl");
				}
				var ruleObj = document.getElementById(rule.id).parentElement;
			}
			device.drawProperties();
		}
	} else {
		if (tCategory == "ARG")
			alert("Actions cannot be added to a Rule's Trigger.")
		else
			alert("Triggers cannot be added to a Rule's Action. tCategory:"+tCategory+" category:"+category+" rule.name:"+rule.name)
	}
}

/*
 * Function called when an existing device is dropped into a rule panel
 */
function showTransferredDevice(tCategory, category, i, rule, fromDevice) {
	if (tCategory == "ARG") {
		var str = "|arg|";
	}else if (tCategory == "CTRL") {
		var str = "|ctrl|";
	}
	var device = new Device(rule.id+str+fromDevice.id.split("|")[2],fromDevice.name,fromDevice.room,fromDevice.properties);
	
	rule.addDevice(device);
	if (tCategory == "ARG") {
		//rule.addArgDevice(device);
		device.create(i, rule.id+"|arg");
	}else if (tCategory == "CTRL") {
		device.create(i, rule.id+"|ctrl");
	}
	device.drawProperties();
	//update the properties of device with that of the fromDevice
	var props = device.properties;	
	var frProps = fromDevice.properties;
	for(var i = 0; i < props.length; i++){
		props[i].value = frProps[i].value;
		props[i].operator = frProps[i].operator; 
		props[i].included = frProps[i].included;
		var p = document.getElementById(device.id+"|"+props[i].id);
		var frP = document.getElementById(fromDevice.id+"|"+frProps[i].id);
		p.included = frProps[i].included;
		p.style.top = frP.style.top;
		var btn = document.getElementById(device.id+"|"+props[i].id+"|1");
		var frBtn = document.getElementById(fromDevice.id+"|"+frProps[i].id+"|1");
		btn.setAttribute("style",frBtn.getAttribute("style"));
		var input = document.getElementById(device.id+"|"+props[i].id+"|input");
		var frInput = document.getElementById(fromDevice.id+"|"+frProps[i].id+"|input");
		input.value = frInput.value;
		var tdOpt = document.getElementById(device.id+"|"+props[i].id+"|cond");
		tdOpt.innerHTML = props[i].operator;
	}
	var div = document.getElementById(device.id);
//	arrangeProps(div, device.properties);
	hoverDevice(div);
	unhoverDevice(div);
}

/*
 * Function called when an item is dropped
 */
function dropDevice(evt) {
	evt.preventDefault();
	var targetId = evt.target.id;
	var target = document.getElementById(evt.target.id);
	var tCategory = target.getAttribute("category")
	var data = evt.dataTransfer.getData("text");
	var dropped = document.getElementById(data);
	var category = dropped.getAttribute("category");
	var i = dropped.getAttribute("dIndex");
	if (category=="snsrMenuItem"||category=="ctrlMenuItem"||category=="RULE") {
		//the object being dropped is a menu item or the rule
		var rectTarget = document.getElementById("composer").getBoundingClientRect();
		if (evt.target.getAttribute("category")=="CMPSR"&&(category=="snsrMenuItem"||category=="ctrlMenuItem")) {
			//we will only create a new rule if the target is the "compose" panel and item dropped is ctrlMenuItem or snsrMenuItem
			var rule = createRule();
			showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, true, false);
		} else {
			//device is dropped into an existing rule
			if (category=="ctrlMenuItem"||category=="snsrMenuItem") {
				//ctrlMenuItem or snsrMenuItem is dropped into the existing rule
				var rule = rulesForEdit[evt.target.parentElement.id];
				showDroppedDevice(tCategory, category, i, rule, evt, rectTarget, false, false);
			} else {
				
			}
			if (category=="device"||category=="RULE") {
				//var device = controllers;
				var rectTarget = document.getElementById("composer").getBoundingClientRect();
				dropped.style.left = parseInt(evt.clientX-rectTarget.left)-deltaLeft+5+"px";
				dropped.style.top = parseInt(evt.clientY-rectTarget.top)-10+"px";
			}
		}
	} else {
		//the object being dropped is a device already inside a rule
		transferDevice(dropped, target);
	}
}

/*
 * Transfers a device to another rule
 * Should only be from the same panel (Trigger->Trigger, Action->Action)
 */
function transferDevice(dropped, target) {
	if (dropped.id.split("|")[0]!=target.id.split("|")[0]) {
		if (dropped.id.split("|")[1]==target.id.split("|")[1]){
			var fromId = dropped.id.split("|");
			var ruleFrom = rulesForEdit[fromId[0]];
			var device = ruleFrom.devices[dropped.id];
			//we will add the device to the target rule
			var ruleTo = rulesForEdit[target.id.split("|")[0]];
			
			var i = parseInt(dropped.getAttribute('dIndex'));
			var tCategory = target.getAttribute("category");
			var category = dropped.getAttribute("category");
			//show the transferred device and add it to the ruleTo
			showTransferredDevice(tCategory, category, i, ruleTo, device)
			
			//we will remove the device from the current rule
			var newSourceDevices = [];
			for (var key in ruleFrom.devices) {
				if (key!=dropped.id)
					newSourceDevices[key] = ruleFrom.devices[key];
			}
			ruleFrom.devices = newSourceDevices;
			//remove the element of the device
			dropped.parentNode.removeChild(dropped);
		} else {
			//cannot transfer from Trigger->Action or vice versa
			alert("Cannot transfer from Trigger->Action or vice versa.");
		}
	
	}else{
		//device is dropped in the same rule
		alert("Please drop to another rule.");
	}
}

function createRule() {
	var rule = new Rule('rule'+ruleCount,'Rule'+ruleCount);
	  Object.defineProperties(rule, {
			'getString': { get: ruleGetter }
	  });
	rulesForEdit[rule.id] = rule;
	rule.create();
	ruleCount++;
	return rule;
}


/*
 * Includes the property in the rule evaluation
 */
function markProp(btn, noToggle) {
	var str = btn.id.split("|");
	ruleId=str[0]
	deviceId=str[0]+"|"+str[1]+"|"+str[2]
	propId = deviceId+"|"+str[3]
	var rule = rulesForEdit[ruleId];
	var ruleDevices = rule.devices;
	var device = ruleDevices[deviceId];
	var p = document.getElementById(propId);
	pObj = device.properties[p.index];
	pObj.value = document.getElementById(propId+"|input").value;
	pObj.operator = document.getElementById(propId+"|cond").innerHTML;
	var toggle = true;
	if (p.included==true)
		toggle = false;
	if (noToggle)
		toggle = true;
	if (toggle==true) {
		p.included = true;
		pObj.included = true;
		btn.setAttribute("style","background-color:green");		
	} else {
		p.included = false;
		pObj.included = false;
		btn.removeAttribute("style");
	}	
	
}

function clickOpt(ito) {
	  var div = document.getElementById("optCond");
	  var device = ito.id.split("|");
	  var deviceId = device[0]+"|"+device[1]+"|"+device[2];
	  var divDevice = document.getElementById(deviceId);
	  divDevice.appendChild(div);
	  div.style.display = "block";
	  var rect1 = ito.getBoundingClientRect();
	  var rect2 = divDevice.getBoundingClientRect();
	  div.setAttribute('targetId', ito.id); //we are setting this so that the setOperator function can set the innerHTML of the clicked <p> element
	  div.style.left = Math.round(rect1.right)-Math.round(rect2.left)+"px";
	  div.style.top = 10+Math.round(rect1.top)-Math.round(rect2.top)-Math.round(rect1.height)+"px";
	  var input = document.getElementById(propId+"|input");
	  input.select();
}
function setOperator(ito){
	var div = document.getElementById("optCond");
	var targetId = div.getAttribute('targetId');
	var td = document.getElementById(targetId);
	var device = targetId.split("|");
	var btnId = device[0]+"|"+device[1]+"|"+device[2] + "|" + device[3]+"|1";
	td.innerHTML = ito.innerHTML;
	td.operator = ito.innerHTML;
	div.style.display = "none";
	markProp(document.getElementById(btnId), true);
	var valId = device[0]+"|"+device[1]+"|"+device[2] + "|" + device[3]+"|val";
	var val = document.getElementById(valId);
	var input = document.getElementById(propId+"|input");
	input.select();
}

function includeProperty(ito) {
	var device = ito.id.split("|");
	var btnId = device[0]+"|"+device[1]+"|"+device[2] + "|" + device[3]+"|1";
	if(isNaN(parseInt(ito.value))) {
		//do nothing
	} else {
	    if (parseInt(ito.value)>parseInt(ito.max)) {
			alert("Please enter values between " + ito.min + " and " + ito.max);
			ito.value = ito.max;
	    }
	} 
	markProp(document.getElementById(btnId), true);
}

function compose() {
var str = "<rules>";
	for (var key in rulesForEdit) {
		var rule = rulesForEdit[key];
		var ruleDevices = rule.devices;
		str = str + rule.getString;
	}
	str = str +"\n</rules>";
	var ta = document.getElementById("ta");
	ta.value = str;
//	$.get("/devices/composeCIR", "cir="+str, function(data, status) {
//		$("#notif").html(data);
//	});
	//alert(document.location.hostname+'/devices/composeCIR');
alert(document.location.hostname+':8888/test.html');

	//ajax.open('POST',  document.location.hostname+'/devices/composeCIR');
	//ajax.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	//var postData = 'cir='+str; 
	//ajax.send(postData);
ajax.open('GET',  'http://'+document.location.hostname+':8888/test.html');
ajax.send();
}
