package com.techtop.onehyreapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class DAOTripData
{
       private DatabaseReference databaseReference;
       public DAOTripData(){
           FirebaseDatabase db = FirebaseDatabase.getInstance();
           databaseReference = db.getReference(TripData.class.getSimpleName());
       }

       public Task<Void> add(TripData td){

          return databaseReference.push().setValue(td);
       }

      public Query get(){
           return databaseReference.orderByKey();
      }

}
