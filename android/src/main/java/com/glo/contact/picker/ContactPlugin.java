package com.glo.contact.picker;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
 
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
 
import org.json.JSONArray;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@NativePlugin(
        requestCodes={ContactPlugin.REQUEST_CONTACTS}
)

public class ContactPlugin extends Plugin {
    protected static final int REQUEST_CONTACTS = 12345; // Unique request code
 
    @PluginMethod()
    public void getContacts(PluginCall call) {
        String value = call.getString("filter");
        // Filter based on the value if want
 
        saveCall(call);
        pluginRequestPermission(Manifest.permission.READ_CONTACTS, REQUEST_CONTACTS);
    }
 
    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
 
 
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            Log.d("Test", "No stored plugin call for permissions request result");
            return;
        }
 
        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                Log.d("Test", "User denied permission");
                return;
            }
        }
 
        if (requestCode == REQUEST_CONTACTS) {
            // We got the permission!
            pickMultipleContactAsync(savedCall);
        }
    }

    void pickMultipleContactAsync(PluginCall call) {
        try {
            Intent phonebookIntent = new Intent("intent.action.INTERACTION_TOPMENU");
            phonebookIntent.putExtra("additional", "phone-multi");
            phonebookIntent.putExtra("FromMMS", true);
            saveCall(call);
            startActivityForResult(call, phonebookIntent, REQUEST_CONTACTS);
        }  catch (Exception e) {
            Log.i("erorr  ==", "The erorr is "+ e);
            call.reject(e.getMessage());
            e.printStackTrace();
        }
    }

    // in order to handle the intents result, you have to @Override handleOnActivityResult
    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        // Get the previously saved call
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }
         if (requestCode == REQUEST_CONTACTS) {
            Log.i("data after ==", "The data is "+ data);
            // Do something with the data
            if(data != null)  {
                Bundle bundle =  data.getExtras();
                Log.i("bundle  ==", "The bundle is "+ bundle);
                ArrayList<String> contactList = bundle.getStringArrayList("result");
                ArrayList<String> contacts = new ArrayList<String>();
                for(int i=0; i<contactList.size(); i++) {
                    String x  = contactList.get(i);
                    String[] separated = x.split(";");
                    contacts.add(separated[1]);
                }
                loadContacts(savedCall,contacts);
            } else{
                savedCall.success();
            }
        }
    }
 
    void loadContacts(PluginCall call, ArrayList<String> contacts) {

        ArrayList<String> phonesList = contacts; // will work better if all phones in this list are in e164 format
        ContentResolver cr = this.getContext().getContentResolver();
        String[] projection = { ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER };
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " IN ('" + TextUtils.join("','", phonesList) + "') OR " +
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " IN ('" + TextUtils.join("','", phonesList) + "')";
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, null);

        ArrayList<Map> contactList = new ArrayList<>();

        while (cur != null && cur.moveToNext()) {
            Map<String,String> map =  new HashMap<String, String>();
            Long id = cur.getLong(0);
            String name = cur.getString(1);
            String phone = cur.getString(2);
            map.put("id", id.toString());
            map.put("displayName", name);
            map.put("phoneNumber", phone);
            contactList.add(map);
            Log.i("got  ==", "The id is "+ id);
            Log.i("got  ==", "The name is "+ name);
            Log.i("got  ==", "The phone is "+ phone);
        }
        
        JSONArray jsonArray = new JSONArray(contactList);
        JSObject ret = new JSObject();
        ret.put("results", jsonArray);
        call.success(ret);
    }
}