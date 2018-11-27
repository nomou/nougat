package freework.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.exceptions.InvalidInstanceException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Sets;
import freework.util.Throwables;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Json schema 验证工具
 * <p>
 * Json Schema: http://json-schema.org/
 * Json --&gt; Json Schema: http://jsonschema.net/
 *
 * @author vacoor
 */
public abstract class JsonValidators {

    /**
     * 根据给定的 json schema resource 构建 JsonSchema 对象
     *
     * @param resource json schema 路径
     */
    public static JsonSchema getSchemaFromResource(String resource) {
        try {
            return getSchema(JsonLoader.fromResource(resource));
        } catch (IOException e) {
            return Throwables.unchecked(e);
        }
    }

    /**
     * 根据给定的 json 字符串构建 json schema
     *
     * @param json json schema 字符串
     */
    public static JsonSchema getSchemaFromString(String json) {
        try {
            return getSchema(JsonLoader.fromString(json));
        } catch (IOException e) {
            return Throwables.unchecked(e);
        }
    }

    public static JsonSchema getSchema(JsonNode schema) {
        try {
            return JsonSchemaFactory.byDefault().getJsonSchema(schema);
        } catch (ProcessingException e) {
            return Throwables.unchecked(e);
        }
    }

    public static void validate(JsonSchema schema, String json) throws InvalidInstanceException {
        validate(schema, json, false);
    }

    public static void validate(JsonSchema schema, String json, boolean deepCheck) throws InvalidInstanceException {
        try {
            validate(schema, JsonLoader.fromString(json), deepCheck);
        } catch (Exception ex) {
            throw toInvalidInstanceException(ex);
        }
    }

    public static void validate(JsonSchema schema, JsonNode instance) throws InvalidInstanceException {
        validate(schema, instance, false);
    }

    public static void validate(JsonSchema schema, JsonNode instance, boolean deepCheck) throws InvalidInstanceException {
        try {
            ProcessingReport report = schema.validate(instance, deepCheck);
            if (!report.isSuccess()) {
                Iterator<ProcessingMessage> it = report.iterator();
                ProcessingMessage message = it.hasNext() ? it.next() : null;
                if (null == message) {
                    message = new ProcessingMessage();
                    message.setLogLevel(LogLevel.ERROR);
                    message.setMessage("invalid json");
                }
                throw new InvalidInstanceException(message);
            }
        } catch (ProcessingException ex) {
            throw toInvalidInstanceException(ex);
        }
    }

    private static InvalidInstanceException toInvalidInstanceException(Exception ex) {
        if (ex instanceof InvalidInstanceException) {
            return (InvalidInstanceException) ex;
        }
        if (ex instanceof ProcessingException) {
            ProcessingException procEx = (ProcessingException) ex;
            ProcessingMessage message = procEx.getProcessingMessage();
            String msg = procEx.getMessage();
            if (null == message) {
                message = new ProcessingMessage();
                message.setLogLevel(LogLevel.ERROR);
                message.setMessage(null != msg ? procEx.getMessage() : "invalid json");
            }
            InvalidInstanceException iex = new InvalidInstanceException(message);
            iex.initCause(procEx);
            return iex;
        }
        ProcessingMessage processingMessage = new ProcessingMessage();
        processingMessage.setLogLevel(LogLevel.ERROR);
        processingMessage.setMessage(ex.getMessage());

        if (ex instanceof JsonParseException) {
            JsonParseException jpe = (JsonParseException) ex;
            processingMessage.setMessage("json parse error, " + jpe.getMessage());
        }

        InvalidInstanceException iex = new InvalidInstanceException(processingMessage);
        iex.initCause(ex);
        return iex;
    }

    /*
    // 验证, 如果有异常转换为 report
    public String validateUnchecked(JsonSchema schema, JsonNode instance, boolean deepCheck) {
        schema.validateUnchecked(instance, deepCheck);
        return null;
    }
    */

    public static String joinMessages(Iterable<ProcessingMessage> messages) {
        return joinMessages(messages, ",");
    }

    public static String joinMessages(Iterable<ProcessingMessage> messages, String sep) {
        if (null == messages) {
            return null;
        }
        StringBuilder buff = new StringBuilder();
        for (Iterator<ProcessingMessage> it = messages.iterator(); it.hasNext(); ) {
            ProcessingMessage message = it.next();
            buff.append(message.getMessage());
            if (it.hasNext()) {
                buff.append(sep);
            }
        }
        return buff.toString();
    }

    public static Set<String> getMessages(Iterable<ProcessingMessage> messages) {
        Set<String> msgs = Sets.newHashSet();
        for (ProcessingMessage message : messages) {
            msgs.add(message.getMessage());
        }
        return msgs;
    }

    private JsonValidators() {
    }
}
