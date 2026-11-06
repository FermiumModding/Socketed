package socketed.common.socket.gem.effect.activatable.condition;

import com.google.gson.*;
import socketed.api.socket.gem.effect.activatable.condition.GenericCondition;
import socketed.api.socket.gem.filter.GenericFilter;
import socketed.common.config.JsonConfig;
import socketed.common.socket.gem.filter.FilterDeserializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConditionDeserializer implements JsonDeserializer<GenericCondition> {
	
	private final Gson gson;
	private final Map<String, Class<? extends GenericCondition>> typeReg;
	
	public ConditionDeserializer() {
		FilterDeserializer filterDeserializer = new FilterDeserializer();
		for(Map.Entry<String, Class<? extends GenericFilter>> entry : JsonConfig.filterDeserializerMap.entrySet()) {
			filterDeserializer.registerType(entry.getKey(), entry.getValue());
		}

		this.gson = new GsonBuilder()
				.registerTypeAdapter(GenericCondition.class, this)
				.registerTypeAdapter(GenericFilter.class, filterDeserializer)
				.create();
		this.typeReg = new HashMap<>();
	}
	
	public void registerType(String typeName, Class<? extends GenericCondition> typeClass) {
		typeReg.put(typeName, typeClass);
	}
	
	@Override
	public GenericCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		Class<? extends GenericCondition> typeClass = typeReg.get(obj.get(GenericCondition.TYPE_FIELD).getAsString());
		return this.gson.fromJson(obj, typeClass);
	}
}