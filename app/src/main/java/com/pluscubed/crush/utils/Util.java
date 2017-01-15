package com.pluscubed.crush.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import rx.Emitter;
import rx.Observable;

public abstract class Util {
    public static Observable<DataSnapshot> queryFirebase(Query query) {
        return Observable.fromEmitter(dataSnapshotEmitter -> query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshotEmitter.onNext(dataSnapshot);
                dataSnapshotEmitter.onCompleted();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }), Emitter.BackpressureMode.BUFFER);

    }
}
