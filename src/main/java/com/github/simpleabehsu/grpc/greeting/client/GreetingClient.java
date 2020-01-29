package com.github.simpleabehsu.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {
    

    public void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",50051)
                .usePlaintext()
                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
//        doClientStreamingCall(channel);
//        doBiDiStreamingCall(channel);
//        doUnaryCallWithDeadline(channel);

        //do something
        System.out.println("Shutting down channel");
        channel.shutdown();


        ManagedChannel securityChannel = NettyChannelBuilder.forAddress("localhost",50051)
                .sslContext(
                        GrpcSslContexts.forClient().trustManager(
                            new File("ssl/ca.crt")
                        ).build()
                )
                .build();

        doUnaryCall(securityChannel);
        System.out.println("Shutting down securityChannel");
        securityChannel.shutdown();

    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // first call (3000ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 3000ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(3000,TimeUnit.MILLISECONDS)).greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder()
                            .setFirstName("abehsu")
                            .build())
                    .build()
            );
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.printf("DeadLine has benn exceeded, we don't want the response");

            } else {
                e.printStackTrace();
            }
        }


        // second call (100ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 100ms");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(100,TimeUnit.MILLISECONDS)).greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder()
                            .setFirstName("abehsu")
                            .build())
                    .build()
            );
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.printf("DeadLine has benn exceeded, we don't want the response");

            } else {
                e.printStackTrace();
            }
        }






    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryOneResquest> requestObserver = asyncClient.greetEveryOne(new StreamObserver<GreetEveryOneResponse>() {
            @Override
            public void onNext(GreetEveryOneResponse greetEveryOneResponse) {
                System.out.println("Response from server; " + greetEveryOneResponse.getResult());

            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();

            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList("Abe","Mark","Amy","James").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestObserver.onNext(
                            GreetEveryOneResquest.newBuilder()
                                    .setGreeting(
                                            Greeting.newBuilder()
                                                    .setFirstName(name)
                                                    .build()
                                    )
                                    .build()
                    );
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3L,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    public static void main(String[] args) throws SSLException {
        System.out.println("Hello I'm a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();

    }
}
