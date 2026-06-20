package com.ddz.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ddz_game_record")
public class GameRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomNo;

    private Long playerId;

    private Integer identity;

    private Integer isWin;

    private Integer scoreChange;

    private Integer baseScore;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
