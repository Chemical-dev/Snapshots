package com.example.publicsnapshots;

import com.alibaba.fastjson.JSONArray;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.policy.internal.JsonPolicyReader;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.identitymanagement.model.Policy;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3control.model.PutBucketPolicyRequest;
import com.amazonaws.services.s3control.model.PutBucketPolicyResult;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import top.jfunc.json.JsonArray;
import top.jfunc.json.JsonObject;
import top.jfunc.json.impl.JSONObject;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.amazonaws.auth.policy.actions.EC2Actions.DescribeSnapshotAttribute;
import static com.amazonaws.auth.policy.actions.EC2Actions.ModifySnapshotAttribute;
import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;

@NoArgsConstructor
@Service
public class SnapshotServiceImpl {
  //  private DescribeSnapshotsRequest describeSnapshotsRequest = new DescribeSnapshotsRequest();
    private Set<String> stringSet = new HashSet<>();
    private DescribeSnapshotsRequest describeSnapshotsRequest = new DescribeSnapshotsRequest().withOwnerIds("343873837875");
    private List<PublicSnapShots> snapShotsList = new ArrayList<>();

//    public Ec2Client connectEC2Client() {
//        return Ec2Client.builder()
//                .region(Region.EU_CENTRAL_1)
//                .credentialsProvider(
//                        StaticCredentialsProvider.create(
//                                AwsBasicCredentials.create(
//                                )
//                        )
//                )
//                .build();
//    }

    public AmazonEC2 connectEC2Client() {
        return AmazonEC2ClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    public List<PublicSnapShots> getSnapshot() {
        stringSet.add("343873837875");
//        System.out.println(connectEC2Client());
        DescribeSnapshotsResult result = connectEC2Client().describeSnapshots(describeSnapshotsRequest);
        List<Snapshot> snapshots = result.getSnapshots();
        List<String> strings = snapshots.stream().map(id-> id.getSnapshotId()).collect(Collectors.toList());
        System.out.println(strings);
//        System.out.println(snapshots.get(0));
        snapshots.forEach(snapshot -> {
            System.out.println(snapshot);
            DescribeSnapshotAttributeRequest describeSnapshotAttribute = new DescribeSnapshotAttributeRequest();
            describeSnapshotAttribute.withSnapshotId(snapshot.getSnapshotId())
                    .withAttribute("createVolumePermission");
            DescribeSnapshotAttributeResult describeSnapshotAttributeResult = connectEC2Client().describeSnapshotAttribute(describeSnapshotAttribute);
            System.out.println("describeSnapshotAttributeResult" + describeSnapshotAttributeResult.getCreateVolumePermissions().get(0).getGroup());
            if(describeSnapshotAttributeResult.getCreateVolumePermissions().get(0).getGroup().equalsIgnoreCase("all")){
                ModifySnapshotAttributeRequest modifySnapshotAttributeRequest = new ModifySnapshotAttributeRequest()
                        .withSnapshotId(snapshot.getSnapshotId())
                        .withAttribute("createVolumePermission")
                        .withOperationType("remove")
                        .withGroupNames("all");
                ModifySnapshotAttributeResult modifySnapshotAttributeResult = connectEC2Client().modifySnapshotAttribute(modifySnapshotAttributeRequest);
                System.out.println("ModifySnapshotAttributeResult:" + describeSnapshotAttributeResult.getCreateVolumePermissions().get(0).getGroup());
            }
        });
//        String it = snapShotsList.iterator().next().accountId;
//        System.out.println("iterator:" + it);
        return snapShotsList;
    }

    public List<PublicSnapShots> getAttributes() {
//        CreateSnapshotRequest request = new CreateSnapshotRequest()
//                .withVolumeId("vol-01a5b741c3f072e20")
//                .withDescription("Alike snapshot");
//        connectEC2Client().createSnapshot(request);
//        System.out.println(request);
        List<String> snapShotIds = getSnapshot().stream().map(snapshot->snapshot.getSnapShotId())
                .collect(Collectors.toList());
//        System.out.println(snapShotIds);
        snapShotIds.forEach(id ->{
            System.out.println(id);
            DescribeSnapshotAttributeRequest describeSnapshotAttributeRequest1 = new DescribeSnapshotAttributeRequest();
            describeSnapshotAttributeRequest1
                    .withSnapshotId(id)
                   .withAttribute("createVolumePermission");
            System.out.println(describeSnapshotAttributeRequest1);
            DescribeSnapshotAttributeResult describeSnapshotAttributeResult = connectEC2Client().describeSnapshotAttribute(describeSnapshotAttributeRequest1);
            List<CreateVolumePermission> createVolumePermission = describeSnapshotAttributeResult.getCreateVolumePermissions();
            Iterator<CreateVolumePermission> it = createVolumePermission.iterator();
            System.out.println("permissions:" + it.next().getGroup());
        });

        return snapShotsList;
    }

    public void modifyAttribute(){
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        ModifySnapshotAttributeRequest request = new ModifySnapshotAttributeRequest()
                .withSnapshotId("snap-07cd46262a5c8cfba") // ID of the snapshot
                .withAttribute("createVolumePermission") // attribute to modify
                .withOperationType("add") // operation to perform (add or remove)
                .withUserIds("343873837875") // user IDs to add or remove
                .withGroupNames("all")
                .withOperationType(OperationType.Add);

        ec2.modifySnapshotAttribute(request);
    }
        public List<String> ListFunctions(){
            AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
            List<String> policies = new ArrayList<>();
            ListFunctionsRequest listFunctionsRequest = new ListFunctionsRequest();
            ListFunctionsResult listFunctionsResult = lambdaClient.listFunctions(listFunctionsRequest);
            listFunctionsResult.getFunctions().forEach(function -> {
//                ListTagsRequest listTagsRequest = new ListTagsRequest();
//                listTagsRequest.setResource(function.getMasterArn());
//                ListTagsResult tagsResponse = lambdaClient.listTags(listTagsRequest);
//                Map<String, String> tag = tagsResponse.getTags();
//                System.out.println(tag);
//                String tag = String.valueOf(lambdaClient.listTags(listTagsRequest));
//                System.out.println(tag);
                GetPolicyRequest request = new GetPolicyRequest().withFunctionName("testdev");
                GetPolicyResult result = lambdaClient.getPolicy(request);
               policies.add(result.getPolicy());

                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode[] myJsonPolicoes = mapper.readValue(policies.toString(), JsonNode[].class);
                 Map<String, String> gg = new HashMap<>();
                    for(JsonNode policy : myJsonPolicoes){
//                        System.out.println("policies:" + policy);
//                        System.out.println(policy.findValue("Resource"));
                        RemovePermissionRequest removePermissionRequest = new RemovePermissionRequest();
                        removePermissionRequest.withFunctionName(function.getFunctionName());
                        System.out.println(policy.findValue("Sid").toString().replaceAll("", ""));
                        if(policy.findValue("Sid").toString().replaceAll("", "") == "FunctionURLAllowPublicAccess");
                        removePermissionRequest.withStatementId(policy.findValue("Sid").toString());
                        lambdaClient.removePermission(removePermissionRequest);
                        AddPermissionRequest addPermissionRequest = new AddPermissionRequest();

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//             Policy policy = new Policy();
//                JSONObject object = new JSONObject(result.getPolicy());
//                System.out.println(object.get("Statement"));
//                List<String> version = Collections.singletonList(object.getString("Statement"));
//                version.forEach(versio->{
//                    System.out.println("my vision:" + versio);
//                });

//                System.out.println(policy);

//                ObjectMapper mapper = new ObjectMapper();
//                try {
//                    Policy policy = mapper.readValue(result.getPolicy(), Policy.class);
//
////                    System.out.println(policy.getPolicyName());
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
//                String policy = result.getPolicy();
            });

//            System.out.println(policies);
//            System.out.println("Functions: " + listFunctionsResult.getFunctions());
            return policies;
        }
        public void getVpcs(){
            AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        DescribeSecurityGroupsResult describeSecurityGroupsResult = ec2.describeSecurityGroups(describeSecurityGroupsRequest);
            System.out.println(describeSecurityGroupsResult);
            List<SecurityGroup> securityGroupsList = describeSecurityGroupsResult.getSecurityGroups();
            for (SecurityGroup securityGroup : securityGroupsList){
                Iterator<IpPermission> iterator = (Iterator<IpPermission>) securityGroup.getIpPermissionsEgress();
            }
        }

    public AmazonS3 getS3Client() {
            return AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build();
    }

    public AmazonS3 getS3ClientWithRegion(String region) {
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    }
        public List<Bucket> getBucketPolicy(){
            ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
            List<Bucket> buckets = getS3Client().listBuckets(listBucketsRequest);
            System.out.println("buckets:  " + buckets);

            buckets.forEach(bucket -> {
                GetBucketLocationRequest getBucketLocationRequest = new GetBucketLocationRequest(bucket.getName());
                getBucketLocationRequest.withBucketName(bucket.getName());
                String region = getS3Client().getBucketLocation(getBucketLocationRequest);
                System.out.println("location:  " + region);
                region = checkBucketRegion(region);
                Regions tRegion = Regions.fromName(region);
                String technicalName = tRegion.getName();
//                getS3Client().setRegion(Region.getRegion(tRegion));
                GetBucketPolicyRequest getBucketPolicyRequest = new GetBucketPolicyRequest(bucket.getName());
                getBucketPolicyRequest.withBucketName(bucket.getName());

                try {
                    BucketPolicy bucketPolicy = getS3ClientWithRegion(technicalName).getBucketPolicy(getBucketPolicyRequest);
//                    System.out.println("policy:  " + bucketPolicy.getPolicyText());


                    if(bucketPolicy.getPolicyText() != null) {
                       JSONObject jsonObject = new JSONObject(bucketPolicy.getPolicyText());
                       String version = jsonObject.getString("Version");

                        JsonArray statementArray =  jsonObject.getJsonArray("Statement");
                        JsonObject statement1 =  statementArray.getJsonObject(0);

                        BucketPolicy currentPolicy = getS3ClientWithRegion(technicalName).getBucketPolicy(bucket.getName());
                        currentPolicy.getPolicyText().replace("Effect\": \"Allow\"", "Effect\": \"Deny\"");

                        getS3ClientWithRegion(technicalName).setBucketPolicy(bucket.getName(), bucketPolicy.getPolicyText());
//                        String permission = statement1.getString("Effect");
//                        permission = "Deny";
                       System.out.println("statement:  " + statement1.getString("Effect"));

                    }
//                    getS3ClientWithRegion(technicalName).setBucketPolicy(bucket.getName(), bucketPolicy.getPolicyText());
                }catch(AmazonServiceException e){
                    if(e.getStatusCode() == 403) {
                        System.out.println("Access denied to bucket policy");
                    }else {
                        throw e;
                    }
                }
            });
        return buckets;
        }

    private String checkBucketRegion(String checkBucketRegion) {
        if (checkBucketRegion == "US")
            return "us-east-1";
        else if (checkBucketRegion == "EU")
            return "eu-west-1";

        return checkBucketRegion;

    }

}
