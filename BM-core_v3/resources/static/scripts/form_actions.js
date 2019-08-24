function submitForm_POST(formID, responseContainerID) {
	var url = $("#" + formID).attr("action");
	var data = $("#" + formID).serialize();
	console.log("Submitting form " + formID + " to " + url + " on POST method"); 
	$.post(url, data, function(data, status) {
		var newLocation = $(data).filter("#location").text();
		if(newLocation != "") {
			console.log("Redirecting to '" + newLocation + "' view");
			window.location.replace("/" + newLocation);
		} else {
			console.log("Displaying response");
			$("#" + responseContainerID).html(data);
		}
	});
}

function submitForm_GET(formID, responseContainerID) {
	var url = $("#" + formID).attr("action");
	var data = $("#" + formID).serialize();
	console.log("Submitting form " + formID + " to " + url + " on GET method"); 
	$.get(url, data, function(DATA, status) {
		var newLocation = $(DATA).filter("#location").text(); //checks if returned view is redirect.html
		if(newLocation != "") { //will redirect to new location
			console.log("Redirecting to '" + newLocation + "' view");
			window.location.replace("/" + newLocation);
		} else {
			console.log("Displaying response from " + url);
			$("#" + responseContainerID).html(DATA);
		}
	});
}

function submitForm_GET(formID, responseContainerID, actionAfter) {
	var url = $("#" + formID).attr("action");
	var data = $("#" + formID).serialize();
	console.log("Submitting form " + formID + " to " + url + " on GET method"); 
	$.get(url, data, function(DATA, status) {
		var newLocation = $(DATA).filter("#location").text(); //checks if returned view is redirect.html
		if(newLocation != "") { //will redirect to new location
			console.log("Redirecting to '" + newLocation + "' view");
			window.location.replace("/" + newLocation);
		} else {
			console.log("Displaying response from " + url);
			$("#" + responseContainerID).html(DATA);
		}
		actionAfter(DATA);
	});
}

function getView(url, containerID) {
	$.get(url, "", function(data, status) {
		console.log("Displaying view of " + url + " in #" + containerID);
		$("#" + containerID).html(data);
	})
}