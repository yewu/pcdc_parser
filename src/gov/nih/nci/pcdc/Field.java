package gov.nih.nci.pcdc;

import java.util.ArrayList;

public class Field {
	private String name;
	private String desc;
	private String type;
	private Permissible_Values pv = new Permissible_Values ();
	private Boolean isVisible;
	private Boolean isRequired;
	
	
	
	public Boolean getIsVisible() {
		return isVisible;
	}
	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}
	public Boolean getIsRequired() {
		return isRequired;
	}
	public void setIsRequired(Boolean isRequired) {
		this.isRequired = isRequired;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		if (desc.contains(":")) 
			this.desc = "\"" + desc + "\"";
		else
			this.desc = desc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Permissible_Values getPv() {
		return pv;
	}
	public void setPv(Permissible_Values pv) {
		this.pv = pv;
	}
	
	
}
