package net.etticat.dokabox.models;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.etticat.dokabox.LoginActivity;
import net.etticat.dokabox.dbmodels.EntryDbHandler;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.UserData;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;


public class WebServiceConnection {

	private Context context;
	private SharedPrefs sharedPrefs;
	private EntryDbHandler entryDbHandler;

	private static final String NAMESPACE = "http://tempuri.org/";
	private static final String URL = "http://192.168.0.116/Dokabox-Proxy-Server/";
	private static final String WEBSERVICE_URL = URL + "Dokabox.asmx";
	private static final String DOWNLOAD_URL = URL + "Download.aspx";
	private static final String UPLOAD_URL = URL + "Upload.aspx";

	private static final String METHOD_LOGIN = "LoginUser";
	private static final String METHOD_DIRECTORYCONTENT = "DirectoryContent";
	private static final String METHOD_ROOTDIRECTORIES = "RootDirectories";
	private static final String METHOD_GETACCESSTOKEN = "GetAccessToken";

	public WebServiceConnection(Context context) {
		this.context = context;
		sharedPrefs = new SharedPrefs(context);
		entryDbHandler = new EntryDbHandler(context);
	}

	public UserData login(String user, String password){
		UserData userData = new UserData();
		String pushToken = "\\";

		SoapObject Request = new SoapObject(NAMESPACE, METHOD_LOGIN);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("user");
		pi.setValue(user);
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("password");
		pi2.setValue(password);
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("iPadId");
		pi3.setValue(sharedPrefs.getUuid());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		PropertyInfo pi4 = new PropertyInfo();
		pi4.setName("pushToken");
		pi4.setValue(pushToken);
		pi4.setType(String.class);
		Request.addProperty(pi4);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL);


		try
		{
			androidHttpTransport.call(NAMESPACE + METHOD_LOGIN, envelope);
			SoapObject response = (SoapObject)envelope.getResponse();



			if (response.hasProperty("Id")) {
				userData.setId(Integer.valueOf(response.getPropertyAsString("Id")));
			}
			if (response.hasProperty("Name")) {
				userData.setName(response.getPropertyAsString("Name"));
			}
			if (response.hasProperty("PasswordKey")) {
				userData.setPasswordKey(response.getPropertyAsString("PasswordKey"));
			}
			if (response.hasProperty("EncryptedPassword")) {
				userData.setEncryptedPassword(response.getPropertyAsString("EncryptedPassword"));
			}
			if (response.hasProperty("PushToken")) {
				userData.setPushToken(response.getPropertyAsString("PushToken"));
			}
			if (response.hasProperty("AccessToken")) {
				userData.setAccessToken(response.getPropertyAsString("AccessToken"));
			}
			if (response.hasProperty("AccessTokenValidity")) {
				userData.setAccessTokenValidity(response.getPropertyAsString("AccessTokenValidity"));
			}
			if (response.hasProperty("IpadId")) {
				userData.setiPadId(response.getPropertyAsString("IpadId"));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
		if(userData.getName() != null){
			sharedPrefs.setUsername(userData.getName());
			sharedPrefs.setAccessToken(userData.getAccessToken());
			sharedPrefs.setEncryptedPassword(userData.getEncryptedPassword());	
		}
		return userData;

	}
	public List<FileSystemEntry> getDirectoryContent(Integer id, Boolean retry){


		if(id == 0)
			return getRootPaths(true);


		List<FileSystemEntry> result = null;




		SoapObject Request = new SoapObject(NAMESPACE, METHOD_DIRECTORYCONTENT);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(sharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(sharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("accessToken");
		pi3.setValue(sharedPrefs.getAccessToken());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		PropertyInfo pi4 = new PropertyInfo();
		pi4.setName("unc");
		pi4.setValue(id);
		pi4.setType(int.class);
		Request.addProperty(pi4);


		PropertyInfo pi5 = new PropertyInfo();
		pi5.setName("depth");
		pi5.setValue(1);
		pi5.setType(int.class);
		Request.addProperty(pi5);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL);


		try
		{
			androidHttpTransport.call(NAMESPACE + METHOD_DIRECTORYCONTENT, envelope);
			SoapObject response = (SoapObject)envelope.getResponse();


			SoapObject directories = (SoapObject) response.getProperty("SmbDirectories");
			SoapObject files = (SoapObject) response.getProperty("SmbFiles");

			int totalCount = directories.getPropertyCount();

			result = new ArrayList<FileSystemEntry>();
			if (totalCount > 0 ) {
				for (int detailCount = 0; detailCount < totalCount; detailCount++) {
					SoapObject responseEntry = (SoapObject) directories.getProperty(detailCount);
					FileSystemEntry entry = new FileSystemEntry();

					if (responseEntry.hasProperty("Id")) {
						entry.setId(Integer.valueOf(responseEntry.getPropertyAsString("Id")));
					}

					if (responseEntry.hasProperty("Name")) {
						entry.setName(responseEntry.getPropertyAsString("Name"));
					}
					if (responseEntry.hasProperty("SyncSubscribed")) {
						entry.setSyncSubscribed(Boolean.valueOf(responseEntry.getPropertyAsString("SyncSubscribed")));
					}                    
					if (responseEntry.hasProperty("AlternationDate")) {
						
						String test = responseEntry.getPropertyAsString("AlternationDate");
						try {
							entry.setAlternationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(
									responseEntry.getPropertyAsString("AlternationDate")));
						} catch (ParseException e) {
							try {
								entry.setAlternationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(
										responseEntry.getPropertyAsString("AlternationDate")));
							} catch (ParseException e1) {
								entry.setAlternationDate(new Date(0));
							}
						}
					}                     
					if (responseEntry.hasProperty("Size")) {
						entry.setSize(Integer.valueOf(responseEntry.getPropertyAsString("Size")));
					}                               
					entry.setParentId(id);
					entry.setType(FileSystemEntryType.FOLDER);

					result.add(entry);
				}
			}

			totalCount = files.getPropertyCount();
			if (totalCount > 0 ) {
				for (int detailCount = 0; detailCount < totalCount; detailCount++) {
					SoapObject responseEntry = (SoapObject) files.getProperty(detailCount);
					FileSystemEntry entry = new FileSystemEntry();

					if (responseEntry.hasProperty("Id")) {
						entry.setId(Integer.valueOf(responseEntry.getPropertyAsString("Id")));
					}

					if (responseEntry.hasProperty("Name")) {
						entry.setName(responseEntry.getPropertyAsString("Name"));
					}
					if (responseEntry.hasProperty("SyncSubscribed")) {
						entry.setSyncSubscribed(Boolean.valueOf(responseEntry.getPropertyAsString("SyncSubscribed")));
					}                    
					if (responseEntry.hasProperty("AlternationDate")) {
						try {
							entry.setAlternationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(
									responseEntry.getPropertyAsString("AlternationDate")));
						} catch (ParseException e) {
							try {
								entry.setAlternationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(
										responseEntry.getPropertyAsString("AlternationDate")));
							} catch (ParseException e1) {
								entry.setAlternationDate(new Date(0));
							}
						}
					}                    
					if (responseEntry.hasProperty("Size")) {
						entry.setSize(Integer.valueOf(responseEntry.getPropertyAsString("Size")));
					}                               
					entry.setType(FileSystemEntryType.FILE);
					entry.setParentId(id);

					result.add(entry);
				}
			}
		}
		catch(IOException e)
		{

			if(e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					result = getDirectoryContent(id, false);
			}
		} catch (XmlPullParserException e) {

		}

		return result;

	}

	private Boolean getAccessToken() {



		SoapObject Request = new SoapObject(NAMESPACE, METHOD_GETACCESSTOKEN);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(sharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(sharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("passwordKey");
		pi3.setValue(sharedPrefs.getEncryptedPassword());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL);


		try
		{
			androidHttpTransport.call(NAMESPACE + METHOD_GETACCESSTOKEN, envelope);
			String accessToken = envelope.getResponse().toString();


			sharedPrefs.setAccessToken(accessToken);

		}
		catch(IOException e)
		{
			if(e.getMessage().equals("No authentication challenges found")){
				sharedPrefs.setUsername("");
				sharedPrefs.setEncryptedPassword("");
				sharedPrefs.setAccessToken("");

				Intent intent = new Intent(context, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			}
			return false;
		} catch (XmlPullParserException e) {
			return false;
		}
		return true;
	}

	private List<FileSystemEntry> getRootPaths(Boolean retry) {


		List<FileSystemEntry> result = null;


		SoapObject Request = new SoapObject(NAMESPACE, METHOD_ROOTDIRECTORIES);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(sharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(sharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("accessToken");
		pi3.setValue(sharedPrefs.getAccessToken());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL);


		try
		{
			androidHttpTransport.call(NAMESPACE + METHOD_ROOTDIRECTORIES, envelope);
			SoapObject response = (SoapObject)envelope.getResponse();

			int totalCount = response.getPropertyCount();
			result = new ArrayList<FileSystemEntry>();
			if (totalCount > 0 ) {
				for (int detailCount = 0; detailCount < totalCount; detailCount++) {
					SoapObject responseEntry = (SoapObject) response.getProperty(detailCount);
					FileSystemEntry entry = new FileSystemEntry();

					if (responseEntry.hasProperty("Id")) {
						String id = responseEntry.getPropertyAsString("Id");
						entry.setId(Integer.valueOf(id));
					}

					if (responseEntry.hasProperty("Name")) {
						entry.setName(responseEntry.getPropertyAsString("Name"));
					}
					if (responseEntry.hasProperty("SyncSubscribed")) {
						entry.setSyncSubscribed(Boolean.valueOf(responseEntry.getPropertyAsString("SyncSubscribed")));
					}                    
					entry.setAlternationDate(new Date(0));
					entry.setSize(0);
					entry.setType(FileSystemEntryType.FOLDER);


					result.add(entry);
				}
			}
		}
		catch(IOException e)
		{

			if(e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					result = getRootPaths(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}


	public void download(FileSystemEntry entry, OnFileTransferProgressHandler onDownloadProgress, Boolean retry){


		File path = entryDbHandler.getPath(entry);

		path.mkdirs();
		File file = new File(path, entry.getName());


		try {

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(DOWNLOAD_URL);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("unc", "" + entry.getId()));
			nameValuePairs.add(new BasicNameValuePair("iPadId", sharedPrefs.getUuid() ));
			nameValuePairs.add(new BasicNameValuePair("user", sharedPrefs.getUsername()));
			nameValuePairs.add(new BasicNameValuePair("accessToken", sharedPrefs.getAccessToken()));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);



			//Define InputStreams to read from the URLConnection.
			// uses 3KB download buffer
			InputStream is = response.getEntity().getContent();
			BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 4);
			FileOutputStream outStream = new FileOutputStream(file);
			byte[] buff = new byte[4 * 1024];

			//Read bytes (and store them) until there is nothing more to read(-1)
			long totalLength = response.getEntity().getContentLength();

			long downloadedLength = 0;

			int restLength;
			while ((restLength = inStream.read(buff)) != -1)
			{
				downloadedLength += restLength;
				outStream.write(buff,0,restLength);

				Integer percent = (int) ((100*downloadedLength/totalLength));
				onDownloadProgress.progressChanged(entry, percent);

			}

			//clean up
			outStream.flush();
			outStream.close();
			inStream.close();
			
			
			entry.setDownloadedDate(new Date(file.lastModified()));
			entry.setDownloadedAlternationDate(entry.getAlternationDate());
			
			entryDbHandler.replaceEntry(entry);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {

			if(e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					download(entry, onDownloadProgress, false);
			}
		}

	}



	public int uploadFile(FileSystemEntry parentEntry, OnUploadProgressHandler uploadProgressHandler) {


		Boolean uploadRunning = false;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;  
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024; 
		File sourceFile = new File(parentEntry.getUri().getPath()); 

		String fileName = sourceFile.getName();
		
		if (!sourceFile.isFile()) {

			return 0;

		}		try { 
			FileInputStream fileInputStream = new FileInputStream(sourceFile);			URL url = new URL(UPLOAD_URL + 
					"?iPadId="  +  Uri.encode(sharedPrefs.getUuid()) + 
					"&user=" + Uri.encode(sharedPrefs.getUsername()) + 
					"&accessToken=" + Uri.encode(sharedPrefs.getAccessToken()) + 
					"&parentUnc=" + parentEntry.getId());
			
			conn = (HttpURLConnection) url.openConnection(); 			conn.setDoInput(true); 			conn.setDoOutput(true); 			conn.setUseCaches(false); 			conn.setRequestMethod("POST");			conn.setRequestProperty("Connection", "Keep-Alive");			conn.setRequestProperty("ENCTYPE", "multipart/form-data");			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			
			dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + lineEnd); 			dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);
			bytesAvailable = fileInputStream.available(); 
			bufferSize = Math.min(bytesAvailable, maxBufferSize);			buffer = new byte[bufferSize];
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);				bytesAvailable = fileInputStream.available();				bufferSize = Math.min(bytesAvailable, maxBufferSize);				bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
				if(!uploadRunning){
					uploadRunning = true;
					uploadProgressHandler.onUploadStart();
				}
			}
			dos.writeBytes(lineEnd);			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			int serverResponseCode = conn.getResponseCode();			String serverResponseMessage = conn.getResponseMessage();

			fileInputStream.close();			dos.flush();			dos.close();
			
			
		}
		catch (Exception e){ 
			uploadProgressHandler.onUploadError(e.getMessage());
		}
		return 1;


	}

	public interface OnFileTransferProgressHandler {
		public void progressChanged(FileSystemEntry entry, Integer percent);
	}

	public interface OnUploadProgressHandler {
		public void onUploadStart();
		public void onUploadError(String message);
	}

}
