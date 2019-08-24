function Device(id, name, room, properties) {
	this.id = id;
	this.name = name;
	this.properties = properties;
	this.roomID = room;
}

function Room(id, name) {
	this.id = id;
	this.name = name;
}

function HashMap() {
	var array = [];
	
	this.get = function get(key) {
		for(i = 0; i < array.length; ++i) {
			var obj = array[i];
//			alert(key + "-" + obj.key);
			if(obj.key === key) {
				return obj.value;
				break;
			}
		}
		return null;
	}
	
	this.add = function add(key, value) {
		var entry = new KeyValue(key, value);
		for(i = 0; i < array.length; ++i) {
			var obj = array[i];
			if(obj.key === key) {
				obj.value = value;
				break;
			}
		}
		array.push(entry);
	}
	
	this.containsKey = function containsKey(key) {
		for(i = 0; i < array.length; ++i) {
			var obj = array[i];
			if(obj.key === key) {
				return true;
				break;
			}
		}
		return false;
	}
	
	this.size = function size() {
		return array.length;
	}
}
function KeyValue(key, value) {
	this.key = key;
	this.value = value;
}