const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, BorderStyle, WidthType, ShadingType,
  LevelFormat, PageNumber, Footer, Header, TabStopType, TabStopPosition
} = require('docx');
const fs = require('fs');

// ─── Helpers ─────────────────────────────────────────────────────────────────
const h1 = (text) => new Paragraph({
  heading: HeadingLevel.HEADING_1,
  children: [new TextRun({ text, bold: true, size: 32, color: '1D4ED8' })],
  spacing: { before: 360, after: 180 }
});

const h2 = (text) => new Paragraph({
  heading: HeadingLevel.HEADING_2,
  children: [new TextRun({ text, bold: true, size: 26, color: '1E3A8A' })],
  spacing: { before: 280, after: 140 }
});

const h3 = (text) => new Paragraph({
  heading: HeadingLevel.HEADING_3,
  children: [new TextRun({ text, bold: true, size: 24, color: '1E40AF' })],
  spacing: { before: 200, after: 100 }
});

const para = (text, opts = {}) => new Paragraph({
  children: [new TextRun({ text, size: 22, color: '1E293B', ...opts })],
  spacing: { before: 80, after: 80 }
});

const bold = (text) => new TextRun({ text, bold: true, size: 22 });
const code = (text) => new TextRun({ text, font: 'Courier New', size: 20, color: '7C3AED', bold: true });

const bullet = (text, level = 0) => new Paragraph({
  numbering: { reference: 'bullets', level },
  children: [new TextRun({ text, size: 22, color: '1E293B' })],
  spacing: { before: 40, after: 40 }
});

const numbered = (text, level = 0) => new Paragraph({
  numbering: { reference: 'numbers', level },
  children: [new TextRun({ text, size: 22 })],
  spacing: { before: 40, after: 40 }
});

const divider = () => new Paragraph({
  border: { bottom: { style: BorderStyle.SINGLE, size: 6, color: '3B82F6', space: 1 } },
  spacing: { before: 200, after: 200 }
});

const codeBlock = (text) => new Paragraph({
  children: [new TextRun({ text, font: 'Courier New', size: 18, color: '7C3AED' })],
  shading: { fill: 'F1F5F9', type: ShadingType.CLEAR },
  spacing: { before: 40, after: 40 },
  indent: { left: 360 }
});

const tableRow = (cells, isHeader = false) => new TableRow({
  children: cells.map(cell => new TableCell({
    borders: {
      top: { style: BorderStyle.SINGLE, size: 1, color: 'CBD5E1' },
      bottom: { style: BorderStyle.SINGLE, size: 1, color: 'CBD5E1' },
      left: { style: BorderStyle.SINGLE, size: 1, color: 'CBD5E1' },
      right: { style: BorderStyle.SINGLE, size: 1, color: 'CBD5E1' },
    },
    shading: isHeader ? { fill: '1D4ED8', type: ShadingType.CLEAR } : { fill: 'F8FAFC', type: ShadingType.CLEAR },
    margins: { top: 80, bottom: 80, left: 120, right: 120 },
    children: [new Paragraph({
      children: [new TextRun({
        text: cell, size: 20,
        bold: isHeader, color: isHeader ? 'FFFFFF' : '1E293B'
      })]
    })]
  }))
});

const dataTable = (headers, rows, widths) => new Table({
  width: { size: 9360, type: WidthType.DXA },
  columnWidths: widths,
  rows: [
    tableRow(headers, true),
    ...rows.map(r => tableRow(r, false))
  ]
});

// ─── Document Content ─────────────────────────────────────────────────────────
const doc = new Document({
  numbering: {
    config: [
      {
        reference: 'bullets',
        levels: [
          { level: 0, format: LevelFormat.BULLET, text: '\u2022', alignment: AlignmentType.LEFT,
            style: { paragraph: { indent: { left: 720, hanging: 360 } } } },
          { level: 1, format: LevelFormat.BULLET, text: '\u25E6', alignment: AlignmentType.LEFT,
            style: { paragraph: { indent: { left: 1080, hanging: 360 } } } }
        ]
      },
      {
        reference: 'numbers',
        levels: [{
          level: 0, format: LevelFormat.DECIMAL, text: '%1.', alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } }
        }]
      }
    ]
  },
  styles: {
    default: { document: { run: { font: 'Calibri', size: 22 } } },
    paragraphStyles: [
      {
        id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal',
        run: { size: 32, bold: true, font: 'Calibri', color: '1D4ED8' },
        paragraph: { spacing: { before: 360, after: 180 }, outlineLevel: 0 }
      },
      {
        id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal',
        run: { size: 26, bold: true, font: 'Calibri', color: '1E3A8A' },
        paragraph: { spacing: { before: 280, after: 140 }, outlineLevel: 1 }
      },
      {
        id: 'Heading3', name: 'Heading 3', basedOn: 'Normal', next: 'Normal',
        run: { size: 24, bold: true, font: 'Calibri', color: '1E40AF' },
        paragraph: { spacing: { before: 200, after: 100 }, outlineLevel: 2 }
      }
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 12240, height: 15840 },
        margin: { top: 1440, right: 1200, bottom: 1440, left: 1200 }
      }
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          children: [
            new TextRun({ text: 'RouteX — Smart Logistics Platform  |  Technical Documentation', size: 18, color: '64748B' })
          ],
          border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: '3B82F6', space: 1 } }
        })]
      })
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          children: [
            new TextRun({ text: 'Confidential | RouteX Platform Documentation  |  Page ', size: 18, color: '94A3B8' }),
            new TextRun({ children: [PageNumber.CURRENT], size: 18, color: '3B82F6' })
          ],
          alignment: AlignmentType.CENTER
        })]
      })
    },
    children: [

      // ═══════════════════════════════════════════════════════════════════════
      // TITLE PAGE
      // ═══════════════════════════════════════════════════════════════════════
      new Paragraph({
        children: [new TextRun({ text: 'RouteX', bold: true, size: 72, color: '1D4ED8', font: 'Calibri' })],
        alignment: AlignmentType.CENTER,
        spacing: { before: 1440, after: 120 }
      }),
      new Paragraph({
        children: [new TextRun({ text: 'Smart Logistics & Route Optimization Platform', size: 36, color: '475569', font: 'Calibri' })],
        alignment: AlignmentType.CENTER,
        spacing: { before: 0, after: 120 }
      }),
      new Paragraph({
        children: [new TextRun({ text: 'Complete Technical Documentation & Interview Preparation Guide', size: 24, color: '94A3B8', italics: true })],
        alignment: AlignmentType.CENTER,
        spacing: { before: 0, after: 800 }
      }),
      divider(),

      dataTable(
        ['Attribute', 'Value'],
        [
          ['Architecture', 'Cloud-Native Microservices (12 Services)'],
          ['Backend', 'Java 21, Spring Boot 3.2, Spring Cloud'],
          ['Frontend', 'React 18, TypeScript, TailwindCSS, Redux Toolkit'],
          ['Messaging', 'Apache Kafka (9 topics, DLQ, idempotent consumers)'],
          ['Cache / State', 'Redis (live tracking, sessions, rate limiting)'],
          ['Databases', 'PostgreSQL 16 (Database-per-Service pattern)'],
          ['DevOps', 'Docker, Kubernetes, Helm, GitHub Actions CI/CD'],
          ['Monitoring', 'Prometheus, Grafana, Loki, ELK Stack'],
          ['Maps', 'Leaflet + OpenStreetMap, custom Dijkstra/A* engine'],
          ['Security', 'JWT, Refresh Tokens, RBAC, Redis Blacklist, BCrypt'],
        ],
        [2800, 6560]
      ),

      new Paragraph({ spacing: { before: 800 } }),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 1: PROJECT OVERVIEW
      // ═══════════════════════════════════════════════════════════════════════
      h1('1. Project Overview'),
      divider(),

      para('RouteX is a production-grade, distributed logistics platform that replicates the core capabilities of industry leaders like Delhivery, Porter, Shiprocket, and Amazon Logistics. It is built as a cloud-native microservices system using Java 21 and Spring Boot 3, with a React TypeScript frontend, Apache Kafka for event streaming, Redis for real-time state, and PostgreSQL for persistence.'),

      h2('1.1 What This Project Demonstrates'),
      bullet('Distributed Systems Design: 12 independently deployable microservices communicating through Kafka events and REST APIs'),
      bullet('Real-Time Engineering: WebSocket-based live GPS tracking using STOMP over SockJS with Redis pub/sub'),
      bullet('Algorithmic Engineering: Custom route optimization engine implementing Dijkstra, A*, and 2-opt local search'),
      bullet('Enterprise Security: JWT access tokens, refresh token rotation, Redis blacklisting, BCrypt, RBAC with 6 roles'),
      bullet('Event-Driven Architecture: Kafka producers/consumers with DLQ, idempotent processing, and retry mechanisms'),
      bullet('DevOps Maturity: Full Docker Compose local stack, Kubernetes manifests with HPA, GitHub Actions CI/CD pipeline'),
      bullet('Observability: Prometheus metrics, Grafana dashboards, Loki log aggregation, distributed tracing'),

      h2('1.2 Business Capabilities'),
      para('RouteX handles the complete logistics lifecycle:'),
      numbered('Customer creates a shipment with pickup and delivery addresses'),
      numbered('System assigns the nearest available driver using the optimization engine'),
      numbered('Driver picks up the package — status transitions trigger Kafka events'),
      numbered('Fleet manager monitors all vehicles on a live map via WebSocket updates'),
      numbered('Customer receives automated notifications (email/SMS/push) at each status change'),
      numbered('ETA predictions are computed in real-time using distance and traffic factors'),
      numbered('Analytics aggregates all events for KPI dashboards: success rate, delivery time, fleet utilization'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 2: ARCHITECTURE
      // ═══════════════════════════════════════════════════════════════════════
      h1('2. System Architecture'),
      divider(),

      h2('2.1 High-Level Architecture'),
      para('RouteX follows a Microservices Architecture where each service owns its domain, database, and deployment lifecycle. All external traffic enters through the API Gateway, which handles JWT validation, rate limiting, and routing.'),

      para('Architecture layers (outside-in):'),
      bullet('Client Layer: React SPA (browser), Mobile (future), Driver App (future)'),
      bullet('Edge Layer: NGINX reverse proxy for TLS termination, rate limiting, static assets'),
      bullet('Gateway Layer: Spring Cloud Gateway with JWT validation filter, circuit breakers, Redis rate limiting'),
      bullet('Service Layer: 10 domain microservices, each with its own PostgreSQL database'),
      bullet('Async Layer: Apache Kafka for event-driven communication between services'),
      bullet('State Layer: Redis for live tracking data, session management, and distributed caching'),
      bullet('Observability Layer: Prometheus + Grafana + Loki for metrics, dashboards, and log aggregation'),

      h2('2.2 Microservices Inventory'),

      dataTable(
        ['Service', 'Port', 'Responsibility', 'DB'],
        [
          ['API Gateway', '8080', 'JWT validation, routing, rate limiting, circuit breaker', 'Redis only'],
          ['Auth Service', '8081', 'Registration, login, JWT issuance, refresh, RBAC', 'routex_auth'],
          ['User Service', '8082', 'User profiles, organization management, multi-tenancy', 'routex_user'],
          ['Shipment Service', '8083', 'Shipment lifecycle, status transitions, proof of delivery', 'routex_shipment'],
          ['Fleet Service', '8084', 'Vehicle registration, maintenance logs, utilization stats', 'routex_fleet'],
          ['Driver Service', '8085', 'Driver profiles, assignments, ratings, earnings', 'routex_driver'],
          ['Route Optimization', '8086', 'Dijkstra/A*, multi-stop optimization, ETA prediction', 'Stateless + Redis'],
          ['Warehouse Service', '8087', 'Inventory, scanning, storage allocation', 'routex_warehouse'],
          ['Tracking Service', '8088', 'GPS streaming, WebSocket, geofencing, route history', 'routex_tracking'],
          ['Notification Service', '8089', 'Email, SMS, push notifications via Kafka consumer', 'routex_notification'],
          ['Analytics Service', '8090', 'KPI aggregation, dashboard data, SLA monitoring', 'routex_analytics'],
          ['WebSocket Gateway', '8091', 'STOMP WebSocket broker, real-time broadcast', 'Redis pub/sub'],
        ],
        [1800, 720, 3600, 1440]
      ),

      h2('2.3 Communication Patterns'),
      bullet('Synchronous (REST): Used for reads, user-facing operations where immediate response is required. Routed through the API Gateway.'),
      bullet('Asynchronous (Kafka): Used for state changes that fan out to multiple consumers — shipment status changes notify tracking, analytics, and notification services simultaneously.'),
      bullet('Real-Time (WebSocket): Driver GPS coordinates are pushed to the Tracking Service via REST, stored in Redis, and broadcast to dashboard subscribers via STOMP/WebSocket.'),
      bullet('Internal (Redis): The Route Optimization Service reads live driver locations from Redis to compute nearest-driver assignments without a database round-trip.'),

      h2('2.4 Database-per-Service Pattern'),
      para('Each service has its own PostgreSQL database with independent Flyway migrations. This is a core microservices principle that ensures:'),
      bullet('Services can evolve their schema independently without coordinating with other teams'),
      bullet('A schema change in the Shipment Service cannot break the Fleet Service'),
      bullet('Each service can choose the appropriate database engine for its needs'),
      bullet('Circuit breakers can isolate database failures to individual services'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 3: KEY TECHNICAL DECISIONS
      // ═══════════════════════════════════════════════════════════════════════
      h1('3. Key Technical Decisions & Tradeoffs'),
      divider(),

      h2('3.1 Why Kafka Over REST for Service Communication?'),
      para('Shipment status updates need to trigger actions in 3+ services: Tracking (update live map), Analytics (record metrics), Notification (send SMS/email), and potentially more. If we used synchronous REST calls:'),
      bullet('The Shipment Service would need to know about all downstream services — tight coupling'),
      bullet('A single slow consumer (e.g., Notification) would block the entire status update'),
      bullet('Adding a new consumer (e.g., Billing) would require modifying the Shipment Service'),

      para('With Kafka:'),
      bullet('Shipment Service publishes one event to a topic and is done — loose coupling'),
      bullet('Each consumer processes independently at its own pace — no blocking'),
      bullet('New consumers can subscribe without any changes to the producer'),
      bullet('Failed events go to the Dead Letter Queue (DLT) for manual inspection'),

      h2('3.2 Why Redis for Driver Locations?'),
      para('Driver GPS coordinates update every 3-5 seconds per driver. At 1000 drivers, that is 200-333 writes per second. Storing this in PostgreSQL would be expensive and create hot spots on the tracking table. Redis provides:'),
      bullet('Sub-millisecond read latency for nearest-driver queries'),
      bullet('Automatic TTL (24 hours) to clean up stale locations without a background job'),
      bullet('Key-based access pattern (driver:location:{driverId}) that matches Redis\'s strengths'),
      bullet('Pub/Sub capability for broadcasting updates to WebSocket clients'),

      h2('3.3 JWT vs Session Tokens'),
      para('RouteX uses JWT (stateless) with Redis-backed refresh token storage. This gives us:'),
      bullet('Stateless access tokens — the API Gateway can validate JWTs without a database call, enabling horizontal scaling'),
      bullet('Short-lived access tokens (15 minutes) reduce the window for token theft'),
      bullet('Refresh token rotation — each use issues a new refresh token, invalidating the old one'),
      bullet('Redis blacklist for immediate revocation on logout — the main downside of stateless JWTs is addressed'),
      bullet('JTI (JWT ID) stored in the blacklist so we can identify individual tokens, not just users'),

      h2('3.4 Route Optimization Algorithm Choice'),
      para('For multi-stop delivery optimization, we use the Nearest Neighbor heuristic combined with 2-opt local search improvement:'),
      bullet('Nearest Neighbor: O(n\u00B2) greedy construction — fast and produces good initial solutions for n < 50 stops (typical delivery batch)'),
      bullet('2-opt Improvement: Iteratively reverses route segments to reduce total distance — typically improves solutions by 5-15%'),
      bullet('A* for point-to-point: Used when a specific path through waypoints is needed, with haversine distance as the admissible heuristic'),
      para('We chose these over exact algorithms (e.g., Held-Karp at O(2\u207F n\u00B2)) because delivery batches rarely exceed 30-40 stops, and the heuristic approaches give near-optimal solutions in polynomial time.'),

      h2('3.5 Why Spring Cloud Gateway Over Netflix Zuul?'),
      bullet('Reactive/Non-Blocking: Spring Cloud Gateway is built on WebFlux and Netty, handling thousands of concurrent connections with a small thread pool'),
      bullet('First-Class Spring Integration: Built-in Redis rate limiting, circuit breakers, route predicates'),
      bullet('Actively Maintained: Zuul 1.x is in maintenance mode; Spring Cloud Gateway is the modern replacement'),
      bullet('WebSocket Support: Handles WebSocket proxying natively for the tracking service'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 4: API DESIGN
      // ═══════════════════════════════════════════════════════════════════════
      h1('4. API Design'),
      divider(),

      h2('4.1 Auth Service APIs'),
      dataTable(
        ['Method', 'Endpoint', 'Auth', 'Description'],
        [
          ['POST', '/api/v1/auth/register', 'Public', 'Register new user, returns JWT tokens'],
          ['POST', '/api/v1/auth/login', 'Public', 'Login with email/password, returns JWT tokens'],
          ['POST', '/api/v1/auth/refresh', 'Public', 'Refresh access token using refresh token'],
          ['POST', '/api/v1/auth/logout', 'Bearer', 'Logout — blacklists access token in Redis'],
          ['GET', '/api/v1/auth/validate', 'Bearer', 'Validate JWT (used by API Gateway)'],
          ['POST', '/api/v1/auth/change-password', 'Bearer', 'Change password, revokes all refresh tokens'],
        ],
        [900, 2400, 900, 3960]
      ),

      h2('4.2 Shipment Service APIs'),
      dataTable(
        ['Method', 'Endpoint', 'Auth', 'Description'],
        [
          ['POST', '/api/v1/shipments', 'CUSTOMER+', 'Create new shipment'],
          ['GET', '/api/v1/shipments', 'COMPANY_ADMIN+', 'List org shipments (paginated, filterable by status)'],
          ['GET', '/api/v1/shipments/track/{number}', 'Public', 'Track shipment by tracking number'],
          ['PATCH', '/api/v1/shipments/{id}/status', 'DRIVER+', 'Update shipment status with optional location proof'],
          ['POST', '/api/v1/shipments/{id}/assign', 'FLEET_MANAGER+', 'Assign driver and vehicle to shipment'],
          ['GET', '/api/v1/shipments/driver/{id}/active', 'DRIVER+', 'Get driver\'s active assignments'],
        ],
        [900, 2800, 1400, 3060]
      ),

      h2('4.3 Shipment Status State Machine'),
      para('Status transitions are strictly validated — the service enforces valid transitions and throws InvalidStatusTransitionException for illegal ones:'),
      dataTable(
        ['From', 'To (allowed)', 'Who', 'Action'],
        [
          ['CREATED', 'ASSIGNED or CANCELLED', 'Fleet Manager / System', 'Driver assignment'],
          ['ASSIGNED', 'PICKED_UP or CANCELLED', 'Driver', 'Driver picks up package'],
          ['PICKED_UP', 'IN_TRANSIT', 'System / Driver', 'Vehicle starts moving'],
          ['IN_TRANSIT', 'OUT_FOR_DELIVERY', 'Driver', 'Last-mile delivery begins'],
          ['OUT_FOR_DELIVERY', 'DELIVERED or FAILED', 'Driver', 'Delivery attempt made'],
          ['FAILED', 'RETURNED or ASSIGNED', 'Fleet Manager', 'Re-attempt or return'],
        ],
        [1800, 2400, 1800, 2400]
      ),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 5: KAFKA ARCHITECTURE
      // ═══════════════════════════════════════════════════════════════════════
      h1('5. Kafka Event Architecture'),
      divider(),

      h2('5.1 Topics and Partitioning'),
      dataTable(
        ['Topic', 'Partitions', 'Producer', 'Consumers', 'Purpose'],
        [
          ['shipment-created', '3', 'Shipment Svc', 'Route Svc, Analytics Svc', 'Trigger route optimization and volume metrics'],
          ['shipment-assigned', '3', 'Shipment Svc', 'Driver Svc, Notification Svc', 'Notify driver of new assignment'],
          ['driver-location-updated', '6', 'Tracking Svc', 'Analytics Svc', 'Driver GPS coordinates — high volume'],
          ['tracking-events', '6', 'Shipment Svc', 'Notification Svc, Analytics Svc', 'Shipment status changes for notifications'],
          ['notification-events', '3', 'Multiple', 'Notification Svc', 'All notification triggers consolidated'],
          ['analytics-events', '3', 'Multiple', 'Analytics Svc', 'Business events for KPI aggregation'],
          ['fleet-events', '2', 'Fleet Svc', 'Driver Svc, Analytics Svc', 'Vehicle status changes'],
          ['analytics-events.DLT', '1', 'System', 'Ops team', 'Dead Letter Queue for failed analytics events'],
        ],
        [2000, 900, 1400, 1800, 2860]
      ),

      h2('5.2 Idempotent Consumer Design'),
      para('All Kafka consumers are designed for at-least-once delivery with idempotent processing:'),
      bullet('The AnalyticsEventConsumer manually acknowledges messages (AckMode.MANUAL)'),
      bullet('Failed messages are not acknowledged, triggering retry based on backoff configuration'),
      bullet('After max retries, Kafka routes to the Dead Letter Topic (.DLT suffix)'),
      bullet('A separate DLT consumer logs failed events for ops review and replay'),
      bullet('Consumers use database upserts or idempotency keys to prevent duplicate processing'),

      h2('5.3 Kafka Producer Configuration'),
      para('Producers are configured for exactly-once semantics within Kafka:'),
      codeBlock('enable.idempotence=true'),
      codeBlock('acks=all'),
      codeBlock('retries=3'),
      codeBlock('max.in.flight.requests.per.connection=5'),
      para('This ensures that even if the producer retries due to a transient network failure, the broker will deduplicate the message using the producer\'s sequence number.'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 6: SECURITY
      // ═══════════════════════════════════════════════════════════════════════
      h1('6. Security Architecture'),
      divider(),

      h2('6.1 Authentication Flow'),
      numbered('Client sends email + password to POST /api/v1/auth/login'),
      numbered('Auth Service looks up the user by email, verifies BCrypt hash (cost factor 12)'),
      numbered('On success, generates a signed JWT access token (15-minute TTL) and a refresh token (7-day TTL)'),
      numbered('JWT contains: userId (sub), email, role, organizationId, JTI (unique ID), issued-at, expiry'),
      numbered('Refresh token is stored in PostgreSQL (hashed) — enables revocation'),
      numbered('Client stores tokens; attaches Bearer {accessToken} to subsequent requests'),
      numbered('API Gateway validates JWT using the Auth Service validate endpoint before routing'),

      h2('6.2 RBAC — Role-Based Access Control'),
      dataTable(
        ['Role', 'Capabilities'],
        [
          ['SUPER_ADMIN', 'Full platform access — manage all organizations, users, and system configuration'],
          ['COMPANY_ADMIN', 'Manage own organization: users, vehicles, shipments, analytics, billing'],
          ['FLEET_MANAGER', 'Manage vehicles, assign drivers, view fleet analytics and routes'],
          ['WAREHOUSE_MANAGER', 'Manage inventory, scan packages, allocate storage, print labels'],
          ['DRIVER', 'View own assignments, update shipment status, submit proof of delivery'],
          ['CUSTOMER', 'Create shipments, track own shipments, view delivery history'],
        ],
        [2400, 6960]
      ),

      h2('6.3 Token Blacklisting'),
      para('When a user logs out, the access token\'s JTI is stored in Redis with a TTL equal to the token\'s remaining lifetime. The API Gateway checks the blacklist on every request:'),
      codeBlock('Key: blacklist:{jti}'),
      codeBlock('Value: "revoked"'),
      codeBlock('TTL: token expiry - current time'),
      para('This ensures that even a stolen access token cannot be used after logout. The Redis TTL automatically cleans up expired entries.'),

      h2('6.4 Refresh Token Rotation'),
      para('Refresh tokens follow a rotation strategy to detect theft:'),
      bullet('Each use of a refresh token generates a new access token AND a new refresh token'),
      bullet('The old refresh token is immediately revoked in the database'),
      bullet('If a stolen refresh token is used after rotation, the legitimate user\'s subsequent refresh will fail, signaling a breach'),
      bullet('All refresh tokens for a user are revoked on password change'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 7: REAL-TIME TRACKING
      // ═══════════════════════════════════════════════════════════════════════
      h1('7. Real-Time Tracking System'),
      divider(),

      h2('7.1 GPS Data Flow'),
      numbered('Driver mobile app sends GPS coordinates to POST /api/v1/tracking/drivers/{id}/location every 5 seconds'),
      numbered('Tracking Service stores latest location in Redis with 24-hour TTL: driver:location:{driverId}'),
      numbered('Same data is persisted to PostgreSQL for historical route playback'),
      numbered('Tracking Service broadcasts to WebSocket topic /topic/driver/{driverId}'),
      numbered('Dashboard subscribers receive real-time updates and move the vehicle marker on the Leaflet map'),
      numbered('If driver has an active shipment, also updates shipment:location:{shipmentId} in Redis'),
      numbered('WebSocket subscribers of /topic/shipment/{id} receive customer-facing tracking updates'),

      h2('7.2 WebSocket Technology Stack'),
      bullet('Protocol: STOMP (Simple Text Oriented Messaging Protocol) over SockJS'),
      bullet('Fallback: SockJS provides WebSocket fallback for environments that don\'t support it (HTTP long polling, EventSource)'),
      bullet('Spring: WebSocketMessageBrokerConfigurer with in-memory SimpleBroker and STOMP endpoint at /ws/tracking'),
      bullet('Frontend: @stomp/stompjs v7 + sockjs-client — auto-reconnects on disconnect with 5-second delay'),
      bullet('Heartbeat: 4-second heartbeat in both directions to detect stale connections'),

      h2('7.3 Scaling Real-Time at Scale'),
      para('In production (thousands of concurrent drivers), the in-memory SimpleBroker is replaced with a STOMP broker relay backed by RabbitMQ or Redis Streams:'),
      bullet('Multiple Tracking Service instances publish to Redis Pub/Sub'),
      bullet('WebSocket Gateway instances subscribe to Redis channels and push to connected clients'),
      bullet('This allows horizontal scaling of both producers and consumers independently'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 8: DEVOPS & DEPLOYMENT
      // ═══════════════════════════════════════════════════════════════════════
      h1('8. DevOps & Deployment'),
      divider(),

      h2('8.1 Local Development — Docker Compose'),
      para('docker-compose.yml starts the complete platform with a single command:'),
      codeBlock('docker-compose up -d'),
      bullet('12 Spring Boot microservices'),
      bullet('8 separate PostgreSQL databases (Database-per-Service)'),
      bullet('Redis 7.2 with AOF persistence'),
      bullet('Kafka + Zookeeper with automatic topic creation'),
      bullet('Kafka UI for topic/message inspection'),
      bullet('React frontend (production build via NGINX)'),
      bullet('NGINX reverse proxy with rate limiting'),
      bullet('Prometheus + Grafana + Loki monitoring stack'),

      h2('8.2 Kubernetes Deployment'),
      para('Kubernetes manifests provide production-grade deployment:'),
      bullet('Namespace isolation: all resources in the routex namespace'),
      bullet('ConfigMaps for non-sensitive configuration (service URLs, Kafka servers)'),
      bullet('Secrets for sensitive data (DB passwords, JWT secret) — use HashiCorp Vault in production'),
      bullet('StatefulSets for Kafka, Zookeeper, and Redis to preserve stable network identity'),
      bullet('Deployments for all application services with RollingUpdate strategy (zero-downtime)'),
      bullet('HorizontalPodAutoscaler (HPA) on Auth Service: scale 2-10 replicas based on CPU (70%) and memory (80%)'),
      bullet('Pod Anti-Affinity rules: Auth Service replicas scheduled on different nodes for HA'),
      bullet('Ingress with TLS termination via cert-manager and Let\'s Encrypt'),
      bullet('NGINX Ingress Controller with WebSocket support for tracking'),

      h2('8.3 CI/CD Pipeline — GitHub Actions'),
      para('The pipeline has 5 stages:'),
      numbered('Code Quality: Checkstyle (Java) + ESLint (TypeScript) on every push'),
      numbered('Backend Tests: Unit + integration tests with Testcontainers (real PostgreSQL, Redis, Kafka)'),
      numbered('Frontend Tests: TypeScript compile check + Vite production build'),
      numbered('Build & Push: Maven package + Docker multi-platform builds (amd64, arm64) pushed to GitHub Container Registry'),
      numbered('Deploy: kubectl set image for rolling update on Kubernetes, with automatic rollback on failure'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 9: INTERVIEW Q&A
      // ═══════════════════════════════════════════════════════════════════════
      h1('9. Interview Preparation — Expected Questions & Answers'),
      divider(),

      h2('9.1 System Design Questions'),

      h3('Q: How would you handle 10,000 concurrent driver location updates per second?'),
      para('A: Current architecture uses Redis for live state. For 10K/s we would:'),
      bullet('Partition the driver-location-updated Kafka topic to 20+ partitions (currently 6)'),
      bullet('Deploy 20+ Tracking Service consumers — one per partition for maximum parallelism'),
      bullet('Use Redis pipelining to batch Redis writes — reduces RTT overhead'),
      bullet('Switch from per-driver Redis keys to Redis Sorted Sets keyed by geography (GeoSet) for efficient "nearest driver" queries using GEORADIUS'),
      bullet('For WebSocket broadcasts, use Redis Pub/Sub with a dedicated WebSocket Gateway cluster'),
      bullet('PostgreSQL writes are async — use a separate consumer to batch-write tracking history without blocking the GPS pipeline'),

      h3('Q: Your Shipment Service went down — what happens to shipment events?'),
      para('A: Kafka provides durability guarantees:'),
      bullet('Producers to Kafka: if the Shipment Service is down, the producer cannot send. API Gateway\'s circuit breaker would open and return 503 quickly, avoiding thread pool exhaustion.'),
      bullet('Consumers from Kafka: if downstream consumers (Analytics, Notification) are down, Kafka retains the messages. Consumer groups track offset, so when the service recovers, it resumes from where it left off.'),
      bullet('Database: PostgreSQL is separate — even if the service pod crashes, data written before the crash is safe.'),
      bullet('In-flight requests: Kubernetes terminationGracePeriodSeconds=30 gives the service 30 seconds to finish processing in-flight requests before SIGKILL.'),

      h3('Q: How do you prevent double-processing of Kafka messages?'),
      para('A: Multiple layers of protection:'),
      bullet('Kafka producer idempotence (enable.idempotence=true) prevents duplicates within a single producer session'),
      bullet('Consumers use MANUAL acknowledgement — only ack after successful processing'),
      bullet('Database operations use UPSERT (INSERT ... ON CONFLICT DO NOTHING) where applicable'),
      bullet('For critical operations (e.g., recording a delivery), we store the Kafka message key in a processed_events table and check it before processing'),

      h3('Q: How does your route optimization engine scale?'),
      para('A: The Route Optimization Service is stateless — it reads live driver locations from Redis (shared state) and computes routes purely in-memory. This means:'),
      bullet('It can be horizontally scaled to any number of replicas instantly'),
      bullet('Each optimization request is completely independent'),
      bullet('For large fleets (1000+ drivers), we can parallelize nearest-driver search using Java parallel streams'),
      bullet('For very large multi-stop problems (50+ stops), we would switch to a more powerful solver (Google OR-Tools) running as a sidecar container'),

      h2('9.2 Spring Boot / Java Questions'),

      h3('Q: Why Java 21 specifically? What features did you use?'),
      para('A: Java 21 is an LTS release with key features:'),
      bullet('Virtual Threads (Project Loom): Spring Boot 3.2 supports virtual threads via spring.threads.virtual.enabled=true, enabling high concurrency without thread pool tuning'),
      bullet('Record Classes: Used for all DTOs (LoginRequest, AuthResponse, etc.) — immutable, concise, with automatic equals/hashCode/toString'),
      bullet('Pattern Matching Switch: Used in the status transition validation and ETA message building in NotificationService'),
      bullet('Sealed Classes: Can be used to model the sum type of shipment status transitions'),
      bullet('Text Blocks: Used for SQL query strings in tests'),

      h3('Q: How is your JWT validation performed at scale?'),
      para('A: The API Gateway validates every request\'s JWT. To avoid a database call per request:'),
      bullet('JWT is self-contained — the gateway verifies the signature using the shared secret (HMAC-SHA256)'),
      bullet('Only after signature verification does it check the Redis blacklist — a sub-millisecond Redis GET'),
      bullet('The gateway does NOT call the Auth Service for every request — only for the /validate endpoint during WebSocket handshakes'),
      bullet('Gateway uses reactive WebClient for non-blocking I/O — a slow Auth Service call doesn\'t block threads'),

      h3('Q: Explain your Spring Security configuration'),
      para('A: SecurityConfig uses the new lambda DSL (Spring Security 6+):'),
      bullet('CSRF disabled — stateless API with JWT does not need CSRF protection'),
      bullet('SessionCreationPolicy.STATELESS — no HttpSession created or used'),
      bullet('Public endpoints whitelisted: /register, /login, /refresh, /validate, Swagger, Actuator'),
      bullet('JwtAuthenticationFilter runs before UsernamePasswordAuthenticationFilter in the filter chain'),
      bullet('DaoAuthenticationProvider with BCryptPasswordEncoder (cost 12) for login authentication'),
      bullet('@EnableMethodSecurity enables @PreAuthorize on individual endpoints for fine-grained RBAC'),

      h2('9.3 Kafka Deep Dive'),

      h3('Q: What is the relationship between partitions and consumers?'),
      para('A: In a Kafka consumer group:'),
      bullet('Maximum parallel consumers = number of partitions. Extra consumers sit idle.'),
      bullet('The tracking-events topic has 6 partitions, so we run 6 Analytics consumer instances for maximum parallelism.'),
      bullet('Kafka guarantees ordering within a partition. We use the shipmentId as the partition key so all events for the same shipment are ordered.'),
      bullet('Rebalancing happens when consumers join/leave the group — brief pause in processing.'),

      h3('Q: What is a Dead Letter Queue and why do you use one?'),
      para('A: When a Kafka consumer fails to process a message after all retries, it should not block the partition forever. The DLQ (analytics-events.DLT) receives these messages so:'),
      bullet('The consumer can continue processing subsequent messages'),
      bullet('Failed messages are preserved for inspection and manual replay'),
      bullet('Ops team can analyze the DLQ to find systematic processing bugs'),
      bullet('In Spring Kafka, DLT is configured with @RetryableTopic or SeekToCurrentErrorHandler'),

      h2('9.4 Frontend Architecture'),

      h3('Q: Why Redux Toolkit alongside React Query?'),
      para('A: They serve different purposes:'),
      bullet('React Query: Server state — fetching, caching, synchronizing data from the API (shipments, fleet stats, analytics). Handles loading/error states, background refetching, and stale-while-revalidate.'),
      bullet('Redux Toolkit: Client state — authentication tokens (user session), UI state (dark mode, sidebar collapsed), and real-time WebSocket data (driver locations). This state does not come from the server in a request-response pattern.'),
      bullet('The separation is clean: React Query for "what the server knows", Redux for "what the client knows".'),

      h3('Q: How do you handle token refresh in the frontend?'),
      para('A: Axios interceptors handle this transparently:'),
      numbered('Any 401 response triggers the refresh logic'),
      numbered('A flag (isRefreshing) prevents multiple simultaneous refresh calls'),
      numbered('Subsequent 401 requests are queued in a failedQueue array'),
      numbered('On successful refresh, all queued requests are retried with the new token'),
      numbered('On failed refresh, the queue is rejected and the user is logged out'),
      para('This pattern ensures a smooth experience — the user never sees a failed request due to token expiry during normal use.'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 10: SETUP GUIDE
      // ═══════════════════════════════════════════════════════════════════════
      h1('10. Local Setup Guide'),
      divider(),

      h2('10.1 Prerequisites'),
      bullet('Java 21 (recommended: Eclipse Temurin via SDKMAN)'),
      bullet('Maven 3.9+'),
      bullet('Node.js 20+ and npm'),
      bullet('Docker Desktop 4.x+'),
      bullet('Git'),

      h2('10.2 Quick Start (Docker Compose — Recommended)'),
      numbered('Clone the repository: git clone https://github.com/yourusername/routex.git'),
      numbered('Enter project: cd routex'),
      numbered('Start everything: docker-compose up -d'),
      numbered('Wait 90 seconds for services to initialize'),
      numbered('Open frontend: http://localhost:3000'),
      numbered('Login with admin@routex.io / Admin@123456'),
      numbered('API Gateway: http://localhost:8080'),
      numbered('Swagger UI (Auth): http://localhost:8081/swagger-ui.html'),
      numbered('Kafka UI: http://localhost:8092'),
      numbered('Grafana: http://localhost:3001 (admin/routex123)'),

      h2('10.3 Build Backend Services'),
      para('To build and run a single service locally:'),
      codeBlock('# Build all services'),
      codeBlock('mvn clean package -DskipTests'),
      codeBlock(''),
      codeBlock('# Run auth service'),
      codeBlock('cd services/auth-service'),
      codeBlock('mvn spring-boot:run'),

      h2('10.4 Build Frontend'),
      codeBlock('cd frontend'),
      codeBlock('npm install'),
      codeBlock('npm run dev   # Development server on port 3000'),
      codeBlock('npm run build # Production build'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 11: GITHUB + FREE HOSTING
      // ═══════════════════════════════════════════════════════════════════════
      h1('11. GitHub Upload & Free Hosting Guide'),
      divider(),

      h2('11.1 Publishing to GitHub'),
      numbered('Create a new repository on github.com — name it "routex" or "routex-platform"'),
      numbered('Make it PUBLIC — required for free Kubernetes hosting on platforms like Render, Railway'),
      numbered('Initialize Git in the project folder:'),
      codeBlock('cd /path/to/routex'),
      codeBlock('git init'),
      codeBlock('git add .'),
      codeBlock('git commit -m "feat: initial RouteX platform implementation"'),
      codeBlock('git branch -M main'),
      codeBlock('git remote add origin https://github.com/YOUR_USERNAME/routex.git'),
      codeBlock('git push -u origin main'),
      numbered('Add a .gitignore before the first commit to exclude build artifacts:'),
      codeBlock('echo "target/\\nnode_modules/\\n.env\\n*.log\\n.DS_Store" > .gitignore'),
      numbered('Add the README.md as the project homepage — GitHub renders it as your landing page'),
      numbered('Add topics to the repository (Settings > Topics): java, spring-boot, react, kafka, kubernetes, microservices, logistics'),

      h2('11.2 Hosting the Frontend for Free — Vercel (Recommended)'),
      numbered('Go to vercel.com and sign up with your GitHub account'),
      numbered('Click "Add New Project" and import the routex repository'),
      numbered('Set the Root Directory to: frontend'),
      numbered('Set the Build Command: npm run build'),
      numbered('Set the Output Directory: dist'),
      numbered('Add environment variable: VITE_API_URL = https://your-backend-url/api'),
      numbered('Click Deploy — Vercel gives you a free *.vercel.app URL'),
      numbered('Every push to main auto-deploys (CI/CD built-in)'),
      para('Alternative: Netlify (same process), Cloudflare Pages (even faster CDN)'),

      h2('11.3 Hosting Backend for Free — Railway.app (Recommended)'),
      numbered('Go to railway.app and sign up with GitHub'),
      numbered('Click "New Project" > "Deploy from GitHub repo"'),
      numbered('Railway detects Docker Compose and can spin up individual services'),
      numbered('Add a PostgreSQL plugin for each service that needs a database'),
      numbered('Add a Redis plugin for caching and sessions'),
      numbered('Set environment variables in Railway\'s dashboard (DB_HOST, JWT_SECRET, etc.)'),
      para('Note: Free tier on Railway gives $5/month credits — enough for the auth, shipment, and API gateway services for a demo.'),

      h2('11.4 Alternative Free Hosting Options'),
      dataTable(
        ['Platform', 'What to host', 'Free Tier', 'Notes'],
        [
          ['Vercel', 'React Frontend', 'Unlimited deployments', 'Best for frontend — global CDN'],
          ['Railway.app', 'Spring Boot services', '$5/month credits', 'Supports Docker, PostgreSQL, Redis'],
          ['Render.com', 'API Gateway + Auth Service', '750 hours/month', 'Free PostgreSQL (90-day expiry)'],
          ['Koyeb.com', 'Any Docker service', '2 free instances', 'Global edge deployment'],
          ['Fly.io', 'Docker containers', '3 shared VMs free', 'Good for Kafka-heavy workloads'],
          ['Supabase', 'PostgreSQL databases', '2 projects free', 'Hosted Postgres with REST API'],
          ['Upstash', 'Redis', '10K commands/day free', 'Serverless Redis — perfect for sessions'],
        ],
        [1400, 2000, 1800, 3160]
      ),

      h2('11.5 Making It Recruiter-Ready'),
      bullet('Add a live demo link to your GitHub repository description'),
      bullet('Record a 3-5 minute Loom video showing the live tracking map, shipment creation, and Grafana dashboard'),
      bullet('Add architecture diagram as an image in the README'),
      bullet('Include your tech stack badges in the README: shields.io provides Java, Spring Boot, React, Kafka, Docker, Kubernetes badges'),
      bullet('Pin this repository on your GitHub profile'),
      bullet('Add it to your LinkedIn as a "Featured" project with the Vercel link'),
      bullet('Write a 500-word Medium/Dev.to article about one technical decision (e.g., "How I built a real-time tracking system with WebSockets and Redis")'),

      // ═══════════════════════════════════════════════════════════════════════
      // SECTION 12: METRICS & PERFORMANCE
      // ═══════════════════════════════════════════════════════════════════════
      h1('12. Performance Characteristics'),
      divider(),

      dataTable(
        ['Operation', 'Latency Target', 'Throughput', 'Bottleneck'],
        [
          ['JWT Validation (Gateway)', '< 5ms', '10,000 req/s per instance', 'Redis blacklist lookup'],
          ['Shipment Creation', '< 50ms', '500 req/s per instance', 'PostgreSQL write'],
          ['Driver Location Update', '< 10ms', '5,000 updates/s per instance', 'Redis write + Kafka produce'],
          ['Route Optimization (10 stops)', '< 100ms', '200 req/s per instance', 'Algorithm CPU'],
          ['Route Optimization (30 stops)', '< 500ms', '50 req/s per instance', '2-opt iteration count'],
          ['Analytics Dashboard Query', '< 200ms', '100 req/s', 'PostgreSQL aggregate query'],
          ['WebSocket Broadcast (1000 clients)', '< 20ms', 'Continuous', 'Network I/O'],
        ],
        [2400, 1560, 1680, 2520]
      ),

      new Paragraph({ spacing: { before: 800 } }),

      new Paragraph({
        children: [
          new TextRun({
            text: 'RouteX Platform Documentation — Generated for Interview Preparation',
            size: 18, color: '94A3B8', italics: true
          })
        ],
        alignment: AlignmentType.CENTER,
        spacing: { before: 400 }
      })
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('/mnt/user-data/outputs/RouteX_Technical_Documentation.docx', buffer);
  console.log('Document written successfully');
}).catch(err => {
  console.error('Error:', err);
  process.exit(1);
});
