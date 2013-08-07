package net.etticat.dokabox.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.UserData;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;


public class WebServiceConnection {
	
	private Context context;
	private SharedPrefs sharedPrefs;
	private static final String NAMESPACE = "http://tempuri.org/";
	private static final String URL = "http://192.168.0.116/Dokabox-Proxy-Server/Dokabox.asmx";

	private static final String METHOD_LOGIN = "LoginUser";
	private static final String METHOD_DIRECTORYCONTENT = "DirectoryContent";
	private static final String METHOD_ROOTDIRECTORIES = "RootDirectories";
	private static final String METHOD_GETACCESSTOKEN = "GetAccessToken";
    
	public WebServiceConnection(Context context) {
		this.context = context;
		sharedPrefs = new SharedPrefs(context);
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
        
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        
        
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
			return getRootPaths(sharedPrefs.getUuid(), sharedPrefs.getUsername(), sharedPrefs.getAccessToken(), true);
		

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
        
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        
        
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
//                    	entry.setAlternationDate(responseEntry.getPropertyAsString("AlternationDate"));
                    }                    
                    if (responseEntry.hasProperty("ParentUnc")) {
                    	entry.setParentId(Integer.valueOf(responseEntry.getPropertyAsString("ParentUnc")));
                    }                    
                    if (responseEntry.hasProperty("Size")) {
                    	entry.setSize(Integer.valueOf(responseEntry.getPropertyAsString("Size")));
                    }                               
                    entry.setAlternationDate(new Date(0));
                    
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
//                    	entry.setAlternationDate(responseEntry.getPropertyAsString("AlternationDate"));
                    }                    
                    if (responseEntry.hasProperty("ParentUnc")) {
                    	entry.setParentId(Integer.valueOf(responseEntry.getPropertyAsString("ParentUnc")));
                    }                    
                    if (responseEntry.hasProperty("Size")) {
                    	entry.setSize(Integer.valueOf(responseEntry.getPropertyAsString("Size")));
                    }                               
                    entry.setAlternationDate(new Date(0));
                    entry.setType(FileSystemEntryType.FILE);
                    
                    result.add(entry);
                }
            }
        }
        catch(Exception e)
        {
        	
        	getAccessToken();
        	if(retry)
        		result = getDirectoryContent(id, false);
        }
		
		return result;
		
	}

	private void getAccessToken() {

		
		
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
        
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        
        
        try
        {
            androidHttpTransport.call(NAMESPACE + METHOD_GETACCESSTOKEN, envelope);
            String accessToken = envelope.getResponse().toString();
            
            
            sharedPrefs.setAccessToken(accessToken);

        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}

	private List<FileSystemEntry> getRootPaths(String uuid, String user,
			String accessToken, Boolean retry) {
		
		
		List<FileSystemEntry> result = null;
		
		
		SoapObject Request = new SoapObject(NAMESPACE, METHOD_ROOTDIRECTORIES);

		
		PropertyInfo pi = new PropertyInfo();
        pi.setName("iPadId");
        pi.setValue(uuid);
        pi.setType(String.class);
        Request.addProperty(pi);

        PropertyInfo pi2 = new PropertyInfo();
        pi2.setName("user");
        pi2.setValue(user);
        pi2.setType(String.class);
        Request.addProperty(pi2);

        PropertyInfo pi3 = new PropertyInfo();
        pi3.setName("accessToken");
        pi3.setValue(accessToken);
        pi3.setType(String.class);
        Request.addProperty(pi3);
		
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(Request);
        
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        
        
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
        catch(Exception e)
        {
        	getAccessToken();
        	if(retry)
        		result = getRootPaths(sharedPrefs.getUuid(), sharedPrefs.getUsername(), sharedPrefs.getAccessToken(), false);
        }
		return result;
	}
}
