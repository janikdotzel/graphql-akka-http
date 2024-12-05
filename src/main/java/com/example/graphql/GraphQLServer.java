package com.example.graphql;

import akka.actor.typed.Scheduler;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.server.Route;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.*;
import com.example.graphql.dto.User;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static graphql.schema.AsyncDataFetcher.async;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class GraphQLServer extends AllDirectives {
    private final GraphQL graphQL;

    public GraphQLServer(ActorSystem<?> system, ActorRef<UserRegistry.Command> userRegistryActor) {
        // Akka Config
        Scheduler scheduler = system.scheduler();
        Duration askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");

        // Load Schema
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(new File("src/main/resources/schema.graphql"));

        // Runtime Wiring
        RuntimeWiring runtimeWiring = newRuntimeWiring()
            .type("Query", builder -> builder
                .dataFetcher("user", env -> {
                    String name = env.getArgument("name");
                    System.out.println("Querying user " + name);
                    CompletionStage<UserRegistry.GetUserResponse> response = AskPattern.ask(userRegistryActor, ref -> new UserRegistry.GetUser(name, ref), askTimeout, scheduler);
                    return response
                            .thenApply(res -> User.toDTO(res.maybeUser()))
                            .toCompletableFuture();
                })
                .dataFetcher("allUsers", env -> {
                    System.out.println("Querying all users");
                    CompletionStage<UserRegistry.Users> response = AskPattern.ask(userRegistryActor, UserRegistry.GetUsers::new, askTimeout, scheduler);
                    return response
                            .thenApply(res -> User.toDTO(res.users()))
                            .toCompletableFuture();
                }))
            .type("Mutation", builder -> builder
                .dataFetcher("createUser", env -> {
                    String name = env.getArgument("name");
                    int age = env.getArgument("age");
                    String countryOfResidence = env.getArgument("countryOfResidence");
                    System.out.println("Creating user " + name);
                    CompletionStage<UserRegistry.ActionPerformed> response = AskPattern.ask(userRegistryActor, ref -> new UserRegistry.CreateUser(new UserRegistry.User(name, age, countryOfResidence), ref), askTimeout, scheduler);
                    return response
                            .thenApply(res -> true)
                            .toCompletableFuture();
                })
                .dataFetcher("deleteUser", env -> {
                    String name = env.getArgument("name");
                    System.out.println("Deleting user " + name);
                    CompletionStage<UserRegistry.ActionPerformed> response = AskPattern.ask(userRegistryActor, ref -> new UserRegistry.DeleteUser(name, ref), askTimeout, scheduler);
                    return response
                            .thenApply(res -> true)
                            .toCompletableFuture();
                }))
            .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    public Route createRoute() {
        return path("graphql", () ->
            post(() ->
                entity(Jackson.unmarshaller(Map.class), requestBody -> {
                    String query = (String) requestBody.get("query");
                    Map<String, Object> variables = (Map<String, Object>) requestBody.get("variables");

                    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                        .query(query)
                        .variables(variables)
                        .build();

                    ExecutionResult result = graphQL.execute(executionInput);
                    return complete(StatusCodes.OK, result.toSpecification(), Jackson.marshaller());
                })
            )
        );
    }

    public static void startServer(ActorSystem<?> system, ActorRef<UserRegistry.Command> userRegistryActor) {
        GraphQLServer graphQLServer = new GraphQLServer(system, userRegistryActor);

        Http.get(system)
            .newServerAt("localhost", 8081)
            .bind(graphQLServer.createRoute());

        System.out.println("GraphQL server online at graphql://localhost:8081/graphql");
    }
}
