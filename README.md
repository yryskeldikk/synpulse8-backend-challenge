# Synpulse8 Backend Challenge

## Name: Yryskeldi Kurmanbekov

# 1. Introduction

The project is a sophisticated banking application designed to provide users with the ability to manage their financial transactions seamlessly. This documentation provides insights into the architecture, data modeling, API design, data access, deployment, and testing strategies.

# 2. Architecture Overview

### 2.1 High-Level Architecture

- The Transaction App follows a microservices-based architecture.
- Key components include the API service, Kafka event streaming, data storage, and external API integration.

### 2.2 Component Descriptions

- `API Service`: Processes API requests.
- `Kafka Event Streaming`: Handles transaction events and enables real-time data updates.
- `Data Storage`: MySQL database for storing transaction data and user information..
- `External API Integration`: Fetches real-time exchange rates for currency conversion.

### 2.3 Communication Protocols

- RESTful API for user interaction.
- Kafka for real-time event streaming.

### 2.4 Deployment

- The application is containerized using `Docker`.
- `Kubernetes` is used for orchestration.

# 3. Data Modeling

### 3.1 Entity-Relationship Diagram

An Entity-Relationship Diagram (ERD) offers a visual representation of the core entities and their attributes within the system. The chosen data model reflects the design decisions based on the project's requirements and objectives.

| Entity       | Attributes                                               |
| ------------ | -------------------------------------------------------- |
| User         | user_id, username, email, and more...                    |
| Transaction  | iban, user_id, and more...                               |
| Bank Account | uid, value, currency, iban, description, transactionDate |

#### Relationships:

- **User** and **Bank Account** exhibit a many-to-one relationship via the `user_id`. This design choice allows multiple bank accounts to be associated with a single user, offering flexibility and scalability for users with multiple financial accounts.

- **Transaction** and **Bank Account** share a many-to-one relationship via the `iban`. This enables the association of transactions with specific bank accounts, which is crucial for maintaining a transaction history for each account.

### 3.2 Key Data Entities

The system focuses on the following key data entities:

1. **User**: This entity holds information about users, including unique identifiers (`user_id`), usernames, email addresses, and additional user-related details. The design decision to separate user data into its entity promotes modularity and clear user management.

2. **Transaction**: This entity captures details of financial transactions, such as the associated bank account (`iban`), transaction value, and currency. By isolating transaction data, the system ensures transaction-specific information is managed effectively.

3. **Bank Account**: Represents individual bank accounts and contains attributes like `uid` (unique identifier), account value, currency, description, and transaction date. This entity is central to tracking and managing user finances.

### 3.3 Data Storage Technologies

The application strategically incorporates two data storage technologies to fulfill different system requirements:

- **Kafka**: Chosen for event streaming and real-time data processing. Kafka enables seamless communication between system components through asynchronous event-based interactions. This design decision supports real-time updates, event handling, and integration with external systems.

- **MySQL**: Employed for structured data storage and historical data persistence. MySQL ensures the durability and organization of long-term data, including user information and transaction history. This structured database allows for efficient retrieval of historical financial data.

The design choices aim to strike a balance between real-time data processing, scalability, and historical data preservation, ensuring the system meets both present and future needs.

# 4. API Modeling

### Transaction Controller

The `TransactionController` is responsible for handling requests related to user transactions.

### Endpoint

- `GET /transactions/{userId}`

### Request Parameters

- `userId` (Path Variable): The unique identifier of the user for whom transactions are requested.
- `year` (Query Parameter): The year for which transactions are requested.
- `month` (Query Parameter): The month for which transactions are requested.
- `pageable` (Query Parameter): Allows for paginated results.

### Response

- HTTP Status: 200 OK
- Body: A `TransactionDto` object representing the paginated list of transactions for the specified user in the given year and month.

### Cross-Origin Resource Sharing (CORS)

The controller is configured with `@CrossOrigin` to allow cross-origin requests.

### Dependency

The controller relies on the `TransactionService` for retrieving transaction data and constructing the response.

Example Usage:

```http
GET /transactions/P-0123456789?year=2023&month=10&page=2&size=10
```

# 5. Data Access

### JpaRepository

`JpaRepository` is an interface provided by the Spring Data JPA framework that simplifies database access in JPA (Java Persistence API) applications. It provides a set of common methods for basic CRUD operations and supports the creation of custom methods for more complex queries.

### Transaction Repository

- Extends: JpaRepository<Transaction, String>
- Custom Method: findByIbanInAndTransactionDateBetween
- Retrieves transactions for specified user IBANs within a date range.

### Bank Account Repository

- Extends: JpaRepository<BankAccount, String>
- Custom Method: findByUserId
- Retrieves bank accounts associated with a specific user.

# 6. Apache Kafka Integration

### Kafka Consumer Configuration

The Kafka integration in this application is configured through the `KafkaConsumerConfig` class, which is annotated with `@EnableKafka`. It provides settings for consuming Kafka messages and deserializing them.

- **`consumerFactory`**: Defines a factory for creating a consumer of Kafka messages. It specifies configurations like the Kafka server address, group ID, and deserialization methods.

- **`kafkaListenerContainerFactory`**: Creates a factory for concurrent Kafka message listener containers. It uses the `consumerFactory` to configure the listener containers.

- **`transactionConsumerFactory`**: Similar to `consumerFactory`, this factory is tailored for deserializing specific messages of type `Transaction`. It uses the `JsonDeserializer` for handling JSON data.

- **`transactionKafkaListenerFactory`**: Configures the Kafka listener container for consuming `Transaction` messages. It utilizes the `transactionConsumerFactory` for message deserialization.

### Kafka Consumer Service

The `KafkaConsumerService` class contains methods annotated with `@KafkaListener`, indicating which topics and consumer groups should be used for message consumption.

- **`consumeString`**: Listens to the Kafka topic defined in the property `${spring.kafka.server.topic.string}` as a member of the group "string_group." It logs received string messages.

- **`consumeTransaction`**: Listens to the Kafka topic defined in the property `${spring.kafka.server.topic.transaction}` as a member of the group "transaction_group." It deserializes incoming `Transaction` messages, saves them to the database using the `transactionRepository`, and logs relevant information.

# 7. Services

### 7.1 Transaction Service

The `TransactionService` is a key component of the application responsible for managing and processing transaction data. This service interacts with repositories to retrieve data, performs calculations, and communicates with an external API server to convert currency values. Below are the key aspects of the `TransactionService`.

#### Retrieving Transactions for a User in a Specific Month

The method `getTransactionsForUserInMonth` retrieves transactions for a user within a specified month. It involves the following steps:

1. **Retrieve Bank Accounts**: The service first fetches all bank accounts associated with the user by calling the `bankAccountRepository.findByUserId` method.

2. **Collect IBANs**: It then collects the International Bank Account Numbers (IBANs) from the user's bank accounts. This information is essential for identifying relevant transactions.

3. **Date Calculation**: The service calculates the start and end dates for the desired month using the `DateUtils` class. This defines the time frame for which transactions will be retrieved.

4. **Retrieve Transactions**: Transactions are retrieved using the `transactionRepository.findByIbanInAndTransactionDateBetween` method. This method queries the database for transactions matching the user's IBANs and transaction date range.

#### Calculating Total Credit and Debit in HKD (Hong Kong Dollars)

The method `getTransactionDtoForUserInMonth` extends the `getTransactionsForUserInMonth` operation to provide additional information. It calculates the total credit and debit amounts in Hong Kong Dollars (HKD) for the retrieved transactions. The following steps summarize the process:

1. **Retrieve Transactions**: Transactions are obtained using the `getTransactionsForUserInMonth` method, which retrieves transaction data based on the user's criteria.

2. **Currency Conversion**: For each transaction, the service communicates with an external API server using the `externalApiServer` to fetch the current exchange rate in HKD for the transaction's currency.

3. **Calculate Converted Value**: The service calculates the equivalent value of each transaction in HKD using the retrieved exchange rate. This is done by multiplying the transaction's original value by the exchange rate.

4. **Separate Credit and Debit**: Based on the transaction value, it distinguishes between credit and debit transactions and accumulates the amounts for each category.

5. **Set Total Amounts**: The total credit and debit values in HKD are set in the `TransactionDto`.

#### Dependencies

The `TransactionService` relies on the following dependencies:

- `TransactionRepository`: Provides access to transaction data in the database.
- `BankAccountRepository`: Retrieves bank account information for a user.
- `ExternalApiService`: Communicates with an external API server to obtain exchange rates.

This service plays a crucial role in aggregating transaction data and performing currency conversions, enabling users to view their financial data with amounts in HKD.

### 7.2 External API Integration

The `ExternalApiService` is responsible for making external API requests to retrieve exchange rates. It is a key component of the application's functionality. The service is configured to interact with an external API that provides exchange rate data based on a given currency.

- **`getCurrentExchangeRateInHKDFromAPI`**: This method retrieves the current exchange rate for Hong Kong Dollars (HKD) from the external API. It takes a `currency` parameter and returns the exchange rate as a `BigDecimal`. The method makes an HTTP GET request to the API using the `RestTemplate` and processes the response to extract the exchange rate information.

#### Configuration - `AppConfig`

The `AppConfig` class serves as the application's configuration file and includes configuration related to the `RestTemplate`.

- **`@EnableAutoConfiguration`**: This annotation enables Spring Boot's auto-configuration, which simplifies the configuration of the application.

- **`@EnableRetry`**: The `@EnableRetry` annotation enables retry support for methods within the application. It allows for retrying methods marked with the `@Retryable` annotation in case of exceptions.

- **`RestTemplate` Configuration**: The `RestTemplate` bean is configured in the `AppConfig` class using `RestTemplateBuilder`. It specifies connection and read timeouts for HTTP requests made using the `RestTemplate`. These settings control the maximum time allowed for establishing a connection and reading the response from external services.

- **`setConnectTimeout`**: Sets the connection timeout to 5 seconds.

- **`setReadTimeout`**: Sets the read timeout to 10 seconds.

This configuration ensures that HTTP requests made through the `RestTemplate` have appropriate timeouts, and it enables retry support for certain methods, making the application more robust when interacting with external services.

The `ExternalApiService` and `AppConfig` work together to fetch and process external exchange rate data efficiently while handling timeouts and retries.

# 8. Testing

### Controller Testing

In my testing approach for the `TransactionController`, I incorporated the following practices:

#### 1. **Focused Testing with `@WebMvcTest`**

I applied the `@WebMvcTest` annotation, which limited the scope of the test to the controller layer and its associated components. This approach provided focused testing for the controller, resulting in improved test performance and more efficient isolation of the testing target.

#### 2. **Isolating Dependencies with `@MockBean`**

In this test, I isolated the `TransactionService` by using the `@MockBean` annotation to create a mock instance of the service. This practice adheres to the principle of unit testing, which encourages isolating the component under test and providing controlled responses to method calls.

#### 3. **Behavior Definition with `Mockito`**

I employed the `Mockito` framework to define the behavior of the `TransactionService` when it's called with specific parameters. This practice enabled me to simulate different scenarios and ensure that the controller responded correctly to various service responses.

#### 4. **Clear Assertions**

In my test case, I used clear and structured assertions to verify the HTTP request and response. The `perform` method initiated the request, and the `andExpect` method was used to assert the response's status, headers, and content. This approach ensured that the expectations were explicitly documented and validated.

### Service Layer Testing

In the testing of the `TransactionService` class, I followed best practices to ensure the reliability and comprehensiveness of the test suite. These practices encompass unit and integration testing, error cases, and mocking, among others:

#### 1. **Test TransactionDto Calculation**

In the `testTransactionDto` method, I implemented a comprehensive test scenario where I simulated transactions, external API responses, and user data. The test verified the correct calculation of `TransactionDto` attributes such as `totalCreditInHKD`, `totalDebitInHKD`, `totalElements`, and `totalPages`. By providing explicit expectations and using the `comparesEqualTo` method, I ensured precise testing outcomes.

#### 2. **Handling No Transactions**

I included a test method named `testNoTransactions` that focuses on the case when there are no transactions for the user. This test verifies that the service correctly handles this situation by asserting that `TransactionDto` attributes, such as `totalElements`, `totalPages`, and lists, are set to their default values.

#### 3. **Test Method Isolation with `@Spy` and `@Mock`**

I employed `@Spy` and `@Mock` annotations to isolate dependencies and methods in the test cases. This practice enhances the reliability of each test and ensures that method calls are controlled and predictable.

#### 4. **Clear Assertions**

Each test method is structured with clear and concise assertions to verify expected outcomes. This approach enhances test readability and ensures that the tests explicitly document the expected behavior of the service methods.

#### 5. **Use of `ArgumentCaptor` and Mockito's Verification**

In the `testGetTransactionsForUserInMonth` method, I demonstrated the use of `ArgumentCaptor` and Mockito's verification methods. These practices are invaluable for capturing and verifying method arguments and invocations, allowing for fine-grained testing of the service logic.

# 9. Environment-Specific Configuration Files

In the application, we utilize Spring Boot's property file system to manage environment-specific configurations. This modular approach allows us to customize the application's behavior for various deployment environments without changing the core application code.

#### 1. **`application.properties`**

- The primary configuration file, `application.properties`, contains the default configuration settings for the application. These properties are applicable in all deployment environments, serving as a baseline.

#### 2. **`application-localhost.properties`**

- The `application-localhost.properties` file contains configurations that are specific to a local development environment. Developers use this file during local development to customize settings that are suitable for their development machines.

- This allows for flexibility in configuring, for example, the database connection, enabling debug logging, and setting development-specific properties.

#### 3. **`application-docker.properties`**

- The `application-docker.properties` file is tailored for environments where the application is running within a Docker container. It includes configurations that are optimized for containerized deployments.

- These configurations might include the use of container-specific networking settings or environment variables.

### **Profiles Activation**

We activate these profiles using the `spring.profiles.active` property in the `application.properties` file, specifying the relevant profile based on the deployment environment. For instance:

```properties
spring.profiles.active=development
```

# 10. Deployment

### Dockerfile

The Dockerfile is used to build a Docker image for the Spring Boot application.The Kubernetes deployment file defines how the application is deployed within a Kubernetes cluster.

### Build the JAR file

```bash
mvn package
```

### 1. Build the Docker Image

Before deploying your application, you need to build a Docker image. Make sure you have the following environment variables defined:

- **DOCKER_REPO** => assigned with the local docker repository IP and Port
- **VERSION** => assigned with the github tag found for this project
- **ENV** => available values: localhost, docker. [ must ensure lower case ]

#### Set Environment Variables

```bash
export DOCKER_REPO=???
export VERSION=???
export ENV=???          # [localhost, docker]
```

Build the docker image and make available for local kubernetes to download

#### Build and Push the Docker Image

```bash
docker build -t $DOCKER_REPO/synulse-challenge:$VERSION
docker push $DOCKER_REPO/synulse-challenge:$VERSION
```

### 2. Deploy to Kubernetes

#### Generate Kubernetes Deployment YAML

Use the provided template to generate the Kubernetes Deployment YAML file. This will replace the environment variables in the template with your actual values.

```bash
envsubst < k8s_deploy.template.yaml > k8s_deploy.yaml
```

#### Deploy to Kubernetes

Create the Kubernetes resources:

```bash
kubectl create -f k8s_deploy.yaml
```
