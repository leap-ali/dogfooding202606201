package com.ddz.service;

import com.ddz.entity.GameRecord;
import java.util.List;

public interface GameRecordService {

    void saveRecord(GameRecord record);

    List<GameRecord> getRecordsByUserId(Long userId);
}
