package tools.mcphub.useful.model.bo;

import java.util.List;

public class WeatherResponse {
    private List<CurrentCondition> current_condition;

    public List<CurrentCondition> getCurrent_condition() {
        return current_condition;
    }

    public void setCurrent_condition(List<CurrentCondition> current_condition) {
        this.current_condition = current_condition;
    }
}
