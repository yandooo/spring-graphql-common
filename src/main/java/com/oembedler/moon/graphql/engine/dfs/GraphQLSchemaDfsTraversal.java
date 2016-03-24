/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 oEmbedler Inc. and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 *  persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.oembedler.moon.graphql.engine.dfs;

import com.google.common.collect.Lists;
import com.oembedler.moon.graphql.GraphQLConstants;
import com.oembedler.moon.graphql.GraphQLSchemaBeanFactory;
import com.oembedler.moon.graphql.engine.*;
import com.oembedler.moon.graphql.engine.stereotype.GraphQLInterface;
import com.oembedler.moon.graphql.engine.stereotype.GraphQLSchemaQuery;
import com.oembedler.moon.graphql.engine.type.GraphQLEnumTypeExt;
import graphql.Scalars;
import graphql.schema.*;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLSchemaDfsTraversal {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLSchemaDfsTraversal.class);
    private static final ExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    // ---

    private String schemaName;
    private final Class<?> schemaClass;
    private final GraphQLSchemaConfig graphQLSchemaConfig;
    private final GraphQLSchemaBeanFactory graphQLSchemaBeanFactory;
    private final GraphQLMappingContext graphQLMappingContext;
    private final ConcurrentHashMap<Class<?>, String> objectTypeNameReferenceMap;
    private final ConcurrentHashMap<String, Class<?>> objectNameTypeReferenceMap;
    private final ConcurrentHashMap<Class<?>, GraphQLType> objectTypeResolverMap;
    private final Map<String, Map<Class<?>, GraphQLOutputType>> mutationReturnTypeResolverMap;
    private final Map<String, Map<Class<?>, GraphQLInputObjectField>> mutationInputTypeResolverMap;
    private final Map<GraphQLFieldDefinition, GraphQLFieldDefinitionWrapper> fieldDefinitionResolverMap;
    private final Set<GraphQLUnionType> graphQLUnionTypeMap;

    // ---

    public GraphQLSchemaDfsTraversal(Class<?> schemaClass, GraphQLSchemaConfig graphQLSchemaConfig, GraphQLSchemaBeanFactory graphQLSchemaBeanFactory) {
        this.schemaClass = schemaClass;
        this.graphQLSchemaConfig = graphQLSchemaConfig;
        this.graphQLSchemaBeanFactory = graphQLSchemaBeanFactory;
        this.graphQLMappingContext = new GraphQLMappingContext(this.graphQLSchemaConfig);
        this.objectTypeResolverMap = new ConcurrentHashMap<>();
        this.objectTypeNameReferenceMap = new ConcurrentHashMap<>();
        this.objectNameTypeReferenceMap = new ConcurrentHashMap<>();
        this.mutationReturnTypeResolverMap = new LinkedHashMap<>();
        this.mutationInputTypeResolverMap = new LinkedHashMap<>();
        this.fieldDefinitionResolverMap = new LinkedHashMap<>();
        this.graphQLUnionTypeMap = new HashSet<>();
    }

    public GraphQLSchema traverse() {
        schemaName = StereotypeUtils.getGraphQLSchemaName(schemaClass, schemaClass.getName());

        DfsContext dfsContext = new DfsContext();
        GraphQLObjectType graphQLRootQueryObjectType = (GraphQLObjectType) findSchemaQueryRoot(dfsContext, schemaClass);
        GraphQLObjectType graphQLMutationObjectType = findSchemaMutations(dfsContext, schemaClass);
        GraphQLSchema graphQLSchema =
                newSchema()
                        .query(graphQLRootQueryObjectType)
                        .mutation(graphQLMutationObjectType)
                        .build();
        SchemaHelper.replaceTypeReferencesForUnionType(graphQLSchema, graphQLUnionTypeMap);
        return graphQLSchema;
    }

    // ---

    public GraphQLType findSchemaQueryRoot(DfsContext dfsContext, Class<?> classSchema) {
        Set<Field> fields = ReflectionUtils.getAllFields(classSchema,
                ReflectionUtils.withAnnotation(GraphQLSchemaQuery.class));

        if (fields.size() == 0)
            throw new GraphQLSchemaTraversalRuntimeException("No GraphQL schema root query found");

        return createGraphQLObjectTypeRecursively(dfsContext, fields.iterator().next().getType());
    }

    public GraphQLType createGraphQLObjectTypeRecursively(DfsContext dfsContext, final Class<?> implClass) {

        ResolvableTypeAccessor resolvableTypeAccessor = ResolvableTypeAccessor.forClass(implClass);
        String objectName = resolvableTypeAccessor.getName();
        String objectDescription = resolvableTypeAccessor.getDescription();

        GraphQLType graphQLObjectType = objectTypeResolverMap.get(implClass);
        if (!objectTypeNameReferenceMap.containsKey(implClass)) {

            objectTypeNameReferenceMap.put(implClass, objectName);
            objectNameTypeReferenceMap.put(objectName, implClass);

            final List<GraphQLFieldDefinition> graphQLFieldDefinitions = new ArrayList<>();

            if (implClass.isEnum()) {
                graphQLObjectType = buildGraphQLEnumType(dfsContext, resolvableTypeAccessor);
            } else {

                ReflectionUtils.getAllFields(implClass).forEach(field -> {
                    GraphQLFieldDefinition definition = getFieldDefinition(dfsContext, implClass, field);
                    if (definition != null)
                        graphQLFieldDefinitions.add(definition);
                });

                ReflectionUtils.getAllMethods(implClass).forEach(method -> {
                    GraphQLFieldDefinition definition = getMethodDefinition(dfsContext, implClass, method);
                    if (definition != null)
                        graphQLFieldDefinitions.add(definition);
                });

                List<GraphQLType> graphQLInterfaceTypes = Lists.newArrayList();
                if (implClass.isInterface()) {
                    if (resolvableTypeAccessor.isGraphQLUnion()) {
                        List<GraphQLType> possibleTypes = new ArrayList<>();
                        for (Class<?> possibleType : resolvableTypeAccessor.getGraphQLUnionPossibleTypes()) {
                            possibleTypes.add(createGraphQLObjectTypeRecursively(dfsContext, possibleType));
                        }

                        graphQLObjectType = GraphQLUnionType.newUnionType()
                                .name(resolvableTypeAccessor.getName())
                                .possibleTypes(possibleTypes.toArray(new GraphQLType[possibleTypes.size()]))
                                .typeResolver(new CompleteObjectTreeTypeResolver(objectTypeResolverMap))
                                .description(resolvableTypeAccessor.getDescription())
                                .build();
                        graphQLUnionTypeMap.add((GraphQLUnionType) graphQLObjectType);
                    } else {
                        graphQLObjectType = newInterface()
                                .name(objectName)
                                .description(objectDescription)
                                .fields(graphQLFieldDefinitions)
                                .typeResolver(new CompleteObjectTreeTypeResolver(objectTypeResolverMap))
                                .build();
                    }
                } else {
                    ClassUtils.getAllInterfacesForClassAsSet(implClass).forEach(aClass -> {
                        if (isAcceptableInterface(dfsContext, aClass)) {
                            GraphQLType graphQLType = createGraphQLObjectTypeRecursively(dfsContext, aClass);
                            graphQLInterfaceTypes.add(graphQLType);
                        }
                    });

                    graphQLObjectType = newObject()
                            .name(objectName)
                            .description(objectDescription)
                            .fields(graphQLFieldDefinitions)
                            .withInterfaces(resolveInterfaceReferences(dfsContext, graphQLInterfaceTypes))
                            .build();
                }
            }

            objectTypeResolverMap.put(implClass, graphQLObjectType);
        } else {
            // reference
            //if (!implClass.isInterface())
            graphQLObjectType = new GraphQLTypeReference(objectName);
        }
        return graphQLObjectType;
    }

    public GraphQLInterfaceType[] resolveInterfaceReferences(DfsContext dfsContext, List<GraphQLType> graphQLInterfaceTypes) {
        GraphQLInterfaceType[] interfaceTypes = new GraphQLInterfaceType[graphQLInterfaceTypes.size()];
        for (int i = 0; i < graphQLInterfaceTypes.size(); i++) {
            GraphQLType type = graphQLInterfaceTypes.get(i);
            if (type instanceof GraphQLTypeReference) {
                Class<?> cls = objectNameTypeReferenceMap.get(type.getName());
                type = objectTypeResolverMap.get(cls);
            }
            interfaceTypes[i] = (GraphQLInterfaceType) type;
        }
        return interfaceTypes;
    }

    public GraphQLEnumType buildGraphQLEnumType(DfsContext dfsContext, ResolvableTypeAccessor resolvableTypeAccessor) {
        Class<?> implClass = resolvableTypeAccessor.getImplClass();
        Enum[] enumList = (Enum[]) implClass.getEnumConstants();
        String valueProviderMethod = resolvableTypeAccessor.getGraphQLEnumValueProviderMethodName();
        String valueSpel = resolvableTypeAccessor.getGraphQLEnumDefaultValueSpel();
        GraphQLEnumTypeExt.Builder enumTypeBuilder = new GraphQLEnumTypeExt.Builder()
                .name(resolvableTypeAccessor.getName())
                .description(resolvableTypeAccessor.getDescription());
        if (enumList != null) {
            for (Enum en : enumList) {
                ResolvableTypeAccessor rta = ResolvableTypeAccessor.forEnumField(en);
                String description = rta.getDescription();
                String enumName = rta.getName();
                Object value = enumName;

                if (StringUtils.hasText(valueProviderMethod))
                    value = invokeMethodByName(dfsContext, implClass, valueProviderMethod, en);

                value = evaluateSpElExpression(dfsContext, implClass, en, valueSpel, value);
                enumTypeBuilder.value(enumName, value, description);
            }
        }
        GraphQLEnumType enumType = enumTypeBuilder.build();
        return enumType;
    }

    public boolean isAcceptableInterface(DfsContext dfsContext, Class<?> aClass) {
        return aClass.isAnnotationPresent(GraphQLInterface.class);
    }

    public GraphQLFieldDefinition getMethodDefinition(DfsContext dfsContext, Class<?> objectClass, Method method) {

        GraphQLFieldDefinition graphQLFieldDefinition = null;
        ResolvableTypeAccessor resolvableTypeAccessor =
                ResolvableTypeAccessor.forMethodReturnType(method, objectClass);
        if (resolvableTypeAccessor.isGraphQLIdOrGraphQLField()) {

            GraphQLOutputType graphQLOutputType = (GraphQLOutputType) createGraphQLFieldType(dfsContext, resolvableTypeAccessor, true);
            List<GraphQLArgument> graphQLArguments = buildGraphQLArgumentsFromMethodParams(dfsContext, method, objectClass);

            GraphQLFieldDefinition.Builder builder =
                    GraphQLFieldDefinition
                            .newFieldDefinition()
                            .name(resolvableTypeAccessor.getName())
                            .deprecate(resolvableTypeAccessor.getGraphQLDeprecationReason())
                            .argument(graphQLArguments)
                            .type(graphQLOutputType)
                            .description(resolvableTypeAccessor.getDescription());

            if (!objectClass.isInterface()) {
                String beanName = objectClass.getName() + resolvableTypeAccessor.getName();
                Object object = getGraphQLSchemaBeanFactory().getBeanByType(objectClass);
                builder.dataFetcher(new ReflectionGraphQLDataFetcher(getGraphQLSchemaConfig(), object, method));
            }
            graphQLFieldDefinition = builder.build();

            addToFieldDefinitionResolverMap(dfsContext, graphQLFieldDefinition, resolvableTypeAccessor.getGraphQLComplexitySpelExpression());
        }

        return graphQLFieldDefinition;
    }

    public GraphQLFieldDefinition getFieldDefinition(DfsContext dfsContext, Class<?> implClass, Field field) {

        GraphQLFieldDefinition graphQLFieldDefinition = null;
        ResolvableTypeAccessor resolvableTypeAccessor =
                ResolvableTypeAccessor.forField(field, implClass);

        if (resolvableTypeAccessor.isNotIgnorable()) {
            GraphQLOutputType graphQLOutputType = (GraphQLOutputType) createGraphQLFieldType(dfsContext, resolvableTypeAccessor, true);
            graphQLFieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
                    .name(resolvableTypeAccessor.getName())
                    .type(graphQLOutputType)
                    .deprecate(resolvableTypeAccessor.getGraphQLDeprecationReason())
                    .description(resolvableTypeAccessor.getDescription())
                    .build();

            addToFieldDefinitionResolverMap(dfsContext, graphQLFieldDefinition, resolvableTypeAccessor.getGraphQLComplexitySpelExpression());
        }

        return graphQLFieldDefinition;
    }

    public void addToFieldDefinitionResolverMap(DfsContext dfsContext, GraphQLFieldDefinition graphQLFieldDefinition, String complexitySpelExpression) {
        fieldDefinitionResolverMap.put(graphQLFieldDefinition,
                new GraphQLFieldDefinitionWrapper(graphQLFieldDefinition, complexitySpelExpression));
    }

    public GraphQLType createGraphQLFieldType(DfsContext dfsContext, ResolvableTypeAccessor resolvableTypeAccessor, boolean isRecursive) {
        boolean isContainer = resolvableTypeAccessor.isCollectionLike();
        Class<?> cls = resolvableTypeAccessor.getActualType();

        GraphQLType graphQLType =
                resolvableTypeAccessor.isGraphQLId() ?
                        Scalars.GraphQLID : getGraphQLMappingContext().getScalarGraphQLType(cls);

        // complex object
        if (graphQLType == null && isRecursive) {
            graphQLType = createGraphQLObjectTypeRecursively(dfsContext, cls);
        }

        if (graphQLType != null) {
            if (isContainer) {
                graphQLType = new GraphQLList(graphQLType);
            } else {
                graphQLType = resolvableTypeAccessor.isNotNull() ?
                        new GraphQLNonNull(graphQLType) : graphQLType;
            }
        }

        return graphQLType;
    }

    public List<GraphQLArgument> buildGraphQLArgumentsFromMethodParams(DfsContext dfsContext, Method method, Class<?> implClass) {
        List<GraphQLArgument> graphQLArguments = new ArrayList<>();

        final GraphQLMethodParameters graphQLMethodParameters = new GraphQLMethodParameters(method, implClass);

        if (graphQLMethodParameters.hasParameters()) {
            graphQLMethodParameters.getParameters().forEach(mpi -> {

                Object defaultValue = invokeMethodByName(dfsContext, implClass, mpi.getGraphQLInDefaultValueProviderMethodName());
                defaultValue = evaluateSpElExpression(dfsContext, implClass, null, mpi.getGraphQLInDefaultValueSpel(), defaultValue);

                if (mpi.isValidGraphQLInParameter()) {
                    GraphQLArgument graphQLArgument = newArgument()
                            .name(mpi.getName())
                            .description(mpi.getDescription())
                            .type(buildGraphQLInputTypeFromMethodParam(dfsContext, mpi.getResolvableTypeAccessor()))
                            .defaultValue(defaultValue)
                            .build();
                    graphQLArguments.add(graphQLArgument);
                }
            });
        }

        return graphQLArguments;
    }

    public Object evaluateSpElExpression(DfsContext dfsContext, Class<?> implClass, Object instance, String spElExpression, Object defaultIfNone) {
        Object defaultValue = defaultIfNone;
        if (StringUtils.hasText(spElExpression)) {
            Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(spElExpression);

            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable(GraphQLConstants.DFS_IMPLEMENTATION_CLASS, implClass);
            context.setVariable(GraphQLConstants.DFS_OBJECT_INSTANCE, instance);
            defaultValue = expression.getValue(context);
        }
        return defaultValue;
    }

    public Object invokeMethodByName(DfsContext dfsContext, Class<?> implClass, String methodName, Object... args) {
        Object defaultValue = null;
        if (StringUtils.hasText(methodName)) {
            Object object = null;
            if (getGraphQLSchemaBeanFactory().containsBean(implClass))
                object = getGraphQLSchemaBeanFactory().getBeanByType(implClass);

            Method defaultValueProviderMethod = args == null ?
                    org.springframework.util.ReflectionUtils.findMethod(implClass, methodName) :
                    org.springframework.util.ReflectionUtils.findMethod(implClass, methodName, getArgumentClasses(args));

            if (defaultValueProviderMethod != null) {
                defaultValue = org.springframework.util.ReflectionUtils.invokeMethod(defaultValueProviderMethod, object, args);
            }
        }
        return defaultValue;
    }

    private Class<?>[] getArgumentClasses(Object[] args) {
        Class<?>[] argClasses = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null)
                argClasses[i] = args[i].getClass();
        }
        return argClasses;
    }

    public GraphQLInputType buildGraphQLInputTypeFromMethodParam(DfsContext dfsContext, ResolvableTypeAccessor resolvableTypeAccessor) {

        Class<?> cls = resolvableTypeAccessor.resolve();

        GraphQLInputType graphQLInputObjectType = (GraphQLInputType) objectTypeResolverMap.get(cls);
        if (graphQLInputObjectType == null)
            graphQLInputObjectType = (GraphQLInputType) createGraphQLFieldType(dfsContext, resolvableTypeAccessor, false);

        if (graphQLInputObjectType == null) {

            // -- class as input parameter
            final List<GraphQLInputObjectField> graphQLInputFieldDefinitions = new ArrayList<GraphQLInputObjectField>();

            ReflectionUtils.getAllFields(cls).forEach(field -> {
                GraphQLInputObjectField definition = buildGraphQLInputObjectField(dfsContext, field, cls);
                if (definition != null)
                    graphQLInputFieldDefinitions.add(definition);
            });

            graphQLInputObjectType = newInputObject()
                    .name(cls.getSimpleName())
                    .description(resolvableTypeAccessor.getDescription())
                    .fields(graphQLInputFieldDefinitions)
                    .build();
        }

        return graphQLInputObjectType;
    }

    public GraphQLInputObjectField buildGraphQLInputObjectField(DfsContext dfsContext, Field field, Class<?> implClass) {

        ResolvableTypeAccessor resolvableTypeAccessor =
                ResolvableTypeAccessor.forField(field, implClass);

        Object defaultValue = invokeMethodByName(dfsContext, implClass, resolvableTypeAccessor.getGraphQLInDefaultValueProviderMethodName());
        defaultValue = evaluateSpElExpression(dfsContext, implClass, null, resolvableTypeAccessor.getGraphQLInDefaultValueSpel(), defaultValue);

        GraphQLInputObjectField graphQLInputObjectField = GraphQLInputObjectField.newInputObjectField()
                .name(resolvableTypeAccessor.getName())
                .description(resolvableTypeAccessor.getDescription())
                .defaultValue(defaultValue)
                .type(buildGraphQLInputTypeFromMethodParam(dfsContext, resolvableTypeAccessor))
                .build();

        return graphQLInputObjectField;
    }

    // ---

    public GraphQLObjectType findSchemaMutations(DfsContext dfsContext, Class<?> implClass) {
        List<GraphQLFieldDefinition> graphQLFieldDefinitions = findSchemaMutationsFields(dfsContext, implClass);
        return newObject()
                .name(getGraphQLSchemaConfig().getSchemaMutationObjectName())
                .fields(graphQLFieldDefinitions)
                .build();
    }

    public List<GraphQLFieldDefinition> findSchemaMutationsFields(DfsContext dfsContext, Class<?> implClass) {

        final List<GraphQLFieldDefinition> graphQLFieldDefinitions = new ArrayList<GraphQLFieldDefinition>();

        ReflectionUtils.getAllMethods(implClass).forEach(method -> {
            ResolvableTypeAccessor methodReturnTypeResolvableTypeAccessor =
                    ResolvableTypeAccessor.forMethodReturnType(method, implClass);

            if (methodReturnTypeResolvableTypeAccessor.isGraphQLMutation()) {
                String beanName = implClass.getName() + methodReturnTypeResolvableTypeAccessor.getName();
                Object object = getGraphQLSchemaBeanFactory().getBeanByType(implClass);

                String mutationName = StereotypeUtils.getGraphQLMutationName(method, methodReturnTypeResolvableTypeAccessor.getName());

                // --- recursively output object type
                GraphQLObjectType graphQLOutputObjectType = createGraphQLOutputObjectType(dfsContext, mutationName, methodReturnTypeResolvableTypeAccessor);

                // --- recursively input object type
                GraphQLInputType graphQLInputType = createGraphQLInputObjectType(dfsContext, mutationName, method, implClass);

                GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                        .name(mutationName)
                        .description(methodReturnTypeResolvableTypeAccessor.getDescription())
                        .deprecate(methodReturnTypeResolvableTypeAccessor.getGraphQLDeprecationReason())
                        .type(graphQLOutputObjectType)
                        .dataFetcher(new ReflectionGraphQLDataMutator(graphQLSchemaConfig, object, method));

                // input arguments
                if (graphQLInputType != null) {
                    fieldBuilder.argument(newArgument()
                            .name(getGraphQLSchemaConfig().getMutationInputArgumentName())
                            .type(graphQLInputType)
                            .build());
                }
                GraphQLFieldDefinition mutationField = fieldBuilder.build();
                addToFieldDefinitionResolverMap(dfsContext, mutationField, methodReturnTypeResolvableTypeAccessor.getGraphQLComplexitySpelExpression());
                graphQLFieldDefinitions.add(mutationField);
            }
        });

        return graphQLFieldDefinitions;
    }

    public GraphQLObjectType createGraphQLOutputObjectType(DfsContext dfsContext, String mutationName, ResolvableTypeAccessor resolvableTypeAccessor) {

        Class<?> cls = resolvableTypeAccessor.resolve();
        GraphQLType graphQLObjectType = objectTypeResolverMap.get(cls);
        if (graphQLObjectType == null)
            graphQLObjectType = createGraphQLFieldType(dfsContext, resolvableTypeAccessor, true);

        GraphQLOutputType clientMutationIdType = getGraphQLSchemaConfig().isAllowEmptyClientMutationId() ?
                GraphQLString : new GraphQLNonNull(GraphQLString);

        addToMutationReturnTypeResolverMap(dfsContext, mutationName, cls, (GraphQLOutputType) graphQLObjectType);

        GraphQLFieldDefinition graphQLFieldDefinition = newFieldDefinition()
                .name(resolvableTypeAccessor.getGraphQLOutName())
                .deprecate(resolvableTypeAccessor.getGraphQLDeprecationReason())
                .description(resolvableTypeAccessor.getDescription())
                .type((GraphQLOutputType) graphQLObjectType)
                .build();

        addToFieldDefinitionResolverMap(dfsContext, graphQLFieldDefinition, resolvableTypeAccessor.getGraphQLComplexitySpelExpression());

        return newObject()
                .name(resolvableTypeAccessor.getName() + getGraphQLSchemaConfig().getOutputObjectNamePrefix())
                .field(newFieldDefinition()
                        .name(getGraphQLSchemaConfig().getClientMutationIdName())
                        .type(clientMutationIdType)
                        .build())
                .field(graphQLFieldDefinition)
                .build();
    }

    public void addToMutationReturnTypeResolverMap(DfsContext dfsContext, String mutationName, Class<?> implClass, GraphQLOutputType graphQLOutputType) {

        Map<Class<?>, GraphQLOutputType> mutationMap = mutationReturnTypeResolverMap.get(mutationName);
        if (mutationMap == null) {
            mutationMap = new LinkedHashMap<>();
        }
        mutationMap.put(implClass, graphQLOutputType);
        mutationReturnTypeResolverMap.put(mutationName, mutationMap);
    }

    public GraphQLInputType createGraphQLInputObjectType(DfsContext dfsContext, String mutationName, Method method, Class<?> implClass) {

        final List<GraphQLInputObjectField> graphQLInputObjectFields = new ArrayList<>();
        final GraphQLMethodParameters graphQLMethodParameters = new GraphQLMethodParameters(method, implClass);

        if (graphQLMethodParameters.hasParameters()) {
            graphQLMethodParameters.getParameters().forEach(mpi -> {
                if (mpi.isValidGraphQLInParameter()) {
                    GraphQLInputType graphQLInputType = buildGraphQLInputTypeFromMethodParam(dfsContext, mpi.getResolvableTypeAccessor());
                    if (graphQLInputType != null) {
                        graphQLInputType = mpi.isRequired() ? new GraphQLNonNull(graphQLInputType) : graphQLInputType;

                        Object defaultValue = invokeMethodByName(dfsContext, implClass, mpi.getGraphQLInDefaultValueProviderMethodName());
                        defaultValue = evaluateSpElExpression(dfsContext, implClass, null, mpi.getGraphQLInDefaultValueSpel(), defaultValue);

                        GraphQLInputObjectField graphQLInputObjectField = newInputObjectField()
                                .name(mpi.getName())
                                .type(graphQLInputType)
                                .description(mpi.getDescription())
                                .defaultValue(defaultValue)
                                .build();
                        graphQLInputObjectFields.add(graphQLInputObjectField);

                        addToMutationInputTypeResolverMap(dfsContext, mutationName, mpi.getParameterType(), graphQLInputObjectField);
                    }
                } else {
                    // --- context object to bind (GraphQLContext, HttpRequest, SecurityContext etc)
                }
            });
        }

        GraphQLInputType clientMutationIdType = getGraphQLSchemaConfig().isAllowEmptyClientMutationId() ?
                GraphQLString : new GraphQLNonNull(GraphQLString);

        String inputObjectName = StereotypeUtils.getGraphQLMutationName(method, method.getName());

        GraphQLInputObjectType inputObjectType = newInputObject()
                .name(inputObjectName + getGraphQLSchemaConfig().getInputObjectNamePrefix())
                .field(newInputObjectField()
                        .name(getGraphQLSchemaConfig().getClientMutationIdName())
                        .type(clientMutationIdType)
                        .build())
                .fields(graphQLInputObjectFields)
                .build();

        return new GraphQLNonNull(inputObjectType);
    }

    public void addToMutationInputTypeResolverMap(DfsContext dfsContext, String mutationName, Class<?> implClass, GraphQLInputObjectField graphQLInputObjectField) {
        Map<Class<?>, GraphQLInputObjectField> mutationMap = mutationInputTypeResolverMap.get(mutationName);
        if (mutationMap == null) {
            mutationMap = new LinkedHashMap<>();
        }
        mutationMap.put(implClass, graphQLInputObjectField);
        mutationInputTypeResolverMap.put(mutationName, mutationMap);
    }

    public GraphQLSchemaBeanFactory getGraphQLSchemaBeanFactory() {
        return graphQLSchemaBeanFactory;
    }

    public ConcurrentHashMap<Class<?>, GraphQLType> getObjectTypeResolverMap() {
        return objectTypeResolverMap;
    }

    public GraphQLSchemaConfig getGraphQLSchemaConfig() {
        return graphQLSchemaConfig;
    }

    public Map<String, Map<Class<?>, GraphQLOutputType>> getMutationReturnTypeResolverMap() {
        return mutationReturnTypeResolverMap;
    }

    public Map<String, Map<Class<?>, GraphQLInputObjectField>> getMutationInputTypeResolverMap() {
        return mutationInputTypeResolverMap;
    }

    public Map<GraphQLFieldDefinition, GraphQLFieldDefinitionWrapper> getFieldDefinitionResolverMap() {
        return fieldDefinitionResolverMap;
    }

    public ConcurrentHashMap<Class<?>, String> getObjectTypeNameReferenceMap() {
        return objectTypeNameReferenceMap;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public GraphQLMappingContext getGraphQLMappingContext() {
        return graphQLMappingContext;
    }

    // ---

    private class DfsContext {
    }

    public static class GraphQLSchemaTraversalRuntimeException extends NestedRuntimeException {
        public GraphQLSchemaTraversalRuntimeException(String msg) {
            super(msg);
        }
    }

}
