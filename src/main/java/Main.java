import entity.LongTrade;
import entity.Position;
import entity.ReportResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static List<Position> positions = new ArrayList<>();
    private static List<LongTrade> longTrades = new ArrayList<>();

    public static void main(String[] args) {
        init();
        List<ReportResult> resultList = new ArrayList<>();
        //1.计算每一年的，平均胜率，交易次数，平均持有天数，
        //正常情况下是应该都有
        Map<Integer, List<Position>> position = positions.stream().collect(Collectors.groupingBy(Position::getYear));
        Map<Integer, List<LongTrade>> longTrade = longTrades.stream().collect(Collectors.groupingBy(LongTrade::getYear));
        for (Map.Entry<Integer, List<Position>> entry : position.entrySet()) {
            Integer year = entry.getKey();
            ReportResult result = getResult(entry.getValue(), longTrade.get(year));
            result.setYear(year);
            resultList.add(result);
        }
        ReportResult total = getResult(positions, longTrades);
        total.setYear(0);
        resultList.add(total);
        System.out.println("年份\t盈亏\t跑赢上证\t平均仓位\t最大回撤\t平均胜率\t交易次数\t平均持有天数\t");
        resultList.sort(Comparator.comparing(ReportResult::getYear));
        for (ReportResult t : resultList) {
            System.out.println(t.getYear() + "\t" + t.getIncRate() + "\t" + t.getBeat() + "\t" + t.getPositionRate() + "\t" + t.getBackRack() + "\t" + t.getWinnerRate() + "\t" + t.getTradeNumber() + "\t" + t.getHoldAvgDay() + "\t");
        }
    }

    private static ReportResult getResult(List<Position> value, List<LongTrade> longTrades) {
        value.sort(Comparator.comparing(Position::getDate));
        Position init = value.get(0);
        Position end = value.get(value.size() - 1);
        BigDecimal baseRate = (end.getBase().subtract(init.getBase())).divide(init.getBase(), 4, BigDecimal.ROUND_UP).multiply(new BigDecimal(100));
        //系统收益率
        BigDecimal sysRate = (end.getSelf().subtract(init.getSelf())).divide(init.getSelf(), 4, BigDecimal.ROUND_UP).multiply(new BigDecimal(100));
        //打败上证
        BigDecimal beat = (sysRate.subtract(baseRate)).divide(new BigDecimal(100).add(baseRate), 4, BigDecimal.ROUND_UP).multiply(new BigDecimal(100));
        List<BigDecimal> self = value.stream().map(a -> a.getSelf()).collect(Collectors.toList());
        //回撤
        BigDecimal backRack = calcMaxDd(self);
        //平均仓位
        Integer all = value.stream().map(a -> a.getNumber()).reduce((a, b) -> a + b).get();
        BigDecimal avgPosition = new BigDecimal(all / 1.0 / (value.size() * (init.getNumber() + init.getDiff())) * 100);
        //---------------------------------------------------------
        //平均胜率
        List<LongTrade> win = longTrades.stream().filter(a -> a.getNewPrice().compareTo(a.getOldPrice()) > 0).collect(Collectors.toList());
        BigDecimal winnerRate = new BigDecimal(win.size() / 1.0 / longTrades.size() * 100);
        //交易次数
        Integer tradeNumber = longTrades.size();
        //平均持有天数
        Integer integer = longTrades.stream().map(a -> Period.between(a.getOldDate(), a.getNewDate()).getDays()).reduce((a, b) -> a + b).get();
        BigDecimal avgHoldDay = new BigDecimal(integer / 1.0 / longTrades.size());
        return ReportResult.builder().backRack(backRack.setScale(2, BigDecimal.ROUND_UP)).beat(beat.setScale(2, BigDecimal.ROUND_UP)).holdAvgDay(avgHoldDay.setScale(2, BigDecimal.ROUND_UP)).incRate(sysRate.setScale(2, BigDecimal.ROUND_UP))
                .positionRate(avgPosition.setScale(2, BigDecimal.ROUND_UP))
                .tradeNumber(tradeNumber).winnerRate(winnerRate.setScale(2, BigDecimal.ROUND_UP)).build();
    }

    public static void init() {
        String originalStr = readFile().toString();
        String[] split = originalStr.split("\\n\\n");
        for (String o : split) {
            if (o.contains("Daily Snapshot")) {
                //此处表明是仓位数据
                String[] positionItem = o.split("\\n");
                for (String line : positionItem) {
                    if (line.startsWith("20")) {
                        String[] item = line.split("\\t");
                        LocalDate date = LocalDate.parse(item[0], DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                        positions.add(Position.builder()
                                .date(date)
                                .number(Integer.valueOf(item[1]))
                                .diff(Integer.valueOf(item[2]))
                                .base(new BigDecimal(item[5]))
                                .year(date.getYear())
                                .self(new BigDecimal(item[6])).build());
                    }
                }
            } else if (o.contains("Details for all trades , LONG1")) {
                String[] tradeItem = o.split("\\n");
                for (String line : tradeItem) {
                    if (!line.startsWith("-----")) {
                        String[] item = line.split("\\t");
                        LocalDate date = LocalDate.parse(item[2], DateTimeFormatter.ofPattern("yyyyMMdd"));
                        longTrades.add(LongTrade.builder().oldPrice(new BigDecimal(item[1]))
                                .oldDate(date)
                                .year(date.getYear())
                                .newPrice(new BigDecimal(item[3]))
                                .newDate(LocalDate.parse(item[4], DateTimeFormatter.ofPattern("yyyyMMdd")))
                                .name(item[9])
                                .industry(item[10]).build());
                    }
                }
            }
        }
    }

    public static StringBuffer readFile() {
        StringBuffer result = new StringBuffer();
        String pathname = "C:\\Users\\fangiming\\Desktop\\DETAILS.txt";
        try {
            FileReader reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line + "\n");
            }
            br.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static BigDecimal calcMaxDd(List<BigDecimal> price) {
        BigDecimal max_unit_value = price.get(0);
        BigDecimal max_dd = BigDecimal.ZERO;
        BigDecimal dd;
        for (int i = 1; i < price.size(); i++) {
            max_unit_value = price.get(i).compareTo(max_unit_value) > 0 ? price.get(i) : max_unit_value;
            dd = price.get(i).divide(max_unit_value, 4, BigDecimal.ROUND_UP).subtract(new BigDecimal(1));
            max_dd = dd.compareTo(max_dd) < 0 ? dd : max_dd;
        }
        return max_dd.multiply(new BigDecimal(100));
    }

}
