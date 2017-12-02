package com.juliao.adryel.arvore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class CadastroArvore extends AppCompatActivity {
    ImageView imagemView;

    //Arquivo de foto
    private String pictureImagePath = "";

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mArvoresDatabaseReference;

    File imgFile = null;

    EditText nome, descricao, altura, especie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_arvore);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        ab.setTitle("Cadastro de Árvores");
        ab.setDisplayHomeAsUpEnabled(true);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("cadastro_arvores");

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mArvoresDatabaseReference = mFirebaseDatabase.getReference().child("arvore");

        if (checkStorage() == false){
            Toast.makeText(this, "MASSA", Toast.LENGTH_SHORT).show();
            return;
        }else {
            Intent i = getIntent();
            String arquivo = i.getStringExtra("imagename");
            Log.i("TESTES", ""+ arquivo);
            try {
                imgFile = createImageFile(arquivo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("Imagem", imgFile.getAbsolutePath());
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = (ImageView) findViewById(R.id.takeFoto);
                Log.i("Testes","Arquivo:"+ imgFile);
                myImage.setImageBitmap(myBitmap);
                myImage.setRotation(90);
            }
        }

        nome = (EditText) findViewById(R.id.textnome);
        descricao = (EditText) findViewById(R.id.textDescricao);
        especie = (EditText) findViewById(R.id.textEspecie);
        altura = (EditText) findViewById(R.id.textAltura);
    }

    private File createImageFile(String filename) throws IOException {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        pictureImagePath = storageDir.getAbsolutePath()+"/"+filename;
        File image = new File(pictureImagePath);

        return image;
    }

    public boolean checkStorage(){
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            return false;
        }
        return true;
    }


    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public void confirmarCadastroArvore(View v){
        Uri selectedImageUri = Uri.fromFile(imgFile);
        StorageReference photoref = mStorageReference.child(selectedImageUri.getLastPathSegment());

        photoref.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(),"Cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                Uri downaloaduri = taskSnapshot.getDownloadUrl();

                Arvore arvore = new Arvore.ArvoreBuilder(nome.getText().toString(), 0, 0, downaloaduri.toString(), altura.getText().toString()).builder();
                mArvoresDatabaseReference.push().setValue(arvore);

                Log.i("testes", arvore.toString());
            }
        });
    }
}
