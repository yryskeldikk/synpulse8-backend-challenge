# Synpulse8 Backend Challenge

## Name: Yryskeldi Kurmanbekov

# 1. Introduction

The project is a sophisticated banking application designed to provide users with the ability to manage their financial transactions seamlessly. This documentation provides insights into the architecture, data modeling, API design, security, data access, logging and monitoring, and testing strategies.

# 2. Architecture Overview

## 2.1 High-Level Architecture

- The Transaction App follows a microservices-based architecture.
- Key components include the API service, Kafka event streaming, data storage, and external API integration.

## 2.2 Component Descriptions

- **API Service**: Processes API requests.
- **Kafka Event Streaming**: Handles transaction events and enables real-time data updates.
- **Data Storage**: MySQL database for storing transaction data and user information..
- **External API Integration**: Fetches real-time exchange rates for currency conversion.

## 2.3 Communication Protocols

- RESTful API for user interaction.
- Kafka for real-time event streaming.

## 2.4 Deployment

- The application is containerized using Docker.
- Kubernetes is used for orchestration.

# 3. Data Modeling

## 3.1 Entity-Relationship Diagram

| Entity       | Attributes                                               |
| ------------ | -------------------------------------------------------- |
| user         | user_id, username, email, ...                            |
| transaction  | iban, user_id                                            |
| bank_account | uid, value, currency, iban, description, transactionDate |

- "bank_account" (many-to-one) "user" via "user_id"
- "transaction" (many-to-one) "bank_account" via "iban"

## 3.2 Key Data Entities

- user (emmitted for simplicicty of the project)
- transaction
- bank_account

## 3.3 Data Storage Technologies

- Kafka for event streaming
- MySQL for storage

# 4. API Modeling

# Deployment

## Build the JAR file

```bash
mvn package
```

## 1. Build the Docker Image

Before deploying your application, you need to build a Docker image. Make sure you have the following environment variables defined:

- **DOCKER_REPO** => assigned with the local docker repository IP and Port
- **VERSION** => assigned with the github tag found for this project
- **ENV** => available values: localhost, docker. [ must ensure lower case ]

### Set Environment Variables

```bash
export DOCKER_REPO=???
export VERSION=???
export ENV=???          # [localhost, docker]
```

Build the docker image and make available for local kubernetes to download

### Build and Push the Docker Image

```bash
docker build -t $DOCKER_REPO/synulse-challenge:$VERSION
docker push $DOCKER_REPO/synulse-challenge:$VERSION
```

## 3. Deploy to Kubernetes

### Generate Kubernetes Deployment YAML

Use the provided template to generate the Kubernetes Deployment YAML file. This will replace the environment variables in the template with your actual values.

```bash
envsubst < k8s_deploy.template.yaml > k8s_deploy.yaml
```

### Deploy to Kubernetes

Create the Kubernetes resources:

```bash
kubectl create -f k8s_deploy.yaml
```
