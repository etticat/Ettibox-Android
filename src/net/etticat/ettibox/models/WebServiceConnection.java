package net.etticat.ettibox.models;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
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

import net.etticat.ettibox.LoginActivity;
import net.etticat.ettibox.R;
import net.etticat.ettibox.dbmodels.EntryDbHandler;
import net.etticat.ettibox.dto.FileSystemEntry;
import net.etticat.ettibox.dto.FileSystemEntry.FileSystemEntryType;
import net.etticat.ettibox.dto.UserData;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;


public class WebServiceConnection {


	private static String NAMESPACE;
	private static String URL;
	private static String WEBSERVICE_URL;
	private static String DOWNLOAD_URL;
	private static String UPLOAD_URL;

	private static final String METHOD_LOGIN = "LoginUser";
	private static final String METHOD_DIRECTORYCONTENT = "DirectoryContent";
	private static final String METHOD_ROOTDIRECTORIES = "RootDirectories";
	private static final String METHOD_GETACCESSTOKEN = "GetAccessToken";
	private static final String METHOD_DELETE = "DeleteFile";
	private static final String METHOD_CREATE_FOLDER = "CreateDirectory";

	private static final int TIMEOUT = 3000;

	private EntryDbHandler mEntryDbHandler;
	private ConnectivityManager mConnectivityManager;

	public WebServiceConnection() {
		mEntryDbHandler = EntryDbHandler.getInstance();
		mConnectivityManager =
				(ConnectivityManager)ContextProvider.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	{
		Resources r = ContextProvider.getContext().getResources();
		NAMESPACE = r.getString(R.string.server_connection_namespace);
		URL = r.getString(R.string.server_connection_url);
		WEBSERVICE_URL = URL + r.getString(R.string.server_connection_webservice);
		DOWNLOAD_URL = URL + r.getString(R.string.server_connection_download);
		UPLOAD_URL = URL + r.getString(R.string.server_connection_upload);
	}

	public UserData login(String user, String password){
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting())
			return null;

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
		pi3.setValue(SharedPrefs.getUuid());
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

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL, TIMEOUT);


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
			SharedPrefs.setUsername(userData.getName());
			SharedPrefs.setAccessToken(userData.getAccessToken());
			SharedPrefs.setEncryptedPassword(userData.getEncryptedPassword());	
		}
		return userData;

	}
	public List<FileSystemEntry> getDirectoryContent(Integer id, Boolean retry){
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting())
			return null;

		if(id == 0)
			return getRootPaths(true);


		List<FileSystemEntry> result = null;

		SoapObject Request = new SoapObject(NAMESPACE, METHOD_DIRECTORYCONTENT);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(SharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(SharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("accessToken");
		pi3.setValue(SharedPrefs.getAccessToken());
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

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL, TIMEOUT);


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
						entry.setSize(Long.valueOf(responseEntry.getPropertyAsString("Size")));
					}                               
					entry.setType(FileSystemEntryType.FILE);
					entry.setParentId(id);

					result.add(entry);
				}
			}
		}
		catch(IOException e)
		{

			if(e != null && e.getMessage() != null && e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					result = getDirectoryContent(id, false);
			}
		} catch (XmlPullParserException e) {

		}

		return result;

	}

	private Boolean getAccessToken() {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting())
			return null;

		SoapObject Request = new SoapObject(NAMESPACE, METHOD_GETACCESSTOKEN);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(SharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(SharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("passwordKey");
		pi3.setValue(SharedPrefs.getEncryptedPassword());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL, TIMEOUT);


		try
		{
			androidHttpTransport.call(NAMESPACE + METHOD_GETACCESSTOKEN, envelope);
			String accessToken = envelope.getResponse().toString();


			SharedPrefs.setAccessToken(accessToken);

		}
		catch(IOException e)
		{

			if(e != null && e.getMessage() != null && e.getMessage().equals("No authentication challenges found")){				
				SharedPrefs.setUsername("");
				SharedPrefs.setEncryptedPassword("");
				SharedPrefs.setAccessToken("");

				Intent intent = new Intent(ContextProvider.getContext(), LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				ContextProvider.getContext().startActivity(intent);
			}
			return false;
		} catch (XmlPullParserException e) {
			return false;
		}
		return true;
	}

	private List<FileSystemEntry> getRootPaths(Boolean retry) {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting())
			return null;

		List<FileSystemEntry> result = null;


		SoapObject Request = new SoapObject(NAMESPACE, METHOD_ROOTDIRECTORIES);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(SharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(SharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("accessToken");
		pi3.setValue(SharedPrefs.getAccessToken());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL, TIMEOUT);


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

			if(e != null && e.getMessage() != null && e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					result = getRootPaths(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}


	public Boolean download(FileSystemEntry entry, OnFileTransferProgressHandler onDownloadProgress, Boolean retry){
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting()) {
			return false;
		}

		File path = mEntryDbHandler.getPath(entry);

		path.mkdirs();
		File file = new File(path, entry.getName());


		try {

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(DOWNLOAD_URL);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("unc", "" + entry.getId()));
			nameValuePairs.add(new BasicNameValuePair("iPadId", SharedPrefs.getUuid() ));
			nameValuePairs.add(new BasicNameValuePair("user", SharedPrefs.getUsername()));
			nameValuePairs.add(new BasicNameValuePair("accessToken", SharedPrefs.getAccessToken()));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);



			//Define InputStreams to read from the URLConnection.
			// uses 3KB download buffer
			InputStream is = response.getEntity().getContent();
			BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 4);
			FileOutputStream outStream = new FileOutputStream(file);
			byte[] buff = new byte[256 * 1024];

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

			mEntryDbHandler.replaceEntry(entry);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {

			if(e != null && e.getMessage() != null && e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					return download(entry, onDownloadProgress, false);
			}
			return false;
		}
		return true;

	}



	public int uploadFile(FileSystemEntry parentEntry, OnUploadProgressHandler uploadProgressHandler) {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting()){
			uploadProgressHandler.onUploadError(ContextProvider.getContext().getResources().getString(R.string.upload_service_no_network_available ));
			return -1;
		}
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
		try { 
			ContentResolver cR = ContextProvider.getContext().getContentResolver();


			InputStream fileInputStream = cR.openInputStream(parentEntry.getUri());

			int filesize = fileInputStream.available();
			URL url = new URL(UPLOAD_URL + 
					"?iPadId="  +  Uri.encode(SharedPrefs.getUuid()) + 
					"&user=" + Uri.encode(SharedPrefs.getUsername()) + 
					"&accessToken=" + Uri.encode(SharedPrefs.getAccessToken()) + 
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
				uploadProgressHandler.progressChanged(parentEntry, (filesize - bytesAvailable)*100/filesize);
			}
			dos.writeBytes(lineEnd);			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);			fileInputStream.close();			dos.flush();			dos.close();


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
		public void progressChanged(FileSystemEntry entry, Integer percent);
	}

	public Boolean createFolder(Integer parentId, String name, boolean retry) {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting())
			return null;

		Boolean success = false;

		SoapObject Request = new SoapObject(NAMESPACE, METHOD_CREATE_FOLDER);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(SharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(SharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("accessToken");
		pi3.setValue(SharedPrefs.getAccessToken());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		PropertyInfo pi4 = new PropertyInfo();
		pi4.setName("parentUnc");
		pi4.setValue(parentId);
		pi4.setType(Integer.class);
		Request.addProperty(pi4);

		PropertyInfo pi5 = new PropertyInfo();
		pi5.setName("name");
		pi5.setValue(name);
		pi5.setType(String.class);
		Request.addProperty(pi5);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL, TIMEOUT);


		try
		{
			androidHttpTransport.call(NAMESPACE + METHOD_CREATE_FOLDER, envelope);

			success = true;

		}
		catch(IOException e)
		{

			if(e != null && e.getMessage() != null && e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					success  = createFolder(parentId, name, false);
			}
		} catch (Exception e) {

		}
		return success;
	}

	public Boolean deleteItem(int id, boolean retry) {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if(!activeNetwork.isConnectedOrConnecting())
			return null;

		Boolean success = false;

		SoapObject Request = new SoapObject(NAMESPACE, METHOD_DELETE);


		PropertyInfo pi = new PropertyInfo();
		pi.setName("iPadId");
		pi.setValue(SharedPrefs.getUuid());
		pi.setType(String.class);
		Request.addProperty(pi);

		PropertyInfo pi2 = new PropertyInfo();
		pi2.setName("user");
		pi2.setValue(SharedPrefs.getUsername());
		pi2.setType(String.class);
		Request.addProperty(pi2);

		PropertyInfo pi3 = new PropertyInfo();
		pi3.setName("accessToken");
		pi3.setValue(SharedPrefs.getAccessToken());
		pi3.setType(String.class);
		Request.addProperty(pi3);

		PropertyInfo pi4 = new PropertyInfo();
		pi4.setName("unc");
		pi4.setValue(id);
		pi4.setType(Integer.class);
		Request.addProperty(pi4);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(Request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(WEBSERVICE_URL, TIMEOUT);


		try
		{
			androidHttpTransport.call(NAMESPACE + METHOD_DELETE, envelope);
			success = true;

		}
		catch(IOException e)
		{

			if(e != null && e.getMessage() != null && e.getMessage().equals("No authentication challenges found")){
				if(getAccessToken() && retry)
					success  = deleteItem(id, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

}
