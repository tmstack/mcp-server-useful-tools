package tools.mcphub.useful.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Arrays;
import java.util.List;

@Service
public class StockService {

    private static final String BASE_URL = "https://qt.gtimg.cn";
    private final RestClient restClient;

    public StockService() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", "StockApiClient/1.0")
                .build();
    }

    @Tool(description = "Get real-time stock quote for a China stock. Input is stock code (e.g. sh600000, sz000001)")
    public String getStockQuote(String stockCode) {
        String rawData = restClient.get()
                .uri("/q={stock_code}", stockCode)
                .retrieve()
                .body(String.class);

        if (rawData == null || rawData.isEmpty()) {
            return "无法获取股票数据，请检查股票代码是否正确或稍后重试。";
        }

        String cleanedData = rawData.split("=")[1].replace("\"", "");
        List<String> fields = Arrays.asList(cleanedData.split("~"));

        // 防御性校验（至少需要48个字段）
        if (fields.size() < 48) {
            return "接口返回数据格式异常，请稍后重试";
        }

        // 构建买卖五档数据
        String bids = buildMarketDepth(fields, 9, true);
        String asks = buildMarketDepth(fields, 19, false);

        String result = String.format("""
                        ======== 股票实时行情 ========
                        名称: %s (%s)
                        当前价格: %s 元
                        涨跌额: %s 元 (%.2f%%)
                        今开: %s 元 | 昨收: %s 元
                        最高: %s 元 | 最低: %s 元
                        成交量: %s 手 | 成交额: %s 万元
                        
                        --------- 买盘五档 ---------
                        %s
                        --------- 卖盘五档 ---------
                        %s
                        更新时间: %s
                        ===========================
                        """,
                fields.get(1),                                   // 名称
                fields.get(2),                                   // 代码
                safeGet(fields, 3),                             // 当前价
                safeGet(fields, 31),                            // 涨跌额
                parseDoubleSafe(safeGet(fields, 32)),           // 涨跌幅
                safeGet(fields, 5),                            // 今开
                safeGet(fields, 4),                             // 昨收
                safeGet(fields, 33),                            // 最高
                safeGet(fields, 34),                            // 最低
                formatNumber(safeGet(fields, 6)),               // 成交量
                formatNumber(safeGet(fields, 37)),             // 成交额
                bids,
                asks,
                formatTimestamp(safeGet(fields, 30))           // 更新时间
        );

        System.out.println(result);
        return result;
    }

    private String buildMarketDepth(List<String> fields, int startIndex, boolean isBid) {
        StringBuilder depth = new StringBuilder();
        String label = isBid ? "买" : "卖";

        for (int i = 0; i < 5; i++) {
            // For bids, we want to display from level 5 to level 1
            int level = isBid ? (5 - i) : (i + 1);
            int priceIndex = startIndex + 2 * (level - 1);
            int volumeIndex = priceIndex + 1;

            String price = "N/A";
            String volume = "N/A";

            if (priceIndex < fields.size()) {
                price = fields.get(priceIndex);
            }
            if (volumeIndex < fields.size()) {
                volume = formatNumber(fields.get(volumeIndex));
            }

            depth.append(String.format("%s%d: %s 元 (%s 手)%n",
                    label, level, price, volume));
        }
        return depth.toString().trim();
    }

    // 安全获取字段（带越界保护）
    private String safeGet(List<String> fields, int index) {
        return index < fields.size() ? fields.get(index) : "N/A";
    }

    // 安全转换double
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String formatNumber(String numStr) {
        try {
            return String.format("%,d", Long.parseLong(numStr));
        } catch (NumberFormatException e) {
            return numStr;
        }
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp.length() < 14) return "N/A";
        return String.format("%s-%s-%s %s:%s:%s",
                timestamp.substring(0, 4),
                timestamp.substring(4, 6),
                timestamp.substring(6, 8),
                timestamp.substring(8, 10),
                timestamp.substring(10, 12),
                timestamp.substring(12, 14));
    }

    public static void main(String[] args) {
        StockService service = new StockService();
        service.getStockQuote("sz000858");
    }
}
