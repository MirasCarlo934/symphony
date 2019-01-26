patterns = ["Flow", "Solid", "Fade", "Twinkle"];
colors=["RainbowColors","ForestColors","RedGreenBlue","OceanColors","OrangeWhiteBlue","Random","GreenRedWhiteStripe","YellowBlueWhiteStripe","Christmas","PartyColors","RedWhiteBlue","CloudColors"];
var wsUri = "ws://"+location.hostname+"/ws";
websocket = new WebSocket(wsUri);

function wsHandler() {
    websocket.onopen = function(evt) {
      for (i=1;i<=2;i++) {
        var hover = document.getElementById("tmp"+i);
        hover.parentNode.removeChild(hover);
      }
    };
    websocket.onclose = function(evt) {
      status.innerHTML="DISCONNECTED";
    };
    websocket.onmessage = function(evt) {
        if (evt.data != "Connected") {
        	status.innerHTML="data arrived";
        } else {
          var status = document.getElementById('status');
          status.innerHTML="Synchronized";
        }
    };
    websocket.onerror = function(evt) {
      console.log("ERROR: " + evt.data);
    };
  } 
/*
 * Sends data for the light sequence
 */
function sendColor(rgb){
	var data = '{"cmd":4,"data":['+rgb.r+","+rgb.g+","+rgb.b+"]}";
	websocket.send(data);
}
/*
 * Sends data for the light sequence
 */
function sendData(){
	var data = '{"cmd":1,"data":[';
	for (i = 0; i < colors.length; i++) {
		var chk = document.getElementById("in_"+i);	//the checkbox object
		if (chk.checked)
			data = data + '1';
		else
			data = data + '0';
		var r = document.getElementsByName("r"+i);	//radiobutton group
	    for (var j=0, len=r.length; j<len; j++) {
	    	if (r[j].checked) {
	    		if ( i == colors.length - 1)
	    			data = data + r[j].value +']}';
	    		else
	    			data = data + r[j].value + ',';
	    	}
	    }
	}
	var txt2 = document.getElementById("txt2");
	txt2.value=data;
	websocket.send(data);
}
/*
 * sends data for fire sequence
 */
function sendFireData(){
	var data = '{"cmd":3,"data":';
	var r = document.getElementsByName("rFire");	//radiobutton group
    for (var j=0, len=r.length; j<len; j++) {
    	if (r[j].checked) {
			data = data + r[j].value +'}';
    	}
    }
	var txt2 = document.getElementById("txt2");
	txt2.value=data;
	websocket.send(data);
}
/**
 * Called when mouse hovers on the item
 * @param ito
 * @returns
 */
function hover(ito) {
	ito.style.backgroundColor = "gray";
}
/**
 * Called when mouse unhovers on the item
 * @param ito
 * @returns
 */
function unhover(ito) {
	ito.removeAttribute("style");
}
function handleClick(ito) {
	alert(ito.id);
}
/**
 * Loads the patterns page where user can select the different light effects 
 * @returns
 */
function loadPatterns(){
	var mainDiv = document.getElementById("main");
	var table = document.createElement('table');
	table.setAttribute("border","1");
	var th = document.createElement('th');
	var trh = document.createElement('tr');
	trh.append(th);
	for (i=0;i<patterns.length;i++) {
		var th1 = document.createElement('th');
		th1.setAttribute('style', "width:50px");
		th1.innerHTML = patterns[i];
		trh.append(th1);
	}
	table.append(trh);
	var th2 = document.createElement('th');
	var th3 = document.createElement('th');
	var p = document.createElement('p');
	for (i = 0; i < colors.length; i++) {
		var lbl = document.createElement('label');
		lbl.setAttribute('id', "lbl_"+i);
		lbl.textContent = colors[i];
		lbl.setAttribute('onclick',"handleClick(this)");
		lbl.setAttribute("onmouseover","hover(this)");
    	lbl.setAttribute("onmouseout","unhover(this)")
    	lbl.setAttribute("class","lbl");;
		var input = document.createElement('input');
		input.setAttribute('type',"checkbox");
		input.setAttribute('id', "in_"+i);
		input.setAttribute('checked',"true");
		input.setAttribute("class","chk");;
		var tdLbl = document.createElement('td');
		tdLbl.append(input);
		tdLbl.append(lbl);
		var tr = document.createElement('tr');
		tr.append(tdLbl);
		for (j=0;j<patterns.length;j++) {
			var td1 = document.createElement('td');
			td1.setAttribute('id', "td"+j+"_"+i);
			var r1 = document.createElement('input');
			r1.setAttribute('type',"radio");
			r1.setAttribute('name',"r"+i);
			r1.setAttribute("class","radio");;
			if (j==0)
				r1.setAttribute('checked',"checked");
			r1.setAttribute('id', "r"+i+"_"+j);
			r1.setAttribute('value', j);
			td1.append(r1);
			tr.append(td1);
		}
		table.append(tr);
	}
	p.appendChild(table);
	mainDiv.appendChild(p);
}
/////////////////////// for the config
var stringToChange = 1;
function init() {
	  drawString();
}
function stringClick(ako) {
	  alert(ako.innerHTML)
}

function drawString(ako) {
	  drawPixels(1, 60, 1);
	  drawPixels(1, 60, 2);
	  drawPixels(2, 30, 3);
}
function drawPixels(sections, numPixels, string) {
	e = document.getElementById("composer");
	var div = document.createElement("div");
	div.setAttribute('id',"divstring"+string);
  	e.appendChild(div);
	div.setAttribute('class',"menu");
	var a = document.createElement("a");
	a.setAttribute('class', "submenu");
	a.setAttribute('id',string);
	a.setAttribute('pix',numPixels);
	a.setAttribute('s',sections);
	a.setAttribute("style","height:45px;border-radius:20px;");
	a.setAttribute("onClick","stringClick(this)");
	a.setAttribute("onmouseover","hoverConfig(this)");
	div.appendChild(a);
	for (j = 0; j < sections; j++) {
		var div2 = document.createElement("div");
		div2.setAttribute('id',"divstring"+string+"_"+j);
		div2.setAttribute("style","margin:0px 10px;display:inline-block;");
//		div2.setAttribute("onClick","stringClick(this)");
		a.appendChild(div2);
	  	for (i = 0; i<7; i++) {
	  		var p = document.createElement("P");
	  		p.setAttribute('id',"string"+string+"_"+i);
	  		//color = c[i % 3]
	  		if (i==0)
	  			p.innerHTML= 1;
	  		else if (i==6)
	  			p.innerHTML= numPixels/sections;
	  		else
	  			p.innerHTML= "=";
	  		p.setAttribute('class', "pStyle");  	  		
	  		p.setAttribute('style', "border-radius:10px;background-color:darkgray;");
	  		div2.appendChild(p);  
	  	}	
	}	
}
function sendConfig(){
	var u = document.getElementById("txtU");
	var s = document.getElementById("txtS");
	var p = document.getElementById("txtPix");
	var data = '{"cmd":2,"u":'+u.innerHTML+',"s":'+s.innerHTML+',"p":'+p.innerHTML+'}';
//	s.options[ss.selectedIndex].value
	websocket.send(data);
}
function hoverConfig(ako) {
	var u = document.getElementById("txtU");
	u.innerHTML = ako.id;
	var s = document.getElementById("txtS");
	s.innerHTML = ako.getAttribute("s");
	var p = document.getElementById("txtPix");
	p.innerHTML = ako.getAttribute("pix");
}