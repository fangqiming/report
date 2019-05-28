package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResult {
    private Integer year;
    private BigDecimal incRate;
    private BigDecimal beat;
    private Integer tradeNumber;
    private BigDecimal winnerRate;
    private BigDecimal positionRate;
    private BigDecimal backRack;
    private BigDecimal holdAvgDay;
}
