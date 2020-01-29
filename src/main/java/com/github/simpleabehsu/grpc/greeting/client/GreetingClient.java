package com.github.simpleabehsu.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {
    

    public void run(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",50051)
                .usePlaintext()
                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
        doClientStreamingCall(channel);
        
        //do something
        System.out.println("Shutting down channel");
        channel.shutdown();

    }

    private void doClientStreamingCall(ManagedChannel channel) {

        GreetServiceGrpc.GreetServiceStub ayncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);


        StreamObserver<LongGreetResquest> requestObserver =  ayncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse longGreetResponse) {
                //we get a response from the server
                System.out.printf("Receive a response from the server");
                System.out.printf(longGreetResponse.getResult());

                //onNext will be called only once

            }

            @Override
            public void onError(Throwable throwable) {
                // we get an error from the server

            }

            @Override
            public void onCompleted() {
                // the server is done sending us data

                //onCompleted will be called right after onNext()
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        // streaming message #1
        System.out.println("sending message 1");
        requestObserver.onNext(LongGreetResquest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder()
                                .setFirstName("James")
                                .build()
                )
                .build()
        );

        // streaming message #2
        System.out.println("sending message 2");
        requestObserver.onNext(LongGreetResquest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder()
                                .setFirstName("John")
                                .build()
                )
                .build()
        );

        // streaming message #3
        System.out.println("sending message 3");
        requestObserver.onNext(LongGreetResquest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder()
                                .setFirstName("abehsu")
                                .build()
                )
                .build()
        );

        // we tell the server that the client is done sending data.
        requestObserver.onCompleted();


        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void doServerStreamingCall(ManagedChannel channel) {

        System.out.println("Createing stub");
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

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


    }

    private void doUnaryCall(ManagedChannel channel) {
        //Unary Example
        System.out.println("Createing stub");
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("abe")
                .setLastName("Hsu")
                .build();

        // Created a protocol buffer request message
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // call the RPC and get back a GreetResponse (protocol buffers)
        GreetResponse greetResponse =  greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());

    }

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();

    }
}
