# GraphQL API for a User Registry via Akka HTTP

This project is a simple example of a GraphQL API for a User Registry using Akka HTTP.

Sources in the sample:

* `Main.java` -- contains the main method which bootstraps the application
* `GraphQLServer.java` -- GraphQL API using Akka HTTP
* `UserRegistry.java` -- the actor which handles the user requests

## Running the project

To run the project, execute the following command:

```bash
mvn exec:java -Dexec.mainClass="com.example.graphql.Main"
```

The server will start at `http://localhost:8081/graphql`.

## Querying the API

> I used GraphQL Playground for testing the API. [Github Link](https://github.com/graphql/graphql-playground)
> But you can use any other tool like Postman or Insomnia.

Query all users:

```graphql
query {
  allUsers {
    name
    age
    countryOfResidence
  }
}
```

Query a specific user by its ID:

```graphql
query {
  user(name: "John") {
    name
    age
    countryOfResidence
  }
}
```

## Mutating the API

> I used GraphQL Playground for testing the API. [Github Link](https://github.com/graphql/graphql-playground)
> But you can use any other tool like Postman or Insomnia.
> 
Create a new user:

```graphql
mutation {
    createUser(name: "John", age: 30, countryOfResidence: "USA")
}
```

Delete a user by its ID:

```graphql
mutation {
    deleteUser(name: "John")
}
```