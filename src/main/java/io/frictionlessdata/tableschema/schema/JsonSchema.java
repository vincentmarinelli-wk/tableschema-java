package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonSchema {
	
	private static final Logger log = LoggerFactory.getLogger(JsonSchema.class);
	
	private final boolean strictValidation;
	private final com.networknt.schema.JsonSchema jsonSchema;
	
	private JsonSchema(JsonNode schemaNode, boolean strictValidation) {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V4);
		this.jsonSchema = factory.getSchema(schemaNode);
		this.strictValidation = strictValidation;
	}

	public static JsonSchema fromJson(String jsonSchema) {
		return fromJson(jsonSchema, true);
	}
	
	public static JsonSchema fromJson(String jsonSchema, boolean strictValidation) {
		return new JsonSchema(JsonUtil.getInstance().readValue(jsonSchema), strictValidation);
	}

	public static JsonSchema fromJson(InputStream jsonSchema, boolean strictValidation) {
		return new JsonSchema(JsonUtil.getInstance().readValue(jsonSchema), strictValidation);
	}
	
	public Set<ValidationMessage> validate(String json) {
		return validate(JsonUtil.getInstance().readValue(json));
	}
	
	public Set<ValidationMessage> validate(JsonNode json) {
		Set<ValidationMessage> errors = jsonSchema.validate(json);
		if (errors.isEmpty()) {
			return Collections.EMPTY_SET;
		} else {
			String msg = String.format("validation failed: %s", errors);
			if (this.strictValidation) {
				log.warn(msg);
				throw new ValidationException(this, errors);
			} else {
				log.warn(msg);
				return errors;
			}
		}
	}

	public String getName() {
		return (null == jsonSchema) ? null : jsonSchema.getSchemaNode().get("title").asText();
	}

}
