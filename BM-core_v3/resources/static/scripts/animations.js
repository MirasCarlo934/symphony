/*
 * Static classes:
 * 	-drawerHandle : for buttons that open a drawer
 * 	-drawer : for drawers
 * 
 * Static IDs:
 * 	-[drawer_id]Content : for element containing contents of drawer
 * 	-
 */

var notifBoxTopMargin;
var notifBoxTotalHeight;
var defaultDrawerHeight = -1; //can be set to define a constant drawer height

$(document).ready(function() {
	notifBoxTopMargin = parseInt($(".notifBox").css("margin-top"));
	notifBoxTotalHeight = parseInt($(".notifBox").height()) + parseInt(notifBoxTopMargin);
	
	$(".notifBox").click(function() {
		exitNotif();
//		$(this).fadeTo(fast, 0);
//		$(this).remove();
	});
});

//returns: true if drawer opened, false if drawer closed
function openDrawer(drawerID, top_padding, bottom_padding, speed, actionAfter) {	
	var $drawer = $("#" + drawerID);
	var drawerHeight = defaultDrawerHeight;
	
	if($drawer.height() == 0) {
		console.log("Opening drawer " + drawerID);
		if(defaultDrawerHeight === -1) { //if === -1, get drawer height from sum of child elements height
			$drawer.children().each(function() {
				drawerHeight += $(this).outerHeight(true);
			});
		}
		$drawer.animate({
			height: drawerHeight,
	    		paddingTop: top_padding, 
	    		paddingBottom: bottom_padding
	    }, speed, actionAfter);
		return true;
	}
	else {
		console.log("Closing drawer " + drawerID);
		$drawer.animate({
	        height: '0px',
	        	paddingTop: '0px', 
	    		paddingBottom: '0px'
	    }, speed, actionAfter);
		return false;
	}
}

function closeDrawer(drawerID, speed, actionAfter) {	
	var $drawer = $("#" + drawerID);
	console.log("Closing drawer " + drawerID);
	$drawer.animate({
        height: '0px',
        	paddingTop: '0px', 
    		paddingBottom: '0px'
    }, speed, actionAfter);
}

/**
 * Pulls a ribbon from the side of the screen
 * 
 * @param ribbonID The HTML ID of the ribbon
 * @param direction The direction in which the ribbon will be pulled from ("left", "right", "top", "bottom" values only)
 * @param distance The distance in which the ribbon will be pulled
 * @param speed The speed in which the ribbon will be pulled
 * @param actionAfter A javascript function for what to do after the animation
 * @returns
 */
function openRibbon(ribbonID, direction, distance, speed, actionAfter) {
	var $ribbon = $("#" + ribbonID);
	distance += "px";
	console.log("Opening ribbon " + ribbonID);
	if(direction == "left") {
		$ribbon.animate({
			left: "+=" + distance
		}, speed, actionAfter);
	} else if(direction == "right") {
		$ribbon.animate({
			right: "+=" + distance
		}, speed, actionAfter);
	} else if(direction == "top") {
		$ribbon.animate({
			top: "+=" + distance
		}, speed, actionAfter);
	} else if(direction == "bottom") {
		$ribbon.animate({
			bottom: "+=" + distance
		}, speed, actionAfter);
	}
}

/**
 * Closes/Pushes a ribbon to the side of the screen
 * 
 * @param ribbonID The HTML ID of the ribbon
 * @param direction The direction in which the ribbon will be pushed to ("left", "right", "top", "bottom" values only)
 * @param distance The distance in which the ribbon will be pulled
 * @param speed The speed in which the ribbon will be pulled
 * @param actionAfter A javascript function for what to do after the animation
 * @returns
 */
function closeRibbon(ribbonID, direction, distance, speed, actionAfter) {
	var $ribbon = $("#" + ribbonID);
	distance += "px";
	console.log("Closing ribbon " + ribbonID);
	if(direction == "left") {
		$ribbon.animate({
			left: "-=" + distance
		}, speed, actionAfter);
	} else if(direction == "right") {
		$ribbon.animate({
			right: "-=" + distance
		}, speed, actionAfter);
	} else if(direction == "top") {
		$ribbon.animate({
			top: "-=" + distance
		}, speed, actionAfter);
	} else if(direction == "bottom") {
		$ribbon.animate({
			bottom: "-=" + distance
		}, speed, actionAfter);
	}
}