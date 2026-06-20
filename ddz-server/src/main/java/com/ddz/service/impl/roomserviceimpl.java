package com.ddz.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddz.entity.Room;
import com.ddz.mapper.RoomMapper;
import com.ddz.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Override
    @Transactional
    public Room createRoom(Long userId, int baseScore) {
        String roomNo;
        while (true) {
            roomNo = RandomUtil.randomNumbers(6);
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Room::getRoomNo, roomNo);
            Room exist = roomMapper.selectOne(wrapper);
            if (exist == null) {
                break;
            }
        }
        Room room = new Room();
        room.setRoomNo(roomNo);
        room.setBaseScore(baseScore);
        room.setStatus(0);
        room.setPlayer1Id(userId);
        roomMapper.insert(room);
        return room;
    }

    @Override
    @Transactional
    public Room joinRoom(Long userId, String roomNo) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomNo, roomNo);
        Room room = roomMapper.selectOne(wrapper);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        if (room.getStatus() == 1) {
            throw new RuntimeException("房间已开始对局");
        }
        if (userId.equals(room.getPlayer1Id()) || userId.equals(room.getPlayer2Id()) || userId.equals(room.getPlayer3Id())) {
            return room;
        }
        if (room.getPlayer2Id() == null) {
            room.setPlayer2Id(userId);
        } else if (room.getPlayer3Id() == null) {
            room.setPlayer3Id(userId);
        } else {
            throw new RuntimeException("房间已满");
        }
        roomMapper.updateById(room);
        return room;
    }

    @Override
    @Transactional
    public Room leaveRoom(Long userId, String roomNo) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomNo, roomNo);
        Room room = roomMapper.selectOne(wrapper);
        if (room == null) {
            return null;
        }
        if (userId.equals(room.getPlayer1Id())) {
            room.setPlayer1Id(room.getPlayer2Id());
            room.setPlayer2Id(room.getPlayer3Id());
            room.setPlayer3Id(null);
        } else if (userId.equals(room.getPlayer2Id())) {
            room.setPlayer2Id(room.getPlayer3Id());
            room.setPlayer3Id(null);
        } else if (userId.equals(room.getPlayer3Id())) {
            room.setPlayer3Id(null);
        }
        if (room.getPlayer1Id() == null) {
            roomMapper.deleteById(room.getId());
            return null;
        }
        roomMapper.updateById(room);
        return room;
    }

    @Override
    public List<Room> getRoomList() {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Room::getCreateTime);
        return roomMapper.selectList(wrapper);
    }

    @Override
    public Room getRoomByNo(String roomNo) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomNo, roomNo);
        return roomMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void updateRoomStatus(String roomNo, int status) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomNo, roomNo);
        Room room = roomMapper.selectOne(wrapper);
        if (room != null) {
            room.setStatus(status);
            roomMapper.updateById(room);
        }
    }
}
