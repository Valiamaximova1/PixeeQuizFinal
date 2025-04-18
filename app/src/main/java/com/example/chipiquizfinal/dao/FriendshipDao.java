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
    @Query("SELECT * FROM friendship WHERE user_id = :userId")
    List<Friendship> getOutgoing(int userId);

    @Query("SELECT * FROM friendship WHERE friend_Id = :userId AND status = 'PENDING'")
    List<Friendship> getIncomingRequests(int userId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long addFriend(Friendship friendship);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Friendship f);

    @Update
    void update(Friendship f);
}

