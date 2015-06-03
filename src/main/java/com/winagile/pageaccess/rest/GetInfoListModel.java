package com.winagile.pageaccess.rest;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GetInfoListModel {
	@XmlElement(name = "suggestions")
	private Collection<GetInfoItemListModel> suggestions;

	GetInfoListModel() {
	}

	GetInfoListModel(Collection<GetInfoItemListModel> suggestions) {
		this.suggestions = suggestions;
	}

	public Collection<GetInfoItemListModel> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(Collection<GetInfoItemListModel> suggestions) {
		this.suggestions = suggestions;
	}
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class GetInfoItemListModel {
	@XmlElement(name = "value")
	private String value;
	@XmlElement(name = "data")
	private String data;

	GetInfoItemListModel() {
	}

	GetInfoItemListModel(String value, String data) {
		this.value = value;
		this.data = data;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}