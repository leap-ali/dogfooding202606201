package com.ddz.controller;

import com.ddz.entity.GameRecord;
import com.ddz.service.GameRecordService;
import com.ddz.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameRecordService gameRecordService;

    @GetMapping("/records")
    public Result<List<GameRecord>> getGameRecords(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<GameRecord> list = gameRecordService.getRecordsByUserId(userId);
        return Result.ok(list);
    }

    @PostMapping("/record")
    public Result<String> saveRecord(@RequestBody GameRecord record) {
        gameRecordService.saveRecord(record);
        return Result.ok("保存成功");
    }
}
