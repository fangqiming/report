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
public class ShortTrade {

    private LocalDate oldDate;

    private LocalDate newDate;

    private BigDecimal oldPrice;

    private BigDecimal newPrice;

    private String name;

    private String industry;

    private Integer year;
}
