package com.github.simpleabehsu.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        //extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        //create the response
        String result = "Hello " + firstName;
        GreetResponse reponse = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        //sent the response
        responseObserver.onNext(reponse );

        //complete the RPC call
        responseObserver.onCompleted();

        //        super.greet(request, responseObserver);
    }


    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {

        String firstName = request.getGreeting().getFirstName();

        try {
            for (int i = 0 ; i < 10 ; i++){
                String result = "Hello " + firstName + ", response number: " + i;

                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();

                responseObserver.onNext(response);

                Thread.sleep(1000L);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }

        //super.greetManyTimes(request, responseObserver);
    }


    @Override
    public StreamObserver<LongGreetResquest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {

        StreamObserver<LongGreetResquest> requestObserver = new StreamObserver<LongGreetResquest>() {

            String result = "";

            // How to react with the new message come in .
            @Override
            public void onNext(LongGreetResquest value) {
                // client sends a message
                // doing some processing
                result += "Hello " + value.getGreeting().getFirstName() + "! ";
            }

            @Override
            public void onError(Throwable t) {
                // client sends an error
            }

            @Override
            public void onCompleted() {
                // client is done

                // this is when we want to return a response (responseObserver)
                responseObserver.onNext(LongGreetResponse.newBuilder()
                        .setResult(result)
                        .build()
                );

                responseObserver.onCompleted();
            }
        };

        return requestObserver;

        //        return super.longGreet(responseObserver);
    }


    @Override
    public StreamObserver<GreetEveryOneResquest> greetEveryOne(StreamObserver<GreetEveryOneResponse> responseObserver) {

        StreamObserver<GreetEveryOneResquest> requestObserver = new StreamObserver<GreetEveryOneResquest>() {
            @Override
            public void onNext(GreetEveryOneResquest greetEveryOneResquest) {
                String result = "Hello " + greetEveryOneResquest.getGreeting().getFirstName();
                GreetEveryOneResponse greetEveryOneResponse = GreetEveryOneResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(greetEveryOneResponse);

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();

            }
        };

        return requestObserver;

    }
}
