package com.jiahaoliuliu.runtimepermissionmanager;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple runtime permission tutorial based on this article
 * https://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Contacts";

    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insertDummyContactWrapper();
    }

    private void insertDummyContactWrapper() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsNeeded.add("GPS");
        }

        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS)) {
            permissionsNeeded.add("Read Contacts");
        }

        if (!addPermission(permissionsList, Manifest.permission.WRITE_CONTACTS)) {
            permissionsNeeded.add("Write Contacts");
        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Needed Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++) {
                    message = message + ", " + permissionsNeeded.get(i);
                }
                showMessageOkCancel(message, new DialogInterface.OnClickListener(){
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        String[] permissionsArray = permissionsList.toArray(new String[permissionsList.size()]);

                        ActivityCompat.requestPermissions(MainActivity.this, permissionsArray,
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                    }
                });
                return;
            }

            ActivityCompat.requestPermissions(MainActivity.this,
                permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);

            return;
        }
        insertDummyContact();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);

            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG, permission + "  granted");
                        Toast.makeText(MainActivity.this, permission + " granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.v(TAG, permission + " not granted");
                        Toast.makeText(MainActivity.this, permission + " not granted", Toast.LENGTH_SHORT).show();
                    }
                }

                //if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //    // Permission granted
                //    insertDummyContact();
                //} else {
                //    // Permission denied
                //    Toast.makeText(MainActivity.this, "WRITE_CONTACTS Denied", Toast.LENGTH_LONG).show();
                //}

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOkCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show();
    }

    private void insertDummyContact() {
        // Two operations are needed to insert a new contact
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        // First, set up a new raw contact
        ContentProviderOperation.Builder op =
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);

        operations.add(op.build());

        // Next, set the name for the contact.

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                "__DUMMY CONTACT from runtime permissions sample");

        operations.add(op.build());

        // Apply the operations.
        ContentResolver resolver = getContentResolver();
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            Log.d(TAG, "Could not add a new contact: " + e.getMessage());
        } catch (OperationApplicationException e) {
            Log.d(TAG, "Could not add a new contact: " + e.getMessage());
        }
    }
}
