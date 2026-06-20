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
@TableName("ddz_room")
public class Room implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomNo;

    private Integer baseScore;

    private Integer status;

    private Long player1Id;

    private Long player2Id;

    private Long player3Id;

    private Long player4Id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
