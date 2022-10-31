/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.amazonaws.util.awsclientgenerator.transform;

import com.amazonaws.util.awsclientgenerator.domainmodels.c2j.*;
import com.amazonaws.util.awsclientgenerator.domainmodels.codegeneration.Error;
import com.amazonaws.util.awsclientgenerator.domainmodels.codegeneration.*;
import com.amazonaws.util.awsclientgenerator.domainmodels.codegeneration.cpp.CppViewHelper;
import com.amazonaws.util.awsclientgenerator.generators.exceptions.SourceGenerationFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.WordUtils;

import java.util.*;
import java.util.stream.Collectors;

public class C2jModelToGeneratorModelTransformer {

    private final C2jServiceModel c2jServiceModel;
    Map<String, Shape> shapes;
    Set<String> removedShapes;
    Set<String> removedOperations;
    Map<String, Operation> operations;
    Set<Error> allErrors;
    boolean standalone;
    boolean hasEndpointTrait;
    boolean hasEndpointDiscoveryTrait;
    boolean requireEndpointDiscovery;
    String endpointOperationName;

    public static final HashSet<String> UNSUPPORTEDHTMLTAGS = new HashSet<>();
    static {
        UNSUPPORTEDHTMLTAGS.add("<note>");
        UNSUPPORTEDHTMLTAGS.add("</note>");
        UNSUPPORTEDHTMLTAGS.add("<important>");
        UNSUPPORTEDHTMLTAGS.add("</important>");
    }

    // A list of non-compliant service models that are already released, so we keep them backward-compatible.
    public static final HashSet<String> LEGACY_SERVICE_IDS = new HashSet<>();
    static {
        LEGACY_SERVICE_IDS.add("amp");
        LEGACY_SERVICE_IDS.add("appintegrations");
        LEGACY_SERVICE_IDS.add("billingconductor");
        LEGACY_SERVICE_IDS.add("clouddirectory");
        LEGACY_SERVICE_IDS.add("cloudfront");
        LEGACY_SERVICE_IDS.add("cloudsearch");
        LEGACY_SERVICE_IDS.add("cloudsearchdomain");
        LEGACY_SERVICE_IDS.add("codeartifact");
        LEGACY_SERVICE_IDS.add("codestar-notifications");
        LEGACY_SERVICE_IDS.add("config");
        LEGACY_SERVICE_IDS.add("databrew");
        LEGACY_SERVICE_IDS.add("ec2");
        LEGACY_SERVICE_IDS.add("elasticache");
        LEGACY_SERVICE_IDS.add("emr-containers");
        LEGACY_SERVICE_IDS.add("entitlement.marketplace");
        LEGACY_SERVICE_IDS.add("events");
        LEGACY_SERVICE_IDS.add("evidently");
        LEGACY_SERVICE_IDS.add("forecast");
        LEGACY_SERVICE_IDS.add("forecastquery");
        LEGACY_SERVICE_IDS.add("grafana");
        LEGACY_SERVICE_IDS.add("importexport");
        LEGACY_SERVICE_IDS.add("inspector");
        LEGACY_SERVICE_IDS.add("lambda");
        LEGACY_SERVICE_IDS.add("location");
        LEGACY_SERVICE_IDS.add("lookoutvision");
        LEGACY_SERVICE_IDS.add("m2");
        LEGACY_SERVICE_IDS.add("migrationhubstrategy");
        LEGACY_SERVICE_IDS.add("mobile");
        LEGACY_SERVICE_IDS.add("mobileanalytics");
        LEGACY_SERVICE_IDS.add("mq");
        LEGACY_SERVICE_IDS.add("nimble");
        LEGACY_SERVICE_IDS.add("opensearch");
        LEGACY_SERVICE_IDS.add("rbin");
        LEGACY_SERVICE_IDS.add("rds-data");
        LEGACY_SERVICE_IDS.add("redshift-data");
        LEGACY_SERVICE_IDS.add("resiliencehub");
        LEGACY_SERVICE_IDS.add("rum");
        LEGACY_SERVICE_IDS.add("sagemaker-a2i-runtime");
        LEGACY_SERVICE_IDS.add("sagemaker-edge");
        LEGACY_SERVICE_IDS.add("schemas");
        LEGACY_SERVICE_IDS.add("sdb");
        LEGACY_SERVICE_IDS.add("transcribe");
        LEGACY_SERVICE_IDS.add("transcribe-streaming");
        LEGACY_SERVICE_IDS.add("wisdom");

        // remaps
        LEGACY_SERVICE_IDS.add("lex");
        LEGACY_SERVICE_IDS.add("lexv2-runtime");
        LEGACY_SERVICE_IDS.add("lexv2-models");
        LEGACY_SERVICE_IDS.add("marketplace-entitlement");
        LEGACY_SERVICE_IDS.add("sagemaker-runtime");
        LEGACY_SERVICE_IDS.add("awstransfer");
        LEGACY_SERVICE_IDS.add("transcribestreaming");
        LEGACY_SERVICE_IDS.add("dynamodbstreams");
    }

    public C2jModelToGeneratorModelTransformer(C2jServiceModel c2jServiceModel, boolean standalone) {
        this.c2jServiceModel = c2jServiceModel;
        this.standalone = standalone;
    }

    public ServiceModel convert() {
        ServiceModel serviceModel = new ServiceModel();
        serviceModel.setMetadata(convertMetadata());
        serviceModel.setVersion(c2jServiceModel.getVersion());
        serviceModel.setDocumentation(formatDocumentation(c2jServiceModel.getDocumentation(), 3));
        serviceModel.setServiceName(c2jServiceModel.getServiceName());

        convertShapes();
        convertOperations();
        removeIgnoredOperations();
        removeUnreferencedShapes();
        postProcessShapes();

        serviceModel.setShapes(shapes);
        serviceModel.setOperations(operations);
        serviceModel.setServiceErrors(allErrors);
        serviceModel.getMetadata().setHasEndpointTrait(hasEndpointTrait);
        serviceModel.getMetadata().setHasEndpointDiscoveryTrait(hasEndpointDiscoveryTrait && !endpointOperationName.isEmpty());
        serviceModel.getMetadata().setRequireEndpointDiscovery(requireEndpointDiscovery);
        serviceModel.getMetadata().setEndpointOperationName(endpointOperationName);
        serviceModel.getMetadata().setAwsQueryCompatible(c2jServiceModel.getMetadata().getAwsQueryCompatible() != null);

        if (c2jServiceModel.getEndpointRules() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = null;
            String shortenedRules = c2jServiceModel.getEndpointRules();
            try {
                jsonNode = objectMapper.readValue(shortenedRules, JsonNode.class);
            } catch (JsonProcessingException e) {
                System.err.println("Unable to parse endpoint rules as a json: " + e.getMessage());
            }
            shortenedRules = jsonNode.toString();
            serviceModel.setEndpointRules(shortenedRules);
        }
        serviceModel.setEndpointTests(c2jServiceModel.getEndpointTests());
        serviceModel.setClientContextParams(c2jServiceModel.getClientContextParams());

        return serviceModel;
    }

    String formatDocumentation(String documentation, int indentDepth) {
        if(documentation != null) {
            String tabString = "";
            for(int i = 0; i < indentDepth; ++i) {
                tabString += " ";
            }
            String wrappedString = WordUtils.wrap(documentation, 80, System.lineSeparator() + tabString + "* ", false);
            wrappedString = wrappedString.replace("/*", "/ *").replace("*/", "* /");
            for (String s : UNSUPPORTEDHTMLTAGS) {
                wrappedString = wrappedString.replace(s, "");
            }
            return wrappedString;
        }
        return null;
    }

    String addDocCrossLinks(final String documentation, final String uid, final String shapeOrOperationName) {
        if(documentation != null && uid != null) {
            String seeAlsoRef = String.format("<p><h3>See Also:</h3>   <a href=\"http://docs.aws.amazon.com/goto/WebAPI/%s/%s\">AWS API Reference</a></p>",
                     uid, shapeOrOperationName);

            return documentation + seeAlsoRef;
        }
        return documentation;
    }

    void removeUnreferencedShapes() {
        Iterator<String> iterator = shapes.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (shapes.get(key).getReferencedBy().isEmpty()) {
                iterator.remove();
            }
        }
    }

    Metadata convertMetadata() {
        C2jMetadata c2jMetadata = c2jServiceModel.getMetadata();

        Metadata metadata = new Metadata();
        metadata.setStandalone(standalone);
        metadata.setApiVersion(c2jMetadata.getApiVersion());
        metadata.setConcatAPIVersion(c2jMetadata.getApiVersion().replace("-", ""));
        metadata.setSigningName(c2jMetadata.getSigningName() != null ? c2jMetadata.getSigningName() : c2jMetadata.getEndpointPrefix());

        metadata.setJsonVersion(c2jMetadata.getJsonVersion());
        if("api-gateway".equalsIgnoreCase(c2jMetadata.getProtocol())) {
            metadata.setEndpointPrefix(c2jMetadata.getEndpointPrefix() + ".execute-api");
            metadata.setProtocol("application-json");
            metadata.setApigateway(true);
        } else {
            metadata.setEndpointPrefix(c2jMetadata.getEndpointPrefix());
            metadata.setProtocol(c2jMetadata.getProtocol());
        }
        metadata.setNamespace(c2jMetadata.getServiceAbbreviation());
        metadata.setServiceFullName(c2jMetadata.getServiceFullName());
        metadata.setSignatureVersion(c2jMetadata.getSignatureVersion());
        metadata.setTargetPrefix(c2jMetadata.getTargetPrefix());
        metadata.setGlobalEndpoint(c2jMetadata.getGlobalEndpoint());
        metadata.setTimestampFormat(c2jMetadata.getTimestampFormat());

        if (metadata.getNamespace() == null || metadata.getNamespace().isEmpty()) {
            String serviceId = c2jMetadata.getServiceId();
            if(LEGACY_SERVICE_IDS.contains(this.c2jServiceModel.getServiceName())) {
                serviceId = c2jMetadata.getServiceFullName(); // fallback to full name for legacy already released models
            }
            if(serviceId == null || serviceId.isEmpty()) {
                throw new SourceGenerationFailedException(String.format("Service model file %s must have metadata.serviceId present", c2jServiceModel.getServiceName()));
            }
            metadata.setNamespace(sanitizeServiceAbbreviation(serviceId));
        } else {
            metadata.setNamespace(sanitizeServiceAbbreviation(metadata.getNamespace()));
        }
        metadata.setServiceId(c2jMetadata.getServiceId() != null ? c2jMetadata.getServiceId() : c2jMetadata.getEndpointPrefix());

        if ("S3-CRT".equalsIgnoreCase(c2jServiceModel.getServiceName())) {
            metadata.setNamespace("S3Crt");
        }

        metadata.setClassNamePrefix(CppViewHelper.convertToUpperCamel(ifNotNullOrEmpty(c2jMetadata.getClientClassNamePrefix(), metadata.getNamespace())));

        c2jServiceModel.setServiceName(ifNotNullOrEmpty(c2jServiceModel.getServiceName(), c2jMetadata.getEndpointPrefix()));
        metadata.setProjectName(ifNotNullOrEmpty(c2jMetadata.getClientProjectName(), c2jServiceModel.getServiceName()));

        if(metadata.getProjectName().contains("."))
        {
            metadata.setProjectName(metadata.getProjectName().replace(".", ""));
        }

        return metadata;
    }

    static String ifNotNullOrEmpty(final String target, final String fallback) {
        if (target != null && !target.isEmpty()){
            return target;
        } else {
            return fallback;
        }
    }

    void postProcessShapes() {
        for(Map.Entry<String, Shape> entry : shapes.entrySet()) {
            Shape shape = entry.getValue();

            /*
            If this shape ends up deriving from AmazonStreamingWebServiceRequest, then we already have implemented accessors for ContentType and the
            header insertion there.  So strip this out of the model (affects S3's PutObjectRequest).
            */
            if (shape.hasStreamMembers() && shape.isRequest()) {
                shape.RemoveMember("contentType");
                shape.RemoveMember("ContentType");
            }

            /**
             * Decide event payload type, should be one of them: "blob" | "string" | "structure" | null
             */
            if (shape.isEvent()) {
                if (!shape.getMembers().values().stream().anyMatch(member -> !member.isEventHeader())) {
                    // Header only event
                    shape.setEventPayloadType(null);
                } else if (shape.hasEventPayloadMembers() || shape.getMembers().size() == 1) {
                    if (shape.getMembers().size() == 1) {
                        shape.getMembers().entrySet().stream().forEach(memberEntry -> {
                            memberEntry.getValue().setEventPayload(true);
                            shape.setEventPayloadMemberName(memberEntry.getKey());
                            shape.setEventPayloadType(memberEntry.getValue().getShape().getType());
                        });
                    } else {
                        throw new RuntimeException("Event shape used in Event Stream should only has one member if it has event payload member.");
                    }
                } else if (shape.getMembers().size() > 1) {
                    if (!shape.getType().equals("structure")) {
                        throw new RuntimeException("Event shape should always has \"structure\" type if has multiple members.");
                    }
                    shape.setEventPayloadType(shape.getType());
                } else {
                    shape.setEventPayloadType(null);
                }
            }
        }
    }

    void convertShapes() {
        shapes = new LinkedHashMap<>(c2jServiceModel.getShapes().size());
        removedShapes = new HashSet<String>();

        // First pass adds basic information
        for (Map.Entry<String, C2jShape> entry : c2jServiceModel.getShapes().entrySet()) {
            Shape shape = convertShapeBasics(entry.getValue(), entry.getKey());
            shapes.put(CppViewHelper.convertToUpperCamel(entry.getKey()), shape);
        }

        // Second Pass adds references to other shapes
        for (Map.Entry<String, C2jShape> entry : c2jServiceModel.getShapes().entrySet()) {
            Shape shape = shapes.get(CppViewHelper.convertToUpperCamel(entry.getKey()));
            convertShapeReferences(entry.getValue(), shape);
        }
    }

    Shape convertShapeBasics(C2jShape c2jShape, String shapeName) {

        Shape shape = new Shape();
        HashSet<String> shapesReferencedBy = new HashSet<String>();
        shape.setReferencedBy(shapesReferencedBy);
        shape.setName(CppViewHelper.convertToUpperCamel(shapeName));
        String crossLinkedShapeDocs = addDocCrossLinks(c2jShape.getDocumentation(), c2jServiceModel.getMetadata().getUid(), shape.getName());
        shape.setDocumentation(formatDocumentation(crossLinkedShapeDocs, 3));

        if (c2jShape.getEnums() != null) {
            shape.setEnumValues(new ArrayList<>(c2jShape.getEnums()));
        } else {
            shape.setEnumValues(Collections.emptyList());
        }

        shape.setMax(c2jShape.getMax());
        shape.setMin(c2jShape.getMin());
        shape.setType(c2jShape.getType());
        shape.setLocationName(c2jShape.getLocationName());
        shape.setPayload(c2jShape.getPayload());
        shape.setFlattened(c2jShape.isFlattened());
        shape.setSensitive(c2jShape.isSensitive());
        if("timestamp".equalsIgnoreCase(shape.getType())) {
            // shape's specific timestampFormat overrides the timestampFormat specified in metadata (if any)
            shape.setTimestampFormat(c2jShape.getTimestampFormat() != null ?
                    c2jShape.getTimestampFormat() :
                    c2jServiceModel.getMetadata().getTimestampFormat());
        }
        shape.setEventStream(c2jShape.isEventstream());
        shape.setEvent(c2jShape.isEvent());
        shape.setException(c2jShape.isException());
        shape.setDocument(c2jShape.isDocument());

        if (c2jShape.getXmlNamespace() != null) {
            XmlNamespace xmlns = new XmlNamespace();
            xmlns.setUri(c2jShape.getXmlNamespace().getUri());
            xmlns.setPrefix(c2jShape.getXmlNamespace().getPrefix());
            shape.setXmlNamespace(xmlns);
        }
        return shape;
    }

    void convertShapeReferences(C2jShape c2jShape, Shape shape) {

        if (removedShapes.contains(shape.getName())) {
            return;
        }

        Map<String, ShapeMember> shapeMemberMap = new LinkedHashMap<>();

        Set<String> required;
        if (c2jShape.getRequired() != null) {
            required = new LinkedHashSet<>(c2jShape.getRequired());
        } else {
            required = Collections.emptySet();
        }

        if (c2jShape.getMembers() != null) {
            c2jShape.getMembers().entrySet().stream().filter(entry -> !entry.getValue().isDeprecated()).forEach(entry -> {
                ShapeMember shapeMember = convertMember(entry.getValue(), shape, required.contains(entry.getKey()));
                shapeMemberMap.put(entry.getKey(), shapeMember);
                if (shapeMember.isHostLabel() && !shapeMember.getShape().isString()) {
                    throw new RuntimeException("Shape marked with 'hostLabel' should be of type 'string': " + shape.getName());
                }
                if (shapeMember.isEndpointDiscoveryId() && !shapeMember.getShape().isString()) {
                    throw new RuntimeException("Shape marked with 'endpointdiscoveryid' should be of type 'string': " + shape.getName());
                }
            });
        }

        shape.setMembers(shapeMemberMap);

        // Shape is a List
        if (c2jShape.getMember() != null && !c2jShape.getMember().isDeprecated()) {
            shape.setListMember(convertMember(c2jShape.getMember(), shape, false));
        }

        if (c2jShape.getKey() != null && !c2jShape.getKey().isDeprecated()) {
            shape.setMapKey(convertMember(c2jShape.getKey(), shape, false));
        }

        if (c2jShape.getValue() != null && !c2jShape.getValue().isDeprecated()) {
            shape.setMapValue(convertMember(c2jShape.getValue(), shape, false));
        }
    }

    ShapeMember convertMember(C2jShapeMember c2jShapeMember, Shape shape, boolean required) {
        ShapeMember shapeMember = new ShapeMember();
        shapeMember.setRequired(required);
        shapeMember.setValidationNeeded(required);
        shapeMember.setDocumentation(formatDocumentation(c2jShapeMember.getDocumentation(), 5));
        shapeMember.setFlattened(c2jShapeMember.isFlattened());
        Shape referencedShape = shapes.get(CppViewHelper.convertToUpperCamel(c2jShapeMember.getShape()));
        referencedShape.getReferencedBy().add(shape.getName());
        referencedShape.setReferenced(true);
        shapeMember.setShape(referencedShape);
        shapeMember.setLocationName(c2jShapeMember.getLocationName());
        shapeMember.setLocation(c2jShapeMember.getLocation());
        shapeMember.setQueryName(c2jShapeMember.getQueryName());
        shapeMember.setContextParam(c2jShapeMember.getContextParam());
        shapeMember.setStreaming(c2jShapeMember.isStreaming());
        shapeMember.setIdempotencyToken(c2jShapeMember.isIdempotencyToken());
        shapeMember.setEventPayload(c2jShapeMember.isEventpayload());
        shapeMember.setEventHeader(c2jShapeMember.isEventheader());
        shapeMember.setHostLabel(c2jShapeMember.isHostLabel());
        shapeMember.setEndpointDiscoveryId(c2jShapeMember.isEndpointdiscoveryid());
        shapeMember.setXmlAttribute(c2jShapeMember.isXmlAttribute());
        if(shapeMember.isStreaming()) {
            shapeMember.setRequired(true);
            shapeMember.setValidationNeeded(true);
        }

        if(shapeMember.isUsedForHeader()) {
           shapeMember.setLocationName(shapeMember.getLocationName().toLowerCase());
        }

        if(c2jShapeMember.getXmlNamespace() != null) {
            XmlNamespace xmlns = new XmlNamespace();
            xmlns.setPrefix(c2jShapeMember.getXmlNamespace().getPrefix());
            xmlns.setUri(c2jShapeMember.getXmlNamespace().getUri());
            shapeMember.setXmlNamespace(xmlns);
        }

        return shapeMember;
    }

    void removeIgnoredOperations() {
        // Backward propagation to mark all operations containing removed shapes.
        for (String shapeName : removedShapes) {
            markRemovedOperations(shapeName);
        }

        // Forward propagation to dereference all shapes related to the operations should be ignored.
        for (String operationName : removedOperations) {
            operations.get(operationName).getRequest().getShape().getReferencedBy().clear();
            dereferenceShape(operations.get(operationName).getRequest().getShape());
            operations.get(operationName).getResult().getShape().getReferencedBy().clear();
            dereferenceShape(operations.get(operationName).getResult().getShape());
            operations.remove(operationName);
        }
    }

    void markRemovedOperations(String name) {
        if (operations.containsKey(name)) {
            removedOperations.add(name);
        }
        else if (shapes.containsKey(name)) {
            Shape shapeShouldIgnore = shapes.get(name);
            for (String shapeName : shapeShouldIgnore.getReferencedBy()) {
                markRemovedOperations(shapeName);
            }
            shapeShouldIgnore.getReferencedBy().clear();
        }
    }

    void dereferenceShape(Shape topShape) {
        if (topShape.getMembers() == null) {
            return;
        }
        for (Map.Entry<String, ShapeMember> entry : topShape.getMembers().entrySet()) {
            entry.getValue().getShape().getReferencedBy().remove(topShape.getName());
            if (entry.getValue().getShape().getReferencedBy().isEmpty()) {
                dereferenceShape(entry.getValue().getShape());
            }
        }
    }

    void convertOperations() {
        allErrors = new HashSet<>();
        operations = new LinkedHashMap<>(c2jServiceModel.getOperations().size());
        removedOperations = new HashSet<>();
        for (Map.Entry<String, C2jOperation> entry : c2jServiceModel.getOperations().entrySet()) {
            if(!entry.getValue().isDeprecated()) {
                operations.put(entry.getKey(), convertOperation(entry.getValue()));
            }
        }
    }

    Operation convertOperation(C2jOperation c2jOperation) {
        Operation operation = new Operation();

        // name
        operation.setName(c2jOperation.getName());

        operation.setEndpointOperation(c2jOperation.isEndpointoperation());
        operation.setHasEndpointDiscoveryTrait(c2jOperation.getEndpointdiscovery() == null ? false :true);
        operation.setRequireEndpointDiscovery(operation.isHasEndpointDiscoveryTrait() ? c2jOperation.getEndpointdiscovery().isRequired() : false);

        if (operation.isEndpointOperation()) {
            endpointOperationName = operation.getName();
        }
        if (operation.isHasEndpointDiscoveryTrait()) {
            hasEndpointDiscoveryTrait = true;
        }
        if (operation.isRequireEndpointDiscovery()) {
            requireEndpointDiscovery = true;
        }

        // Documentation
        String crossLinkedShapeDocs =
                addDocCrossLinks(c2jOperation.getDocumentation(), c2jServiceModel.getMetadata().getUid(), c2jOperation.getName());

        operation.setDocumentation(formatDocumentation(crossLinkedShapeDocs, 9));
        operation.setAuthtype(c2jOperation.getAuthtype());
        operation.setAuthorizer(c2jOperation.getAuthorizer());
        if (c2jOperation.getEndpoint() != null) {
            operation.setEndpoint(convertEndpoint(c2jOperation.getEndpoint()));
            hasEndpointTrait = true;
            operation.setHasEndpointTrait(true);
        }

        if(operation.getAuthtype() == null) {
            if(c2jServiceModel.getMetadata().getSignatureVersion() != null &&
                    c2jServiceModel.getMetadata().getSignatureVersion().equals("bearer")) {
                operation.setSignerName("Aws::Auth::BEARER_SIGNER");
            } else {
                operation.setSignerName("Aws::Auth::SIGV4_SIGNER");
            }
        } else if (operation.getAuthtype().equals("v4-unsigned-body")) {
            operation.setSignerName("Aws::Auth::SIGV4_SIGNER");
        } else if (operation.getAuthtype().equals("bearer")) {
            operation.setSignerName("Aws::Auth::BEARER_SIGNER");
        } else if (operation.getAuthtype().equals("custom")) {
            operation.setSignerName("\"" + operation.getAuthorizer() + "\"");
        } else {
            operation.setSignerName("Aws::Auth::NULL_SIGNER");
        }

        operation.setStaticContextParams(c2jOperation.getStaticContextParams());

        // input
        if (c2jOperation.getInput() != null) {
            String requestName = c2jOperation.getName() + "Request";
            Shape requestShape = renameShape(shapes.get(c2jOperation.getInput().getShape()), requestName);
            requestShape.setRequest(true);
            requestShape.setReferenced(true);
            requestShape.getReferencedBy().add(c2jOperation.getName());
            requestShape.setLocationName(c2jOperation.getInput().getLocationName());
            if (c2jOperation.getInput().getXmlNamespace() != null) {
                XmlNamespace xmlns = new XmlNamespace();
                xmlns.setUri(c2jOperation.getInput().getXmlNamespace().getUri());
                xmlns.setPrefix(c2jOperation.getInput().getXmlNamespace().getPrefix());
                requestShape.setXmlNamespace(xmlns);
            }
            if(requestShape.getLocationName() != null && requestShape.getLocationName().length() > 0 &&
                    (requestShape.getPayload() == null || requestShape.getPayload().length() == 0) ) {
                requestShape.setPayload(requestName);
            }

            requestShape.setSignBody(true);
            if (operation.getAuthtype() != null && operation.getAuthtype().equals("v4-unsigned-body")) {
                requestShape.setSignBody(false);
            }

            if(c2jOperation.isHttpChecksumRequired()) {
                requestShape.setComputeContentMd5(true);
            }

            requestShape.getMembers().values().stream().filter(member -> member.getShape().isEventStream()).forEach(member -> member.getShape().setOutgoingEventStream(true));

            ShapeMember requestMember = new ShapeMember();
            requestMember.setShape(requestShape);
            requestMember.setDocumentation(formatDocumentation(c2jOperation.getInput().getDocumentation(), 3));

            // verify that member-refs in hostPrefix are all required members.
            operation.setRequest(requestMember);
            if (operation.getEndpoint() != null) {
                List<String> requiredMembers = operation.getEndpoint().getMemberReferences();
                for (int i = 0; i < requiredMembers.size(); i++) {
                    if (!requestShape.getMembers().get(requiredMembers.get(i)).isRequired()) {
                        throw new RuntimeException("HostPrefix member-ref should be Required. Operation: " + operation.getName() + ", member in Request: " + requiredMembers.get(i));
                    }
                    if (!requestShape.getMembers().get(requiredMembers.get(i)).isHostLabel()) {
                        throw new RuntimeException("HostPrefix member-ref should be marked with 'hostLabel'. Operation: " + operation.getName() + ", member in Request: " + requiredMembers.get(i));
                    }
                }
            }
        }

        // output
        if (c2jOperation.getOutput() != null) {
            String resultName = c2jOperation.getName() + "Result";
            Shape resultShape = renameShape(shapes.get(c2jOperation.getOutput().getShape()), resultName);
            resultShape.setResult(true);
            resultShape.setReferenced(true);
            resultShape.getReferencedBy().add(c2jOperation.getName());
            ShapeMember resultMember = new ShapeMember();
            resultMember.setShape(resultShape);
            resultMember.setDocumentation(formatDocumentation(c2jOperation.getOutput().getDocumentation(), 3));
            operation.setResult(resultMember);
        }
        // http
        operation.setHttp(convertHttp(c2jOperation.getHttp()));

        // http checksum
        if (c2jOperation.getHttpChecksum() != null) {
            C2jHttpChecksum c2jHttpChecksum = c2jOperation.getHttpChecksum();

            operation.setRequestChecksumRequired(c2jHttpChecksum.isRequestChecksumRequired());
            operation.setRequestAlgorithmMember(c2jHttpChecksum.getRequestAlgorithmMember());
            operation.setRequestValidationModeMember(c2jHttpChecksum.getRequestValidationModeMember());
            if (c2jHttpChecksum.getResponseAlgorithms() != null) {
                operation.setResponseAlgorithms(new ArrayList<>(c2jHttpChecksum.getResponseAlgorithms()));
            }
        }

        // errors

        List<Error> operationErrors = new ArrayList<>();
        if (c2jOperation.getErrors() != null) {
            operationErrors.addAll(c2jOperation.getErrors().stream().map(c2jError -> convertError(c2jError, c2jOperation.getName())).collect(Collectors.toList()));
        }

        operation.setErrors(operationErrors);

        return operation;
    }

    Shape renameShape(Shape shape, String name) {
        if (shape.getName().equals(name)) {
            return shape;
        }

        // Detect any conflicts with shape name defined by service team, need to rename it if so.
        Optional<String> conflicted = shapes.keySet().stream().filter(shapeName ->
            name.equals(shapeName) || shape.getMembers().values().stream().anyMatch(shapeMember ->
                shapeMember.getShape().getName().equals(shapeName) && (name.equals("Get" + shapeName) || name.equals("Set" + shapeName)))).findFirst();
        if (conflicted.isPresent()) {
            String originalShapeName = conflicted.get();
            String newShapeName = "";
            switch(originalShapeName) {
                case "CopyObjectResult":
                    newShapeName = "CopyObjectResultDetails";
                    renameShapeMember(shape, "CopyObjectResult", originalShapeName, newShapeName, newShapeName, true);
                    break;
                case "BatchUpdateScheduleResult":
                    shapes.remove(originalShapeName);
                    break;
                case "GeneratedPolicyResult":
                    newShapeName = "GeneratedPolicyResults";
                    renameShapeMember(shape, "generatedPolicyResult", originalShapeName, newShapeName, newShapeName, false);
                    break;
                case "SearchResult":
                    newShapeName = "SearchResultDetails";
                    renameShapeMember(shape, "results", originalShapeName, "results", newShapeName, true);
                    break;
                default:
                    throw new RuntimeException("Unhandled shape name conflict: " + name);
            }
        }

        Shape cloned = cloneShape(shape);
        cloned.setName(name);
        shapes.put(name, cloned);
        return cloned;
    }

    Shape cloneShape(Shape shape) {
        Shape cloned = new Shape();
        cloned.setReferencedBy(shape.getReferencedBy());
        cloned.setDocumentation(shape.getDocumentation());
        cloned.setEnumValues(shape.getEnumValues());
        cloned.setListMember(shape.getListMember());
        cloned.setMapKey(shape.getMapKey());
        cloned.setMapValue(shape.getMapValue());
        cloned.setMax(shape.getMax());
        cloned.setMin(shape.getMin());
        cloned.setMembers(shape.getMembers());
        cloned.setResult(shape.isResult());
        cloned.setRequest(shape.isRequest());
        cloned.setType(shape.getType());
        cloned.setPayload(shape.getPayload());
        cloned.setFlattened(shape.isFlattened());
        cloned.setTimestampFormat(shape.getTimestampFormat());
        cloned.setComputeContentMd5(shape.isComputeContentMd5());
        cloned.setSupportsPresigning(shape.isSupportsPresigning());
        cloned.setSignBody(shape.isSignBody());
        cloned.setSignerName(shape.getSignerName());
        cloned.setEventStream(shape.isEventStream());
        cloned.setEvent(shape.isEvent());
        cloned.setException(shape.isException());
        cloned.setXmlNamespace(shape.getXmlNamespace());
        cloned.setDocument(shape.isDocument());
        return cloned;
    }

    /**
     * Renames shape in the model tree
     *
     * @param parentShape - current parent in which renamed Shape being located
     * @param originalMemberKey - original key name in the parent of the Shape being renamed
     * @param originalShapeName - original name of the Shape
     * @param newMemberKey - new key name in the parent of the Shape being renamed
     * @param newShapeName - new Shape name
     * @param isPayload - if the current Shape being renamed is a payload of a parent
     */
    void renameShapeMember(Shape parentShape,
                           String originalMemberKey, String originalShapeName,
                           String newMemberKey, String newShapeName, boolean isPayload) {
        if (!shapes.containsKey(originalShapeName)) {
            throw new NoSuchElementException("Shape to rename was not found in all shapes: " + originalShapeName);
        }
        if (!parentShape.getMembers().containsKey(originalMemberKey)) {
            throw new NoSuchElementException("Requested to rename non-existent child shape key "
                    + originalMemberKey + " of a Shape type " + originalShapeName);
        }
        shapes.get(originalShapeName).setName(newShapeName);
        shapes.put(newShapeName, shapes.get(originalShapeName));
        shapes.remove(originalShapeName);
        parentShape.getMembers().put(newMemberKey, parentShape.getMembers().get(originalMemberKey));
        parentShape.RemoveMember(originalMemberKey);
        if (isPayload)
        {
            parentShape.setPayload(newMemberKey);
        }
    }

    Http convertHttp(C2jHttp c2jHttp) {
        Http http = new Http();
        http.setMethod(c2jHttp.getMethod());
        http.setRequestUri(c2jHttp.getRequestUri());
        if (c2jServiceModel.getEndpointRules() != null && c2jHttp.getRequestUri() != null) {
            // Legacy C2J models require path arguments preprocessed (i.e. removed) to avoid duplication with URI segments
            if(c2jServiceModel.getServiceName().equalsIgnoreCase("S3")) {
                String bucketPathToRemove = "/{Bucket}";
                String requestUri = c2jHttp.getRequestUri();
                if (requestUri.startsWith(bucketPathToRemove)) {
                    requestUri = requestUri.substring(bucketPathToRemove.length());
                    if (requestUri.isEmpty()) {
                        requestUri = null;
                    }
                }
                http.setRequestUri(requestUri);
            }
        }
        http.setResponseCode(c2jHttp.getResponseCode());
        return http;
    }

    Endpoint convertEndpoint(C2jEndpoint c2jEndpoint) {
        Endpoint endpoint = new Endpoint();
        endpoint.setHostPrefix(c2jEndpoint.getHostPrefix());
        return endpoint;
    }

    Error convertError(C2jError c2jError, String operationName) {
        Error error = new Error();
        error.setModeled(false);
        if(c2jServiceModel.getShapes().get(c2jError.getShape()) != null) {
            C2jShape shape = c2jServiceModel.getShapes().get(c2jError.getShape());
            c2jError.setError(shape.getError());
            c2jError.setException(shape.isException());

            Shape referencedShape = shapes.get(CppViewHelper.convertToUpperCamel(c2jError.getShape()));
            referencedShape.getReferencedBy().add(operationName);
            referencedShape.setReferenced(true);
            // We define an error as a modeled error if:
            // 1. Its shape is defined in C2J model.
            // 2. For XML payload type, it has extra member other than "message" and "code" (case insensitive).
            // 3. For JSON payload type, it has extra member other than "message" (case insensitive).
            String errorPayloadType = CppViewHelper.computeServicePayloadType(this.c2jServiceModel.getMetadata().getProtocol());
            if (errorPayloadType.equals("xml") && referencedShape.isXmlModeledException() ||
                errorPayloadType.equals("json") && referencedShape.isJsonModeledException()) {
                error.setModeled(true);
            }

            if (shape.getRetryable() != null && shape.getRetryable().getOrDefault("throttling", false)) {
                error.setRetryable(true);
            }
        }
        error.setDocumentation(formatDocumentation(c2jError.getDocumentation(), 3));
        error.setName(CppViewHelper.convertToUpperCamel(c2jError.getShape()));
        error.setText(c2jError.getShape());
        error.setException(c2jError.isException());
        error.setFault(c2jError.isFault());
        error.setCoreError(CoreErrors.VARIANTS.containsKey(error.getName()));

        //query xml loads this inner structure to do this work.
        if (c2jError.getError() != null && c2jError.getError().getCode() != null) {
            if(c2jError.getError().getHttpStatusCode() >= 500 || !c2jError.getError().isSenderFault()) {
                error.setRetryable(true);
            }

            error.setText(c2jError.getError().getCode());
        }

        allErrors.add(error);
        return error;
    }

    String sanitizeServiceAbbreviation(String serviceAbbreviation) {
        return serviceAbbreviation.replace(" ", "").replace("-", "").replace("_", "").replace("Amazon", "").replace("AWS", "").replace("/", "");
    }
}
