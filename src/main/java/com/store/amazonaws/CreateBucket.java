package com.store.amazonaws;

import java.io.File;
import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import com.store.amazonaws.constant.GlobalConstants;

public class CreateBucket {

	private CreateBucket() {

	}

	private static AmazonS3 s3Client = null;

	public static AmazonS3 getAmazonClient() {
		if (s3Client != null) {
			return s3Client;
		}
		AWSCredentials credentials = new BasicAWSCredentials(GlobalConstants.ACCESS_KEY_ID,
				GlobalConstants.ACCESS_SEC_ID);
		s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.AP_SOUTH_1).build();
		return s3Client;
	}

	public static void main(String[] args) throws IOException {

		// create a client connection based on credentials
		// AmazonS3 s3client = new AmazonS3Client(credentials);

		/*
		 * TransferManager transferManager =
		 * TransferManagerBuilder.standard().withS3Client(s3Client)
		 * .withMinimumUploadPartSize((long) 5 * 1024 * 1024).build();
		 * 
		 * // create bucket - name must be unique for all S3 users //
		 * s3client.createBucket(bucketName); File file = new
		 * File("documents/my-picture.jpg"); Upload upload =
		 * transferManager.upload(bucketName, "", file); try {
		 * upload.waitForCompletion(); } catch (InterruptedException e) {
		 * System.out.println("exception"); }
		 */
		String bucketName = GlobalConstants.BUCKET_NAME;
		// create folder into bucket
		String folderName = GlobalConstants.FOLDER_NAME;
		// CommonService.createFolder(bucketName, folderName, s3client,
		// GlobalConstants.SUFFIX);

		// upload file to folder and set it to public
		String fileName = folderName + GlobalConstants.SUFFIX + GlobalConstants.FILE_NAME;
		s3Client.putObject(new PutObjectRequest(bucketName, fileName, new File(GlobalConstants.FILE_PATH))
				.withCannedAcl(CannedAccessControlList.PublicRead));
		ImageService.deleteFolder(bucketName, folderName, s3Client);
		s3Client.deleteBucket(bucketName);
	}

}