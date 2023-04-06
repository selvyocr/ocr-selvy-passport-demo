package com.selvasai.example.passport2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.innovatrics.mrz.MrzRecord;
import com.innovatrics.mrz.types.MrzDate;

import java.util.Calendar;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        buildMrzResult();
    }

    private void buildMrzResult() {
        TextView mrzType = (TextView) findViewById(R.id.type);
        TextView mrzIssuingCountry = (TextView) findViewById(R.id.issuing_country);
        TextView mrzPassportNo = (TextView) findViewById(R.id.passport_no);
        TextView mrzSurname = (TextView) findViewById(R.id.surname);
        TextView mrzGivenNames = (TextView) findViewById(R.id.given_names);
        TextView mrzDateOfBirth = (TextView) findViewById(R.id.date_of_birth);
        TextView mrzDateOfExpiry = (TextView) findViewById(R.id.date_of_expiry);

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        final MrzRecord mrzRecord = (MrzRecord) intent.getSerializableExtra("mrz");

        if (mrzRecord != null) {
            mrzType.setText(mrzRecord.code.toString());
            mrzIssuingCountry.setText(mrzRecord.issuingCountry);
            mrzPassportNo.setText(mrzRecord.documentNumber);
            mrzSurname.setText(mrzRecord.surname);
            mrzGivenNames.setText(mrzRecord.givenNames);
            mrzDateOfBirth.setText(mrzRecord.dateOfBirth.toMrz());
            mrzDateOfExpiry.setText(mrzRecord.expirationDate.toMrz());

            if (!expirationDateCheck(mrzRecord.expirationDate)) {
                TextView FieldMrzDateOfExpiry = (TextView) findViewById(R.id.field_date_of_expiry);
                FieldMrzDateOfExpiry.setBackgroundColor(Color.RED);
                mrzDateOfExpiry.setBackgroundColor(Color.RED);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

        super.onBackPressed();
    }

    private boolean expirationDateCheck(MrzDate expirationDate) {
        Calendar nowTime = Calendar.getInstance();

        if ((nowTime.get(Calendar.YEAR) % 100) > expirationDate.year) {
            return false;
        } else if ((nowTime.get(Calendar.YEAR) % 100) == expirationDate.year) {
            if (nowTime.get(Calendar.MONTH) + 1 > expirationDate.month) {
                return false;
            } else if (nowTime.get(Calendar.MONTH) + 1 == expirationDate.month) {
                if (nowTime.get(Calendar.DATE) > expirationDate.day) {
                    return false;
                }
            }
        }

        return true;
    }
}
