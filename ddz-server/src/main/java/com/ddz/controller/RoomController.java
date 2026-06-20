package com.ddz.controller;

import com.ddz.entity.Room;
import com.ddz.service.RoomService;
import com.ddz.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/create")
    public Result<Room> createRoom(@RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer baseScore = params.get("baseScore");
        if (baseScore == null) {
            baseScore = 1;
        }
        if (baseScore != 1 && baseScore != 2 && baseScore != 5 && baseScore != 10) {
            return Result.error("底分只能是1、2、5、10");
        }
        Room room = roomService.createRoom(userId, baseScore);
        return Result.ok(room);
    }

    @PostMapping("/join")
    public Result<Room> joinRoom(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String roomNo = params.get("roomNo");
        if (roomNo == null || roomNo.isEmpty()) {
            return Result.error("房间号不能为空");
        }
        try {
            Room room = roomService.joinRoom(userId, roomNo);
            return Result.ok(room);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/leave")
    public Result<String> leaveRoom(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String roomNo = params.get("roomNo");
        if (roomNo == null || roomNo.isEmpty()) {
            return Result.error("房间号不能为空");
        }
        roomService.leaveRoom(userId, roomNo);
        return Result.ok("退出成功");
    }

    @GetMapping("/list")
    public Result<List<Room>> getRoomList() {
        List<Room> list = roomService.getRoomList();
        return Result.ok(list);
    }

    @GetMapping("/{roomNo}")
    public Result<Room> getRoomByNo(@PathVariable String roomNo) {
        Room room = roomService.getRoomByNo(roomNo);
        if (room == null) {
            return Result.error("房间不存在");
        }
        return Result.ok(room);
    }
}
