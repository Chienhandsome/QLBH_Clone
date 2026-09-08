package kazuko.ex.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kazuko.ex.model.bill.BillTemplateDefinition;

public final class BillTemplateJson {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private BillTemplateJson() {}

    public static String toJson(BillTemplateDefinition definition) {
        return GSON.toJson(definition);
    }

    public static BillTemplateDefinition fromJson(String json) {
        if (json == null || json.isBlank()) {
            return BillTemplateDefinition.createDefault80mm();
        }
        BillTemplateDefinition result = GSON.fromJson(json, BillTemplateDefinition.class);
        if (result == null) {
            return BillTemplateDefinition.createDefault80mm();
        }
        if (result.getBlocks() == null) {
            result.setBlocks(null);
        }
        return result;
    }

    public static BillTemplateDefinition copy(BillTemplateDefinition definition) {
        return fromJson(toJson(definition));
    }
}
