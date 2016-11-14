package com.maplerise.cryptonames;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TextView;

import com.maplerise.cryptonames.api.ApiClient;
import com.maplerise.cryptonames.api.ApiInterface;
import com.maplerise.cryptonames.auth.Attestor;
import com.maplerise.cryptonames.model.NamesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.attr.id;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    private TextView namesView;

    private ApiInterface apiService;

    private Attestor attestor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "starting search activity");

        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        namesView = (TextView) findViewById(R.id.names);

        CryptoNamesApp app = (CryptoNamesApp) getApplicationContext();
        attestor = app.getAttestor();

        apiService = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public void onPause() {
        super.onPause();
        attestor.pause(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        attestor.resume(this);
    }

    // called when update button clicked
    public void onClickUpdate(View view) {
        Spinner tagSpinner = (Spinner)findViewById(R.id.tags);
        String tagValue = String.valueOf(tagSpinner.getSelectedItem());
        Log.d(TAG, "tagValue = " + tagValue);

        namesView.setText("...");

        Call<NamesResponse> call = apiService.getNames(tagValue);
        call.enqueue(new Callback<NamesResponse>() {
            @Override
            public void onResponse(Call<NamesResponse>call, Response<NamesResponse> response) {
                Log.d(TAG, "code = " + response.code());

                if (response.code() != 200) {
                    namesView.setText("not authorized");
                    return;
                }

                List<String> taggedNames = response.body().getResults();
                String tag = response.body().getTag();
                Log.d(TAG, "Number of names received: " + taggedNames.size() + " (\"" + tag + "\")");

                StringBuilder namesText = new StringBuilder();
                for (String name : taggedNames) {
                    namesText.append(name).append('\n');
                }

                namesView.setText(namesText);
            }

            @Override
            public void onFailure(Call<NamesResponse>call, Throwable t) {
                Log.e(TAG, t.toString());

                namesView.setText("names not found");
            }
        });
    }

    private static final int[] MODE_ID = {
            R.id.attest_off, R.id.attest_invalid, R.id.attest_valid,
            R.id.attest_expired, R.id.attest_on
    };

    private static final int[] MODE_STR = {
            R.string.attest_off, R.string.attest_invalid, R.string.attest_valid,
            R.string.attest_expired, R.string.attest_on
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_attest, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int mode = attestor.getMode();
        menu.findItem(MODE_ID[mode]).setChecked(true);
        namesView.setText(getText(MODE_STR[mode]));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.attest_on:
                if (!item.isChecked()) {
                    attestor.setMode(Attestor.ON);

                    item.setChecked(true);
                    namesView.setText(getText(R.string.attest_on));
                }
                return true;
            case R.id.attest_off:
                if (!item.isChecked()) {
                    attestor.setMode(Attestor.OFF);

                    item.setChecked(true);
                    namesView.setText(getText(R.string.attest_off));
                }
                return true;
            case R.id.attest_valid:
                if (!item.isChecked()) {
                    attestor.setMode(Attestor.VALID);

                    item.setChecked(true);
                    namesView.setText(getText(R.string.attest_valid));
                }
                return true;
            case R.id.attest_invalid:
                if (!item.isChecked()) {
                    attestor.setMode(Attestor.INVALID);

                    item.setChecked(true);
                    namesView.setText(getText(R.string.attest_invalid));
                }
                return true;
            case R.id.attest_expired:
                if (!item.isChecked()) {
                    attestor.setMode(Attestor.EXPIRED);

                    item.setChecked(true);
                    namesView.setText(getText(R.string.attest_expired));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

// end of file
