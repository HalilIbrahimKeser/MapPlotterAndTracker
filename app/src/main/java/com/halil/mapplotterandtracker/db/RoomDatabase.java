package com.halil.mapplotterandtracker.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.halil.mapplotterandtracker.Entities.Locations;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Entities.UserInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Trip.class, UserInfo.class, Locations.class}, version = 1, exportSchema = false)
public abstract class RoomDatabase extends androidx.room.RoomDatabase {

    public abstract Dao Dao();
    private static volatile RoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    public static RoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                            RoomDatabase.class, "mapplotterandtracker_db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    private static final Callback sRoomDatabaseCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {

                Dao dao = INSTANCE.Dao();
                dao.deleteAlltrips();

                //Dummy Trip
                Trip.StartGeo startGeo = new Trip.StartGeo(59.948376, 11.007322);
                Trip.StopGeo stopGeo = new Trip.StopGeo(11.007322, 59.943497);
                Trip trip =  new Trip("Home", "Job", 0.5, 10, 50, 20,50, startGeo, stopGeo, false);

                dao.tripInsert(trip);

            });
        }
    };
}
