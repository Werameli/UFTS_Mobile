package net.werameli.ufts_mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    boolean logged;
    Button btnLogin;
    ImageButton btnLogout, btnRename, btnDownload, btnUpload, btnDelete, btnListFiles;
    EditText ownerusername, ownerpassword, ownerIP;
    FTPClient ftpClient;
    ListView listView;
    String namechecktext, passchecktext, ipchecktext;
    String adresult, pos;
    SharedPreferences.Editor editor;
    ArrayList<String> files;

//    public static String hashPassword(String plainTextPassword) {
//        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(10));          T.B.F in V2
//    }

    public static boolean sendPingRequest(String ipAddress)
            throws IOException {
        InetAddress checkable = InetAddress.getByName(ipAddress);
        if (checkable.isReachable(5000))
            return true;
        else
            return false;
    }

    public void ftplogon() {
        try {
            ftpClient = ftp.loginFtp(ipchecktext, 21, namechecktext, passchecktext);
            if (ftpClient.sendNoOp()) {
                if (!logged) {
                    prefswriter(ipchecktext, namechecktext, passchecktext);
                }
                mainPage();
                Toast.makeText(this, "Complete!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Invalid username or password!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.d("E", e.toString());
        }
    }


    public void prefswriter(String ip, String name, String pass) {
        editor.putBoolean("logged", true);
        editor.putString("ip", ip);
        editor.putString("name", name);
        editor.putString("password", pass);

        editor.commit();
    }

    public void isChecked() {

    }


    protected void loginFunc() throws Exception {
        // Obtaining user's credentials
        namechecktext = ownerusername.getText().toString();
        passchecktext = ownerpassword.getText().toString();
        ipchecktext = ownerIP.getText().toString();



        // Checking for empty fields
        if (namechecktext.equals("") || passchecktext.equals("") || ipchecktext.equals("")) {
            Toast.makeText(MainActivity.this, "Username, password or IP is empty!", Toast.LENGTH_SHORT).show();
        } else {
            if (sendPingRequest(ipchecktext))
            {
                Log.d("I", "Reachable");
            } else {
                Toast.makeText(this, "Unknown host!", Toast.LENGTH_SHORT).show();
            }
            ftplogon();

        }
    }

    public void refresher(ArrayAdapter adapter) throws IOException {
        listView.clearChoices();
        files = ftp.ftpPrintFilesList(ftpClient, "~");
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Files refreshed!", Toast.LENGTH_SHORT).show();
    }


    protected void mainPage() throws IOException {

        setContentView(R.layout.activity_main);

        // main page buttons
        btnLogout = findViewById(R.id.btn_logout);
        btnDelete = findViewById(R.id.btn_delete);
        btnRename = findViewById(R.id.btn_rename);
//        btnUpload = findViewById(R.id.btn_upload);
        btnDownload = findViewById(R.id.btn_download);
        btnListFiles = findViewById(R.id.btn_listfiles);
        listView = findViewById(R.id.list);



        files = ftp.ftpPrintFilesList(ftpClient,"~");
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.setItemChecked(position, true);
                pos = listView.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Selected: " + pos, Toast.LENGTH_SHORT).show();
            }
        });




        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File filesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File file = new File(filesDir, pos);
                    if (!file.exists()) {
                        if (!file.createNewFile()) {
                            Toast.makeText(MainActivity.this, "Failed to create local file!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    ftp.downloadFile(pos, file, ftpClient);
                    Toast.makeText(MainActivity.this, "Download completed!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

//        btnUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ftp.uploadFile(, , ftpClient);                           T.B.A in V2
//            }
//        });

        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Enter new name (with file extension):");

                    final EditText input = new EditText(MainActivity.this);

                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adresult = input.getText().toString();
                            Toast.makeText(MainActivity.this, adresult, Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                    ftp.renameFile(pos, adresult, ftpClient);
                    refresher(adapter);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String item = pos;
                    ftp.deleteFile(item, ftpClient);
                    Toast.makeText(MainActivity.this, "Selected file deleted successfully!", Toast.LENGTH_SHORT).show();
                    refresher(adapter);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ftpClient.disconnect();
                    editor.clear();
                    editor.commit();
                    Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
                    loginForm();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btnListFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    refresher(adapter);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });




    }

    protected void loginForm() throws IOException {
        setContentView(R.layout.loginwindow);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPref = getApplicationContext().getSharedPreferences("CredData", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        boolean logged = sharedPref.getBoolean("logged", false);



        // login form buttons
        btnLogin = findViewById(R.id.connect);
        ownerusername = findViewById(R.id.usrTB);
        ownerpassword = findViewById(R.id.pwdTB);
        ownerIP = findViewById(R.id.ipTB);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Main login function
                try {
                    loginFunc();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        if (logged) {
            ipchecktext = sharedPref.getString("ip", "null");
            namechecktext = sharedPref.getString("name", "null");
            passchecktext = sharedPref.getString("password", "null");
            ftplogon();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            loginForm();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}



