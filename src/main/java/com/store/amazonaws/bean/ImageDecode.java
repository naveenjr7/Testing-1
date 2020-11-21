package com.store.amazonaws.bean;

import org.springframework.stereotype.Component;

@Component
public class ImageDecode {
	private String base64;
	
	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

}
