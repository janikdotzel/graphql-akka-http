package com.example.graphql;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Main {

    public static void main(String[] args) {
        Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {
            ActorRef<UserRegistry.Command> userRegistryActor = context.spawn(UserRegistry.create(), "UserRegistry");

            // Start GraphQL API
            GraphQLServer.startServer(context.getSystem(), userRegistryActor);

            return Behaviors.empty();
        });

        ActorSystem.create(rootBehavior, "UserRegistryGraphQL");
    }
}