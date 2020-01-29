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
}
