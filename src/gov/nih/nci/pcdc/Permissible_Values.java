package gov.nih.nci.pcdc;

import java.util.ArrayList;

public class Permissible_Values {

	private String name;
	
	private ArrayList<String> values = new ArrayList<String>();

	private ArrayList<String> codes = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getValues() {
		return values;
	}
	public void setValues(ArrayList<String> values) {
		this.values = values;
	}
	public void addValues(String value) {
		this.values.add(value);
	}

	public ArrayList<String> getCodes() {
		return codes;
	}
	public void setCodes(ArrayList<String> codes) {
		this.codes = codes;
	}
	public void addCodes(String code) {
		this.codes.add(code);
	}
	public Boolean contains(String value) {
		Boolean found = false;
		for (int i=0; i<this.values.size();i++) {
			if (this.values.get(i).equals(value)) return true;
		}
		return false;
	}
}
