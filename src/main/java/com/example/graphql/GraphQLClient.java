package com.example.graphql;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.http.javadsl.unmarshalling.Unmarshaller;

import java.util.concurrent.CompletionStage;

public class GraphQLClient {

    private static final String SERVER_URL = "http://localhost:8081/graphql";

    public static void main(String[] args) {
        Behavior<Void> rootBehavior = Behaviors.setup(context -> {

            // Create a user mutation
            String createUserMutation = "mutation { createUser(name: \"John\", age: 30, countryOfResidence: \"USA\") }";
            sendGraphQLRequest(createUserMutation, context.getSystem(), "Create User Mutation");

            Thread.sleep(1000);

            // Query all users
            String allUsersQuery = "query { allUsers { name age countryOfResidence } }";
            sendGraphQLRequest(allUsersQuery, context.getSystem(), "All Users Query");

            Thread.sleep(1000);

            // Query a specific user
            String userQuery = "query { user(name: \"John\") { name age countryOfResidence } }";
            sendGraphQLRequest(userQuery, context.getSystem(), "User Query");

            Thread.sleep(1000);

            // Delete a user mutation
            String deleteUserMutation = "mutation { deleteUser(name: \"John\") }";
            sendGraphQLRequest(deleteUserMutation, context.getSystem(), "Delete User Mutation");

            return Behaviors.empty();
        });

        ActorSystem.create(rootBehavior, "graphql-client");
    }

    private static void sendGraphQLRequest(String query, ActorSystem<Void> system, String operationName) {
        String payload = String.format("{\"query\": \"%s\", \"variables\": {}}", query.replace("\"", "\\\""));

        HttpRequest request = HttpRequest
                .POST(SERVER_URL)
                .withEntity(HttpEntities.create(ContentTypes.APPLICATION_JSON, payload));

        CompletionStage<HttpResponse> responseStage = Http.get(system).singleRequest(request);

        responseStage.thenCompose(response -> Unmarshaller.entityToString().unmarshal(response.entity(), system.executionContext(), system))
                .thenAccept(responseString -> System.out.println("Response for " + operationName + ": " + responseString))
                .exceptionally(ex -> {
            System.err.println("Error during " + operationName + ": " + ex.getMessage());
            return null;
        });
    }
}
