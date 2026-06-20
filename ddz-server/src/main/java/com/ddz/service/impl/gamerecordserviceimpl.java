package com.ddz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddz.entity.GameRecord;
import com.ddz.mapper.GameRecordMapper;
import com.ddz.service.GameRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GameRecordServiceImpl implements GameRecordService {

    @Autowired
    private GameRecordMapper gameRecordMapper;

    @Override
    @Transactional
    public void saveRecord(GameRecord record) {
        gameRecordMapper.insert(record);
    }

    @Override
    public List<GameRecord> getRecordsByUserId(Long userId) {
        LambdaQueryWrapper<GameRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameRecord::getPlayerId, userId);
        wrapper.orderByDesc(GameRecord::getCreateTime);
        wrapper.last("limit 50");
        return gameRecordMapper.selectList(wrapper);
    }
}
