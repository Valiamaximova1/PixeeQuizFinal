package com.example.chipiquizfinal.dao;
// DAO: FriendshipDao.java

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chipiquizfinal.entity.Friendship;

import java.util.List;

@Dao
public interface FriendshipDao {
    @Query("SELECT * FROM friendship " +
            "WHERE friend_id = :otherId AND user_id = :meId AND status = 'PENDING' " +
            "LIMIT 1")
    Friendship getIncomingRequest(int otherId, int meId);

    @Query("SELECT * FROM friendship WHERE user_id = :meId AND user_id = :otherId LIMIT 1")
    Friendship getOutgoingRequest(int meId, int otherId);

    @Query("UPDATE friendship SET status = :newStatus WHERE id = :friendshipId")
    void updateStatus(int friendshipId, String newStatus);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long addFriend(Friendship friendship);


    @Query("SELECT * FROM friendship WHERE user_id = :requester AND friend_id = :requested LIMIT 1")
    Friendship getFriendship(int requester, int requested);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Friendship f);

    @Update
    void update(Friendship f);
}

