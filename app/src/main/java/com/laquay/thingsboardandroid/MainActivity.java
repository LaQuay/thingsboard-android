package com.laquay.thingsboardandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String baseURL = "https://demo.thingsboard.io/api";
    private static final String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";
    private String authToken;
    private SharedPreferences sharedPreferences;

    private TextView tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Resources related
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.text1);

        // Instantiate the SharedPreferences
        sharedPreferences = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        // Read AUTH token if exists
        authToken = sharedPreferences.getString(AUTH_TOKEN_KEY, "NO_KEY");

        // Draw in screen
        addDataToScreen();
    }

    private void addDataToScreen() {
        getDeviceTypes();
        getTelemetryValuesOfADevice("someType", "someDeviceId");
    }

    public void openLoginDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_login, null);

        final EditText loginET = dialogView.findViewById(R.id.username_login_dialog);
        final EditText passwordET = dialogView.findViewById(R.id.password_login_dialog);

        dialogBuilder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String username = loginET.getText().toString();
                        final String password = passwordET.getText().toString();
                        Log.d(TAG, "Username: " + username);
                        Log.d(TAG, "Password: " + password);

                        obtainThingsBoardToken(username, password);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void obtainThingsBoardToken(String username, String password) {
        String loginURL = baseURL + "/auth/login";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST, loginURL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());

                        try {
                            authToken = response.getString("token");
                            sharedPreferences.edit().putString(AUTH_TOKEN_KEY, authToken).apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), R.string.auth_ok, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                        Toast.makeText(getApplicationContext(), R.string.auth_fail, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        VolleyController.getInstance(this).addToQueue(jsonObjReq);
    }

    public void getDeviceTypes() {
        String URL = baseURL + "/device/types";

        JsonArrayRequest jsonArrReq = new JsonArrayRequest(Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        tv1.setText(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("X-Authorization", "Bearer " + authToken);
                return headers;
            }
        };
        VolleyController.getInstance(this).addToQueue(jsonArrReq);
    }

    private void getTelemetryValuesOfADevice(String deviceType, String deviceId) {
        String URL = baseURL + "/plugins/telemetry/" + deviceType + "/" + deviceId + "/values/timeseries";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("X-Authorization", "Bearer " + authToken);
                return headers;
            }
        };
        VolleyController.getInstance(this).addToQueue(jsonObjReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.login) {
            openLoginDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
