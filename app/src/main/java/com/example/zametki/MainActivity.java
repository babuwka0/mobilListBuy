package com.example.zametki;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbH;
    private SQLiteDatabase db;
    private Cursor c;
    private SimpleCursorAdapter ad;
    private ListView lv;
    private TextView statTxt;
    private EditText nameInp;
    private EditText priceInp;
    private Spinner typeSp;
    private Spinner sortFldSp;
    private Spinner sortOrdSp;
    private static final String[] sortCols = {
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_PRICE,
            DatabaseHelper.COLUMN_TYPE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbH = new DatabaseHelper(this);
        lv = findViewById(R.id.list);
        statTxt = findViewById(R.id.stats);
        nameInp = findViewById(R.id.input_name);
        priceInp = findViewById(R.id.input_price);
        typeSp = findViewById(R.id.spinner_type);
        sortFldSp = findViewById(R.id.spinner_sort_field);
        sortOrdSp = findViewById(R.id.spinner_sort_order);
        Button addBtn = findViewById(R.id.button_add);
        ArrayAdapter<CharSequence> typeAd = ArrayAdapter.createFromResource(
                this, R.array.purchase_types, android.R.layout.simple_spinner_item);
        typeAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSp.setAdapter(typeAd);

        ArrayAdapter<CharSequence> sortFldAd = ArrayAdapter.createFromResource(
                this, R.array.sort_fields, android.R.layout.simple_spinner_item);
        sortFldAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortFldSp.setAdapter(sortFldAd);

        ArrayAdapter<CharSequence> sortOrdAd = ArrayAdapter.createFromResource(
                this, R.array.sort_orders, android.R.layout.simple_spinner_item);
        sortOrdAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortOrdSp.setAdapter(sortOrdAd);

        AdapterView.OnItemSelectedListener rL = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        sortFldSp.setOnItemSelectedListener(rL);
        sortOrdSp.setOnItemSelectedListener(rL);

        addBtn.setOnClickListener(v -> addItem());
    }

    @Override
    protected void onResume() {
        super.onResume();
        db = dbH.getWritableDatabase();
        refreshList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (c != null) {
            c.close();
            c = null;
        }
        if (ad != null) {
            lv.setAdapter(null);
            ad = null;
        }
        if (db != null) {
            db.close();
            db = null;
        }
    }

    private void addItem() {
        if (db == null) {
            return;
        }
        String nm = nameInp.getText().toString().trim();
        String ps = priceInp.getText().toString().trim();
        String tp = typeSp.getSelectedItem().toString();
        double pr = ps.length() > 0 ? Double.parseDouble(ps) : 0;

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, nm);
        cv.put(DatabaseHelper.COLUMN_PRICE, pr);
        cv.put(DatabaseHelper.COLUMN_TYPE, tp);
        db.insert(DatabaseHelper.TABLE, null, cv);

        nameInp.setText("");
        priceInp.setText("");
        refreshList();
    }

    private void refreshList() {
        if (db == null) {
            return;
        }
        if (c != null) {
            c.close();
        }
        int fi = sortFldSp.getSelectedItemPosition();
        if (fi < 0) {
            fi = 0;
        }
        int oi = sortOrdSp.getSelectedItemPosition();
        String ord = oi == 1 ? "DESC" : "ASC";
        String col = sortCols[fi];
        String sql = "SELECT " + DatabaseHelper.COLUMN_ID + ", "
                + DatabaseHelper.COLUMN_NAME + ", "
                + DatabaseHelper.COLUMN_PRICE + ", "
                + DatabaseHelper.COLUMN_TYPE + " FROM "
                + DatabaseHelper.TABLE + " ORDER BY " + col + " " + ord;
        c = db.rawQuery(sql, null);

        if (ad == null) {
            ad = new SimpleCursorAdapter(this, R.layout.list_item_row, c,
                    new String[]{
                            DatabaseHelper.COLUMN_NAME,
                            DatabaseHelper.COLUMN_PRICE,
                            DatabaseHelper.COLUMN_TYPE
                    },
                    new int[]{R.id.row_name, R.id.row_price, R.id.row_type},
                    0);
            lv.setAdapter(ad);
        } else {
            ad.changeCursor(c);
        }

        updateStats();
    }

    private void updateStats() {
        Cursor cur = db.rawQuery("SELECT COUNT(*), IFNULL(SUM(" + DatabaseHelper.COLUMN_PRICE + "),0) FROM "
                + DatabaseHelper.TABLE, null);
        cur.moveToFirst();
        int cnt = cur.getInt(0);
        double sm = cur.getDouble(1);
        cur.close();
        statTxt.setText("Записей: " + cnt + ", сумма: " + sm);
    }
}
