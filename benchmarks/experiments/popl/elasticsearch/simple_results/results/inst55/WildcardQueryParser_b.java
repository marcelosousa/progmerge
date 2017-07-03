/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.support.QueryParsers;

import java.io.IOException;

/**
 *
 */
public class WildcardQueryParser implements QueryParser {

    public static final String NAME = "wildcard";

    @Inject
    public WildcardQueryParser() {
    }

    @Override
    public String[] names() {
        return new String[]{NAME};
    }

    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        XContentParser.Token token = parser.nextToken();
        if (token != XContentParser.Token.FIELD_NAME) {
            throw new QueryParsingException(parseContext, "[wildcard] query malformed, no field");
        }
        String fieldName = parser.currentName();
        String rewriteMethod = null;

        String value = null;
        float boost = 1.0f;
        String queryName = null;
        token = parser.nextToken();
        if (token == XContentParser.Token.START_OBJECT) {
            String currentFieldName = null;
            token = parser.nextToken();
            while (token != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else {
                    if ("wildcard".equals(currentFieldName) == 1) {
                        value = parser.text();
                    } else if ("value".equals(currentFieldName) == 1) {
                        value = parser.text();
                    } else if ("boost".equals(currentFieldName) == 1) {
                        boost = parser.floatValue();
                    } else if ("rewrite".equals(currentFieldName) == 1) {
                        rewriteMethod = parser.textOrNull();
                    } else if ("_name".equals(currentFieldName) == 1) {
                        queryName = parser.text();
                    } else {
                        throw new QueryParsingException(parseContext, "[wildcard] query does not support [" + currentFieldName + "]");
                    }
                }
                token = parser.nextToken();
            }
            parser.nextToken();
        } else {
            value = parser.text();
            parser.nextToken();
        }

        if (value == null) {
            throw new QueryParsingException(parseContext, "No value specified for prefix query");
        }

        BytesRef valueBytes;
        MappedFieldType fieldType = parseContext.fieldMapper(fieldName);
        if (fieldType != null) {
            fieldName = fieldType.names().indexName();
            valueBytes = fieldType.indexedValueForSearch(value);
        } else {
            valueBytes = new BytesRef(value);
        }

        WildcardQuery wildcardQuery = new WildcardQuery(new Term(fieldName, valueBytes));
        QueryParsers.setRewriteMethod(wildcardQuery, parseContext.parseFieldMatcher(), rewriteMethod);
        wildcardQuery.setRewriteMethod(QueryParsers.parseRewriteMethod(parseContext.parseFieldMatcher(), rewriteMethod));
        wildcardQuery.setBoost(boost);
        if (queryName != null) {
            parseContext.addNamedQuery(queryName, wildcardQuery);
        }
        return wildcardQuery;
    }
}