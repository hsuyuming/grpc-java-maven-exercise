package com.github.simpleabehsu.grpc.greeting.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
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
}
