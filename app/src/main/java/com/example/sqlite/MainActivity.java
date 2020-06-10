package com.example.sqlite;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.sqlite.PersonaContract.PersonaEntry;


public class MainActivity extends AppCompatActivity {

    ListView listView;
    PersonaDbHelper personaDbHelper;
    SQLiteDatabase db;
    Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);
        registerForContextMenu(listView);

        personaDbHelper = new PersonaDbHelper(this);
        db = personaDbHelper.getWritableDatabase();
        CargarPersonas();
    }

    public void CargarPersonas()
    {
        String [] columns = {
                PersonaEntry._ID,
                PersonaEntry.COLUMN_NAME_NAME,
                PersonaEntry.COLUMN_NAME_PHONE
        };

        cursor = db.query(PersonaEntry.TABLE_NAME, columns, null, null, null, null, null);

        String [] from = {
                PersonaEntry.COLUMN_NAME_NAME,
                PersonaEntry.COLUMN_NAME_PHONE
        };

        int [] to = {
                android.R.id.text1,
                android.R.id.text2
        };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, from, to, 0);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_menu_nuevo:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.add_edit_layout, null);
                final EditText etNombre = (EditText)v.findViewById(R.id.etNombre);
                final EditText etTelefono = (EditText)v.findViewById(R.id.etTelefono);

                builder.setTitle("Agregar registro");
                builder.setView(v);
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //codigo para agregar un nuevo registro
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PersonaEntry.COLUMN_NAME_NAME, etNombre.getText().toString());
                        contentValues.put(PersonaEntry.COLUMN_NAME_PHONE, etTelefono.getText().toString());

                        MainActivity.this.db.insert(PersonaEntry.TABLE_NAME, null, contentValues);
                        MainActivity.this.CargarPersonas();
                        Toast.makeText(MainActivity.this, "Registro agregado", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        //mover el cursor al registro que se mantuvo pulsado
        cursor.moveToPosition(info.position);
        final int _id = cursor.getInt(cursor.getColumnIndex(PersonaEntry._ID));

        switch (item.getItemId()) {
            case R.id.edit:


                //editar registro
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.add_edit_layout, null);
                final EditText etNombre = (EditText)v.findViewById(R.id.etNombre);
                final EditText etTelefono = (EditText)v.findViewById(R.id.etTelefono);

                //Obtener nombre y telefono del cursor y ponerlo en los EditText correspondientes
                String nombre = cursor.getString(cursor.getColumnIndex(PersonaEntry.COLUMN_NAME_NAME));
                String telefono = cursor.getString(cursor.getColumnIndex(PersonaEntry.COLUMN_NAME_PHONE));

                etNombre.setText(nombre);
                etTelefono.setText(telefono);

                builder.setTitle("Editar registro");
                builder.setView(v);
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //codigo para editar registro
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PersonaEntry.COLUMN_NAME_NAME, etNombre.getText().toString());
                        contentValues.put(PersonaEntry.COLUMN_NAME_PHONE, etTelefono.getText().toString());

                        String where = PersonaEntry._ID + " = '" + _id + "'";
                        MainActivity.this.db.update(PersonaEntry.TABLE_NAME, contentValues, where, null);
                        MainActivity.this.CargarPersonas();
                        Toast.makeText(MainActivity.this, "Registro editado", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
                return true;
            case R.id.delete:
                //borrar registro
                String where = PersonaEntry._ID + " = '" + _id + "'";
                MainActivity.this.db.delete(PersonaEntry.TABLE_NAME, where,null);
                MainActivity.this.CargarPersonas();
                Toast.makeText(MainActivity.this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        personaDbHelper.close();
        super.onDestroy();
    }
}