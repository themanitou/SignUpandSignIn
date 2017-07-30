package ca.skillsup.androidapp.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ca.skillsup.androidapp.app.AppConfig;
import ca.skillsup.androidapp.app.AppController;

/**
 * Created by manitou on 12/20/16.
 */

public class TransactionManager {
    private static final String TAG = TransactionManager.class.getSimpleName();
    public final static int TRANSACTION_RESULT_SUCCESS = 0;
    public final static int TRANSACTION_RESULT_FAILED = 1;

    public interface callBackListener {
        void onTransactionResult(int code, String message);
    }

    private static TransactionManager mInstance;
    private ProgressDialog progressDialog;

    private TransactionManager() { }

    public static TransactionManager getInstance() {
        if (mInstance == null) {
            mInstance = new TransactionManager();
        }
        return mInstance;
    }

    /**
     * Function to store class venue in MySQL database will post params(address, longitude,
     * latitude) to register url
     * */
    public void createVenue(final Context context, final String address, final Double longitude,
                            final Double latitude) {
        // Tag used to cancel the request
        String tag_string_req = "req_create_venue";

        final callBackListener mCallback = (callBackListener) context;

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Creating venue...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,  AppConfig.URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Create venue response: " + response);

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {
                                // User successfully stored in MySQL
                                String venue_id = jObj.getString("id");
                                Log.i(TAG, "Venue " + venue_id + " successfully created.");
                                mCallback.onTransactionResult(TRANSACTION_RESULT_SUCCESS, venue_id);
                            } else {
                                // Error occurred on Php/MySql side. Get the error
                                // message
                                String errorMsg = "Php/MySql error:\n" +
                                        jObj.getString("error_msg");
                                Log.e(TAG, errorMsg);
                                mCallback.onTransactionResult(TRANSACTION_RESULT_FAILED, errorMsg);
                            }
                        } catch (JSONException e) {
                            String errorStr = "JSONObject error:\n" + e.getMessage();
                            Log.e(TAG, errorStr);
                            mCallback.onTransactionResult(TRANSACTION_RESULT_FAILED, errorStr);
                        }
                        hideDialog();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorStr = "Volley error:\n" + error.getMessage();
                        Log.e(TAG, errorStr);
                        mCallback.onTransactionResult(TRANSACTION_RESULT_FAILED, errorStr);
                        hideDialog();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("address", address);
                params.put("longitude", String.valueOf(longitude));
                params.put("latitude", String.valueOf(latitude));

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
