# GraphQL API for a User Registry via Akka HTTP

This project is a simple example of a GraphQL API for a User Registry using Akka HTTP.

## Running the project

To run the project, execute the following command:

```bash
mvn exec:java -Dexec.mainClass="com.example.graphql.Main"
```

The server will start at `http://localhost:8081/graphql`.

## Querying the API

You can use the following query to get all users:

```graphql
query {
  allUsers {
    name
    age
    countryOfResidence
  }
}
```

You can also use the following query to get a specific user by its ID:

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

You can use the following mutation to create a new user:

```graphql
mutation {
  createUser(name: "John", age: 30, countryOfResidence: "USA") {
    name
    age
    countryOfResidence
  }
}
```

You can also use the following mutation to delete a user by its ID:

```graphql
mutation {
  deleteUser(name: "John")
}
```