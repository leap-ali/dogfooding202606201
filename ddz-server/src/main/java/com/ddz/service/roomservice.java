package com.ddz.service;

import com.ddz.entity.Room;
import java.util.List;

public interface RoomService {

    Room createRoom(Long userId, int baseScore);

    Room joinRoom(Long userId, String roomNo);

    Room leaveRoom(Long userId, String roomNo);

    List<Room> getRoomList();

    Room getRoomByNo(String roomNo);

    void updateRoomStatus(String roomNo, int status);
}
