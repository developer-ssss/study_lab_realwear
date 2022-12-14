package com.studylab.study_lab_realwear.datasource;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.studylab.study_lab_realwear.Result;

import java.io.File;
import java.util.List;

public class FirebaseDataSource implements DataSource{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public void uploadFile(File toUpload, String destination, CompletedCallback<Result<Uri>> callback) {
        Log.d("DEBUG:DataSource", "uploadFile: " + toUpload.getName() + " to " + destination);
        Uri localFile = Uri.fromFile(toUpload);
        StorageReference storageReference = firebaseStorage.getReference().child(destination);

        storageReference.putFile(localFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri result = task.getResult();
                            callback.onComplete(new Result.Success<Uri>(result));
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onComplete(new Result.Error(e));
                Log.d("DEBUG", "DataSource: storeImage() failed!");
            }
        });
    }

    public void downloadFile(String downloadPath, File localFile, CompletedCallback<Result> callback) {
        Log.d("DEBUG:DataSource", "downloadFile: " + downloadPath);
        StorageReference ref = firebaseStorage.getReference().child(downloadPath);

        ref.getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    callback.onComplete(new Result.Success<File>(localFile));
                } else {
                    callback.onComplete(new Result.Error(task.getException()));
                }
            }
        });
    }

    @Override
    public void getAnswer(CompletedCallback<Result<String>> callback) {
        db.collection("answer")
                .document("answer")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            callback.onComplete(new Result.Success<String>(document.getString("answer")));
                        }
                    }
                });
    }
}
