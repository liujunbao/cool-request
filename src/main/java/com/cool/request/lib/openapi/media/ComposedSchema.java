package com.cool.request.lib.openapi.media;

/**
 * ComposedSchema
 */

public class ComposedSchema extends Schema<Object> {


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ComposedSchema {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
