package com.store.amazonaws.controller;

import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.store.amazonaws.ImageService;
import com.store.amazonaws.bean.ImageDecode;

@RestController
public class ImageController {

	@Autowired
	private ImageService imageService;
	private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

	@GetMapping("/getImage/{fileName}")
	public String getImage(@PathVariable String fileName) {
		return imageService.getImageURLFromS3(fileName);
	}

	@PostMapping("/saveImage")
	public String saveImage(@RequestBody ImageDecode imageDecode) {
		System.out.println("entered into");
		System.out.println(imageDecode.getBase64());
		String fileName = imageService.convertBase64ToImage(imageDecode.getBase64());
		String objectUrl = imageService.saveImageToS3(fileName);
		logger.info("Create ObjectURL for image is " + objectUrl);
		return fileName;
	}
	
	@DeleteMapping("deleteImage/{fileName}")
	public boolean deleteImage(@PathVariable String fileName) {
		return imageService.deleteImageFromS3(fileName);
	}

}
