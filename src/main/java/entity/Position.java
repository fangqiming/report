package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Position {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 股票数量
     */
    private Integer number;
    /**
     * 剩余可买股票数量
     */
    private Integer diff;
    /**
     * 基准指数
     */
    private BigDecimal base;
    /**
     * 自身指数
     */
    private BigDecimal self;

    /**
     * 年
     */
    private Integer year;

}
