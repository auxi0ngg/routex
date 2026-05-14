# RouteX — Smart Logistics & Route Optimization Platform

> Production-grade distributed logistics platform built with Java 21, Spring Boot 3, React, Kafka, Redis, PostgreSQL

## Architecture Overview

RouteX is a cloud-native microservices platform for end-to-end logistics operations — shipment booking, real-time GPS tracking, route optimization, fleet management, warehouse operations, and analytics.

## Services

| Service | Port | Description |
|---|---|---|
| API Gateway | 8080 | Spring Cloud Gateway, JWT validation, rate limiting |
| Auth Service | 8081 | JWT, refresh tokens, OAuth2, RBAC |
| User Service | 8082 | Users, organizations, multi-tenancy |
| Shipment Service | 8083 | Booking, status transitions, proof of delivery |
| Fleet Service | 8084 | Vehicles, maintenance, utilization |
| Driver Service | 8085 | Driver profiles, assignments, earnings |
| Route Optimization | 8086 | Dijkstra/A*, multi-stop, ETA prediction |
| Warehouse Service | 8087 | Inventory, scanning, storage allocation |
| Tracking Service | 8088 | GPS streaming, geofencing, history |
| Notification Service | 8089 | Email, SMS, push notifications |
| Analytics Service | 8090 | KPIs, dashboards, SLA monitoring |
| WebSocket Gateway | 8091 | Real-time updates, live tracking feeds |

## Quick Start

```bash
git clone https://github.com/auxi0ngg/routex.git
cd routex
docker-compose up -d
```

Frontend: http://localhost:3000  
API Gateway: http://localhost:8080  
Grafana: http://localhost:3001  
Kafka UI: http://localhost:8092  

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.2, Spring Cloud, Spring Security
- **Messaging**: Apache Kafka
- **Cache**: Redis
- **Database**: PostgreSQL (per-service schemas)
- **Frontend**: React 18, TypeScript, TailwindCSS, Redux Toolkit
- **Maps**: Leaflet + OpenStreetMap
- **DevOps**: Docker, Kubernetes, Helm, GitHub Actions
- **Monitoring**: Prometheus, Grafana, Loki, ELK Stack
