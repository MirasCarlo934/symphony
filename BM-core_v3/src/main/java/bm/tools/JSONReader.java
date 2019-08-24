package bm.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.json.JSONObject;

public class JSONReader {

	public JSONObject[] readFromInputStream(InputStream input) throws IOException {
		Vector<JSONObject> jsons = new Vector<JSONObject>(5, 5);
		InputStreamReader reader = new InputStreamReader(input);
		
		String s = "";
		int i = 0;
		while(reader.ready()) {
			char c = (char) reader.read();
			s += c;
			if(c == '{')
				i++;
			else if(c == '}')
				i--;
			
			if(i == 0) { 
				boolean valid = false;
				while(!valid) { //checks if leading and trailing characters are '{' and '}' respectively
					if(!s.startsWith("{")) {
						s = s.substring(1);
						valid = false;
					} else if(!s.endsWith("}")) {
						s = s.substring(0, s.length() - 1);
					} else valid = true;
					if(s.length() <= 0) {
						valid = false;
						break;
					}
				}
				if(valid) {
					JSONObject json = new JSONObject(s);
					jsons.add(json);
					s = "";
				}
			}
		}
		
		return jsons.toArray(new JSONObject[jsons.size()]);
	}
}
