package com.store.amazonaws;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.store.amazonaws.constant.GlobalConstants;
import com.store.amazonaws.controller.UUIDKeyGenerator;

@Component
public class ImageService {

	private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

	public void getObj(AmazonS3 s3client) {
		String bucketName = GlobalConstants.BUCKET_NAME;
		String objectName = GlobalConstants.BUCKET_FILE_PATH;

		try {
			S3Object s3object = s3client.getObject(bucketName, objectName);
			S3ObjectInputStream inputStream = s3object.getObjectContent();
			FileUtils.copyInputStreamToFile(inputStream, new File(GlobalConstants.LOCAL_DOWNLOAD_PATH));

			System.out.println("file copied to destination.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createFolder(String bucketName, String folderName, AmazonS3 client, String SUFFIX) {
		// create meta-data for your folder and set content-length to 0
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);

		// create empty content+
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

		// create a PutObjectRequest passing the folder name suffixed by /
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName + SUFFIX, emptyContent,
				metadata);

		// send request to S3 to create folder
		client.putObject(putObjectRequest);
	}

	/**
	 * This method first deletes all the files in given folder and than the folder
	 * itself
	 */

	public static void deleteFolder(String bucketName, String folderName, AmazonS3 client) {
		List fileList = client.listObjects(bucketName, folderName).getObjectSummaries();
		for (Object object : fileList) {
			S3ObjectSummary file = (S3ObjectSummary) object;
			client.deleteObject(bucketName, file.getKey());
		}
		client.deleteObject(bucketName, folderName);
	}

	public static String convertBase64ToImage(String base64) {
		String decode[] = base64.split(",");
		String extensionDecode = decode[0], imageDecode = decode[1];
		String extension = null;
		switch (extensionDecode) {
		case "data:image/jpeg;base64":
			extension = "jpeg";
			break;
		case "data:image/png;base64":
			extension = "png";
			break;
		case "data:image/jpg;base64":
			extension = "jpg";
			break;
		}
		byte[] data = javax.xml.bind.DatatypeConverter.parseBase64Binary(imageDecode);
		String fileName = UUIDKeyGenerator.generateType1UUID();
		fileName += "." + extension;
		String path = GlobalConstants.FILE_PATH + fileName;
		logger.info("Filepath "+path);
		File file = new File(path);
		try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
			outputStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	public String saveImageToS3(String fileName) {
		AmazonS3 s3Client = CreateBucket.getAmazonClient();
		String awsFolderPath = GlobalConstants.FOLDER_NAME + GlobalConstants.SUFFIX + fileName;
		PutObjectResult result = s3Client.putObject(new PutObjectRequest(GlobalConstants.BUCKET_NAME, awsFolderPath,
				new File(GlobalConstants.FILE_PATH + fileName)).withCannedAcl(CannedAccessControlList.PublicRead));
		System.out
				.println(result.getExpirationTimeRuleId() + " " + result.getContentMd5() + " " + result.getMetadata());
		return s3Client.getUrl(GlobalConstants.BUCKET_NAME, awsFolderPath).toExternalForm();
	}

	public String getImageURLFromS3(String fileName) {
		AmazonS3 s3Client = CreateBucket.getAmazonClient();
		String awsFolderPath = GlobalConstants.FOLDER_NAME + GlobalConstants.SUFFIX + fileName;
		return s3Client.getUrl(GlobalConstants.BUCKET_NAME, awsFolderPath).toExternalForm();
	}

	public void getImageFromS3(String fileName) {
		AmazonS3 s3Client = CreateBucket.getAmazonClient();
		String awsFolderPath = GlobalConstants.FOLDER_NAME + GlobalConstants.SUFFIX + fileName;
		S3Object s3object = s3Client.getObject(GlobalConstants.BUCKET_NAME, awsFolderPath);
		S3ObjectInputStream inputStream = s3object.getObjectContent();
		// FileUtils.copyInputStreamToFile(inputStream, new
		// File("/Users/user/Desktop/hello.txt"));

	}

	public void getListOfObjectsAndBucketFromS3(String fileName) {
		AmazonS3 s3Client = CreateBucket.getAmazonClient();

		List<Bucket> buckets = s3Client.listBuckets();
		for (Bucket bucket : buckets) {
			System.out.println(bucket.getName());
		}

		ObjectListing objectListing = s3Client.listObjects(GlobalConstants.BUCKET_NAME);
		for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
			logger.info(os.getKey());
		}

	}

	public boolean deleteImageFromS3(String fileName) {
		AmazonS3 s3Client = CreateBucket.getAmazonClient();
		String awsFolderPath = GlobalConstants.FOLDER_NAME + GlobalConstants.SUFFIX + fileName;
		try {
			s3Client.deleteObject(GlobalConstants.BUCKET_NAME, awsFolderPath);
		} catch (Exception e) {
			System.out.println("Occured Error" + e);
			return false;
		}
		return true;

	}

	public boolean deleteMultipleImagesFromS3(String[] fileNameWithPath) {
		AmazonS3 s3Client = CreateBucket.getAmazonClient();
		try {
			DeleteObjectsRequest deleteObjectRequest = new DeleteObjectsRequest(GlobalConstants.BUCKET_NAME)
					.withKeys(fileNameWithPath);
			s3Client.deleteObjects(deleteObjectRequest);

		} catch (Exception e) {
			System.out.println("Occured Error" + e);
			return false;
		}
		return true;

	}
}
