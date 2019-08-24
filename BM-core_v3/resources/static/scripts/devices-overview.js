var animationSpeed = 300;
		/* var colors = ["blue", "red", "green", "darkgray"]; */
		
		[(${devices})]
		
		[(${rooms})]
		
		var deviceArray = [(${devArray})]
		var roomIDArray = [(${roomIDArray})]
		
		var rooms = new HashMap();
		var devices = new HashMap();
		
		var nameInputHTML = "<input type='text' class='smallTextBox' style='width:80%; margin:auto;' placeholder='new name' name='newName'/>";
		var saveIconSrc = "/pics/save.png";
		var origHTMLs = new HashMap();
		
		function DeviceHTML(devID) {
			this.name = $("#d_" + devID + "_name").html();
			this.icon = $("#d_" + devID + "_editBtn").attr("src");
		}
		
		function Room(id, width, height, minimized) {
			this.id = id;
			this.width = width;
			this.height = height;
			this.minimized = minimized;
		}
		
		function Device(id, clicked, isEditing) {
			this.id = id;
			this.clicked = clicked;
			this.isEditing = isEditing;
		}
		
		function updateDevices() {
			$("div.deviceContainer").each(function() {
				var $device = $(this);
				var device = new Device($device.attr("id"), false, false);
				devices.add($device.attr("id"), device);
				//console.log($device.attr("id"));
			});
		}
		
		function updateRoomDimensions() {
			var tallest = 0; //including margins
			$("div.roomContainer").each(function() {
				var $room = $(this);
				if($room.outerHeight(true) > tallest) {
					tallest = $room.outerHeight(true);
				}
				if(rooms.containsKey($room.attr("id"))) {
					var room = rooms.get($room.attr("id"));
					room.width = $room.width();
					/* room.height = $room.height(); */
				} else {
					var room = new Room($room.attr("id"), $room.width(), $room.height(), false);
					rooms.add($room.attr("id"), room);
				}
			});
			
			/* $("div.roomContainerContainer").each(function() { //to resize roomContainerContainers inside rooms first!
				var $box = $(this);
				if(!$box.parent().parent().hasClass("root_sort")) {
					$box.height("auto");
				}
			}); */
			$("div.roomContainerContainer").each(function() { //resize roomContainerContainers in root_sort!
				var $box = $(this);
				if($box.parent().parent().hasClass("root_sort")) {
					$box.height(tallest + "px");
				}
			});
		}
		
		$(document).ready(function() {
			
			/*
				INITIALIZATION
			*/			
			updateDevices();
			updateRoomDimensions();
			
			$(".sortableHorizontal")
				.sortable({
					connectWith: ".sortableHorizontal",
					revert: animationSpeed,
					beforeStop: function(event, ui) {
						if($(this).attr("id") == "root_sort" && ui.item.attr("class").includes("device_sortItem")) {
							alert("YEAH");
							ui.sender.sortable("refresh");
						}
						/*alert(ui.item.attr("id"));
						alert(ui.sender.attr("id")); */
						/* var items = $(this).sortable("toArray");
						var senderItems = ui.sender.attr("id");
						alert(items);
						alert(senderItems); */
					},
					"placeholder": 'sortPlaceholder',  
					"opacity": 0.5,
		        		"start": function (event, ui) {
		        			var $item = ui.item;
		        			if ($(this).hasClass("sortableHorizontal")) {
		        				/* alert($item.attr("class")); */
		        	            $(this).sortable("option", "connectWith", ".room_sort");
		        	            $(this).sortable("refresh");
		        	        }
		        			$(".sortPlaceholder").css({
		        				"margin-top": $(".deviceContainer").css("margin-top"),
		        				"margin-left": $(".deviceContainer").css("margin-left"),
		        				"height": $(".deviceContainer").height() - ($(".sortPlaceholder").css("border-width").replace(/[^-\d\.]/g, '') * 2) + "px",
		        			});
		        		}, 
		        	    "receive": function(event, ui) {
		        	        updateRoomDimensions();
		        	        /* if (ui.item.hasClass("deviceContainer")) {
			        	        	$(this).sortable("option", "connectWith", ".sortableHorizontal");
			        	        	$(this).sortable("refresh");
		        	        } */ 
		        	        /* if(ui.item.hasClass("root_sortItem")) {
			        	        ui.item.children().height("auto");
		        	        } */
		        	        	
		        	        /* serializeAllSortables(); */
		        	    }
				})
				.disableSelection()
				.css({
					"min-height": $(".deviceContainer").outerHeight(true),
					"min-width": $(".deviceContainer").outerWidth(true),
					"padding-right": "15px"
				});
			
			$("div.deviceContainer")
				.hover(function() {
					//openDrawer($(this).attr("id") + "_infos", 0, 0, animationSpeed);
					$(this).find($("img.overlay")).animate({
						"filter": "opacity(100%) invert(100%)"
					}, animationSpeed);
				}/* , function() {
					var dev = devices.get($(this).attr("id"));
					console.log(dev.isEditing);
					if(dev.isEditing === false) {
						console.log("test");
						$(this).find($("img.overlay")).animate({
							"filter": "opacity(0%) invert(100%)"
						}, animationSpeed);
					}
				} */)
				.click(function() {
					//openDrawer($(this).attr("id") + "_infos", 0, 0, animationSpeed);
					console.log($(this).attr("id") + " clicked!")
					var clicked = devices.get($(this).attr("id")).clicked;
					if(clicked) { //hide overlay
						$(this).find($("img.overlay")).animate({
							"filter": "opacity(0%) invert(0%)"
						}, animationSpeed);
						devices.get($(this).attr("id")).clicked = false;
					} else { //show overlay						
						$(this).find($("img.overlay")).animate({
							"filter": "opacity(100%) invert(100%)"
						}, animationSpeed);
						devices.get($(this).attr("id")).clicked = true;
					}
				});
			
			/* $("div.roomContainer > div.header").click(function() {
				var roomID = $(this).parent().attr("id");
				if(rooms.get(roomID).minimized === true) {
					openRoom(roomID);
				} else {
					closeRoom(roomID);
				}
			}); */
		});
		
		function serializeAllSortables() {
			var rooms = [];
			var string = "";
			$(".sortableHorizontal").each( function() {
				var array = $(this).sortable("toArray");
				/* array.unshift($(this).attr("id")); */
				rooms.push(array);
				string += $(this).attr("id") + ":"
				for(var i = 0; i < array.length; i++) {
					string += array[i] + ",";
				}
				string += ";;;";
			});
			$.get("/devices/rearrangeHome", {
		        "string": string
		    })
		}
		
		function editDevice(devID) {
			var dev = devices.get("d_" + devID);
			dev.isEditing = true;
			console.log("Editing device " + devID);
			var origHTML = new DeviceHTML(devID);
			$("#d_" + devID + "_nameContainer").height($("#d_" + devID + "_nameContainer").height());
			$("#d_" + devID + "_name").html(nameInputHTML);
			$("#d_" + devID + "_name").prop("iseditable", true);
			$("#d_" + devID + "_editBtn").attr("src", saveIconSrc);
			$("#d_" + devID + "_editBtn").attr("onclick", "saveDevice('" + devID + "')");
			origHTMLs.add(devID, origHTML);
		}
		
		function saveDevice(devID) {
			var dev = devices.get("d_" + devID);
			dev.isEditing = false;
			console.log("Updating device " + devID + " credentials");
			submitForm_GET("d_" + devID + "_form", "notif");
			var origHTML = origHTMLs.get(devID);
			/* $("#d_" + devID + "_name").html(origHTML.name.html($(".name").attr("value"))); */
			$("#d_" + devID + "_name").html(($(".name").attr("value")));
			$("#d_" + devID + "_editBtn").attr("src", origHTML.icon);
			$("#d_" + devID + "_editBtn").attr("onclick", "editDevice('" + devID + "')");
			console.log("Device " + devID + " credentials updated!");
		}
		
		var roomHeights = new HashMap();
		var roomWidths = new HashMap();
		function openRoom(roomID) {
			var $room = $("#" + roomID);
			var $header = $("#" + roomID + "_header"); //this contains the $nameplate !
			var $nameplate = $("#" + roomID + "_name");
			var $children = $room.children().not($header);
			
			console.log("Opening container for room " + roomID);
			$room.animate({
				"min-width": roomWidths.get(roomID),
				"min-height": roomHeights.get(roomID),
				"width": rooms.get(roomID).width,
				"height": rooms.get(roomID).height
			}, animationSpeed, function() {	
				$room.css({ //resets width & height so they can resize dynamically
					"width": "",
					"height": "",
					"min-width": "",
					"min-height": ""
				}) 
				$children.css({
					opacity: 100
				});
			});
			$nameplate.animate({
				"background-color": "white"
			});
			rooms.get(roomID).minimized = false;
		}
		function closeRoom(roomID) {
			var $room = $("#" + roomID);
			var $header = $("#" + roomID + "_header"); //this contains the $nameplate !
			var $nameplate = $("#" + roomID + "_name");
			var $children = $room.children().not($header);
			
			console.log("Closing container for room " + roomID);
			updateRoomDimensions();
			roomHeights.add(roomID, $room.height());
			roomWidths.add(roomID, $room.width());
			$children.css({
				opacity: 0
			});
			$room.animate({
				"min-width": $nameplate.width(),
				"min-height": $nameplate.height(),
				"width": $nameplate.width(),
				"height": $nameplate.height()
			}, animationSpeed);
			$nameplate.animate({
				"background-color": $room.css("background-color")
			});
			rooms.get(roomID).minimized = true;
		}