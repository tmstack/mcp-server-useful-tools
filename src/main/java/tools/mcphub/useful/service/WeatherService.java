package tools.mcphub.useful.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.mcphub.useful.model.bo.CurrentCondition;
import tools.mcphub.useful.model.bo.WeatherResponse;

@Service
public class WeatherService {

    private static final String BASE_URL = "https://wttr.in";

    private final RestClient restClient;

    public WeatherService() {

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/geo+json")
                .defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
                .build();
    }

    /**
     * Get current weather information for a specific city in China
     *
     * @param cityName city name (e.g. 杭州, 上海)
     * @return weather information
     * @throws RestClientException if the request fails
     */
    @Tool(description = "Get current weather information for a China city. Input is city name (e.g. 杭州, 上海)")
    public String getWeather(String cityName) {

        // 调用接口获取数据
        WeatherResponse response = restClient.get()
                .uri("/{city_name}?format=j1", cityName)
                .retrieve()
                .body(WeatherResponse.class);
        // 解析当前天气信息
        if (response != null && response.getCurrent_condition() != null && !response.getCurrent_condition().isEmpty()) {
            CurrentCondition currentCondition = response.getCurrent_condition().get(0);
            String result = String.format("""
                            城市: %s
                            天气情况: %s
                            气压: %s（mb）
                            温度: %s°C (Feels like: %s°C)
                            湿度: %s%%
                            降水量:%s (mm)
                            风速: %s km/h (%s)
                            能见度: %s 公里
                            紫外线指数: %s
                            观测时间: %s
                            """,
                    cityName,
                    currentCondition.getWeatherDesc().get(0).getValue(),
                    currentCondition.getPressure(),
                    currentCondition.getTemp_C(),
                    currentCondition.getFeelsLikeC(),
                    currentCondition.getHumidity(),
                    currentCondition.getPrecipMM(),
                    currentCondition.getWindspeedKmph(),
                    currentCondition.getWinddir16Point(),
                    currentCondition.getVisibility(),
                    currentCondition.getUvIndex(),
                    currentCondition.getLocalObsDateTime()
            );
            System.out.println(result);
            return result;
        } else {
            return "无法获取天气信息，请检查城市名称是否正确或稍后重试。";
        }
    }

    public static void main(String[] args) {
        WeatherService client = new WeatherService();
        client.getWeather("杭州");
    }

}