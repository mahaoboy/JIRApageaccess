package com.winagile.pageaccess.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddPageAccessModel {

	@XmlElement(name = "value")
	private String message;

	public AddPageAccessModel() {
	}

	public AddPageAccessModel(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}