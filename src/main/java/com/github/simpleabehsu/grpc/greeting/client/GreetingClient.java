package com.github.simpleabehsu.grpc.greeting.client;

import com.proto.greet.GreetManyTimesRequest;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",50051)
                .usePlaintext()
                .build();

        System.out.println("Createing stub");
//        old and dummy
//        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
//        DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.DummyServiceFutureStub(channel);

        // Created a greet service client (blocking - synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

//        Unary Example
//        // Created a protocol buffer greeting message
//        Greeting greeting = Greeting.newBuilder()
//                .setFirstName("abe")
//                .setLastName("Hsu")
//                .build();
//
//        // Created a protocol buffer request message
//        GreetRequest greetRequest = GreetRequest.newBuilder()
//                .setGreeting(greeting)
//                .build();
//
//        // call the RPC and get back a GreetResponse (protocol buffers)
//        GreetResponse greetResponse =  greetClient.greet(greetRequest);
//
//        System.out.println(greetResponse.getResult());


        // Server Streaming
        //we prepare the request
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("abehsu").build())
                .build();

        // we stream the reponses ( in a blocking manner)
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining( greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });



        //do something
        System.out.println("Shutting down channel");
        channel.shutdown();

    }
}
