package GroupProject.Team8.DiseaseOutbreakMonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import com.tbruyelle.rxpermissions3.RxPermissions;

public class ConfirmActivity extends AppCompatActivity {

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private static final int PERMISSIONS_COARSE_LOCATION = 99;
    long date;
    PatientModel patient = new PatientModel();
    TextView textViewName, textViewDOB, textViewSex, textViewTemperature, textViewBloodPressure,
            textViewSymptoms, textViewComments, textViewDiagnosis;
    TextView textViewPopup;
    Button buttonPopup;

    //LocationManager locationManager;

    // Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Unix time
        date = System.currentTimeMillis() / 1000L;

        Intent intent = getIntent();
        patient.setName(intent.getStringExtra(Constants.NAME));
        patient.setDateOfBirth(intent.getStringExtra(Constants.DOB));
        patient.setSex(intent.getStringExtra(Constants.SEX));
        patient.setTemperatureCelsius(intent.getDoubleExtra(Constants.TEMP, -1));
        patient.setBloodPressureSystolic(intent.getIntExtra(Constants.BP_SYSTOLIC, -1));
        patient.setBloodPressureDiastolic(intent.getIntExtra(Constants.BP_DIASTOLIC, -1));
        patient.setDisease(intent.getStringExtra(Constants.HW_DIAGNOSIS));
        patient.setSymptoms(intent.getStringExtra(Constants.SYMPTOMS));
        if (intent.getStringExtra(Constants.COMMENT) == null) {
            patient.setComment("");
        } else {
            patient.setComment(intent.getStringExtra(Constants.COMMENT));
        }

        textViewName = findViewById(R.id.textNameFillable);
        textViewDOB = findViewById(R.id.textDOBFillable);
        textViewSex = findViewById(R.id.textSexFillable);
        textViewTemperature = findViewById(R.id.textTempFillable);
        textViewBloodPressure = findViewById(R.id.textBPFillable);
        textViewSymptoms = findViewById(R.id.textSymptomsFillable);
        textViewComments = findViewById(R.id.textCommentFillable);
        textViewDiagnosis = findViewById(R.id.textDiagnosisFillable);
        fillSummaryTable();

        patient.setDate(date);
        final RxPermissions permissions = new RxPermissions(this);
        permissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                   if (granted) {   // automatically granted for api version < 23
                       updateGPS();
                     }
                   else {
                        // permission not granted
                       Log.i("permission: ", "not granted");
                   }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), DiagnosisActivity.class);
        intent.putExtra(Constants.NAME, patient.getName());
        intent.putExtra(Constants.DOB, patient.getDateOfBirth());
        intent.putExtra(Constants.SEX, patient.getSex());
        intent.putExtra(Constants.BP_SYSTOLIC, patient.getBloodPressureSystolic());
        intent.putExtra(Constants.BP_DIASTOLIC, patient.getBloodPressureDiastolic());
        intent.putExtra(Constants.SYMPTOMS, patient.getSymptoms());
        intent.putExtra(Constants.TEMP, patient.getTemperatureCelsius());
        startActivity(intent);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "Please enable GPS so this patient data can be stored", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ConfirmActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    saveLatAndLong(location);
                }
            });
        }
        else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_COARSE_LOCATION);
            }
        }
    }

    private void saveLatAndLong(Location location) {
        if (location != null) {
            patient.setLatitude(location.getLatitude());
            patient.setLongitude(location.getLongitude());
        }

    }

    public void fillSummaryTable() {
        textViewName.setText(patient.getName());
        textViewDOB.setText(patient.getDateOfBirth());
        textViewSex.setText(patient.getSex());
        textViewTemperature.setText(String.valueOf(patient.getTemperatureCelsius()));
        int bpSystolic = patient.getBloodPressureSystolic();
        int bpDiastolic = patient.getBloodPressureDiastolic();
        String bloodPressure = bpSystolic + "/" + bpDiastolic;
        textViewBloodPressure.setText(bloodPressure);
        textViewSymptoms.setText(patient.getSymptoms().replace(",", ", "));
        textViewComments.setText(patient.getComment());
        textViewDiagnosis.setText(patient.getDisease());
    }

    public void createNewDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.popup, null);
        textViewPopup = popupView.findViewById(R.id.textViewPopupTitle);
        textViewPopup.setText(R.string.text_popup);
        buttonPopup = (Button) popupView.findViewById(R.id.textViewPopupButton);
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();

        buttonPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

    public void addToDatabase(View view) {
        DatabaseHelper dbHelper = new DatabaseHelper(ConfirmActivity.this);
        if(dbHelper.addOne(patient)) {
            patient.printPatientDetails();
            createNewDialog();
        }
        else {
            Toast.makeText(ConfirmActivity.this, "Error adding user to database. Try again in a moment.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}