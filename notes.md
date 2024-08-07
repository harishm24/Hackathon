# Fundamental of Backend Communications and Protocol

## Design Patterns

### Request - Response
Client sends a request, then server parses and then processes the request, finally send a response to the client, which parses it and consumes.

#### Where it is used?
- Web, HTTP, DNS. SSH
- RPC (Remote Procedure Call)
- SQL and Database Protocols
- APIs (REST/SOAP/GraphQL)
- Implemented in variations

#### Anatomy of Request/Response
- A request structure is defined by both client and server.
- Request has a boundary
- Defined by a protocol and message

#### Where it doesn't work
- Notification service (doesn't really scale well)
- Chatting application (can try but will just spam the network)
- Very Long requests (possible but its better to use another of execution like asynchronous)

#### Where it isn't ideal
- A request takes long time to process
    - Upload a YouTube video
- The backend wants to sends notification
   - A user just logged in
- Polling is a good communication style

#### Pros and Cons
- Pros
    - Elegant and Simple
    - Scalable
- Cons
  - Bad for multiple receivers (as it cannot be controlled in an elegant manner)
  - High coupling
  - Client/server have to be running
  - Chaining and circuit breaking (that's why service mesh and sidecar proxies are there)

### Asynchronus v/s Synchronous

#### Synchronous I/O
- Caller sends a request and blocks
- Caller cannot execute any code meanwhile
- Receiver responds, Caller unblocks
- Caller and Receiver are in "sync"

##### Example
- Program ask OS to read from disk
- Program main thread is taken off the CPU
- Read completes, program can resume execution

#### Asynchronous I/O
- Caller sends a request
- Caller can work until it gets a response
- Caller either
   - Checks if the response is ready (epoll)
   - Receiver calls back when it's done (io_uring)
   - Spins up a new thread that blocks
- Caller and receiver are not necessary in sync

##### Workload
- async programming (promises/futures)
- async backend processing
- async commits in postgres
- async io in linux (epoll, io_uring)
- async replication
- async os fsync (fs cache)

##### Example
- Program spins up a second thread
- Secondary thread reads from disk, OS blocks it
- Main program still running and executing code
- Thread finish reading and call backs main thread

#### Synchronous v/s Asynchronous in Request Response
- Synchronicity is a client property
- Most modern client libraries are async
- Client send an HTTP request and do work

#### Synchronous v/s Asynchronous in real life
- If it is still confusing
- In synchronous communication the caller waits for a response from receiver
   -  Like asking someone a question in a meeting
- Asynchronous communication the response can come whenever. Caller and receiver can do anything meanwhile
   - email

### Push
Very popular if really want the response as fast as possible like really need the results immediately in client

- Client connects to a server
- Server sends data to the client
- Client doesn't have to request anything
- Protocol must be bidirectional (TCP can work as a push effectively)
- Used by RabbitMQ

#### Pros and Cons
- Pros
  -  Real time (push to the client immediately and writing to the client socket as moment the event is generated)
- Cons
   - Clients must be online (cannot push something to a client that is offline)
   - Clients might not be able to handle (Kafka didn't move to push model because of hard time processing messages of too many push data)
   - Requires a bidirectional protocol
   - Polling is preferred for light client (client pulls on their leisure)

Websockets is actually a bidirectional protocol, because it uses the TCP link so it support push

#### Request/response isn't always ideal
- Client wants real time notification from backend
	- A user just logged in
	- A message us just received
- Push model is good for certain cases

### Short Polling
Very common when a request takes a long time to process and execute it asynchronously on the backend.

The backend can do whatever it feels to like it can queue, can persist through some sort of a disk, can put it in memory and then later execute the request.

The request is not executed immediately but can be checked later for progress.

- Client sends a request
- Server responds immediately with a handle (in form of a unique identifier that corresponds to this request)
- Client uses that handle to check for status
- Multiple "short" request response as polls

#### Mechanism 
- A request is sent and a server immediately responds back with request ID, job ID and task ID
- Client will save this ID to disk and then disconnect
- Another client can pick up the pending request and check if the request is ready now after several requests
- If disconnected the server will try to respond, client in this case was disconnected and a beautiful response was lost, server is not gonna keep that response

#### Pros
- Simple (Client is very simple to build as well as the backend)
- Good for long running requests
- Client can disconnect

#### Cons
- Too chatty
- Network bandwidth
- Wasted backend resources

### Long Polling
- Client sends a request
- Sever responds immediately with a handler
- Server continues to process the request
- Client uses that handle to check for status
- Server DOES not reply until it has the response
- Client can disconnect as a handle was given and is less chatty

#### Mechanism
- A request is sent to the server
- The server immediately responds back with request ID, job ID and task ID
- Ask if the request is ready
- Response is sent the moment it is ready

### Server Sent Events
A pure HTTP thing and doesn't really work on other protocols. The response is very, very, very long but it doesn't have an end (the respawn doesn't have an end). In this data, data is keep getting and it just never ends.

The trick here is the client to understand these chunks, the mini responses. But the client is smart enough.

There is a limitation in Chrome that only six TCP connections can be establish to that domain and a lot of browsers follow that because in HTTP 1.1, you can send only one request per connection. While this request is being processed, nothing else can be sent in that connection unless you enable pipeline though that's a problematic thing.

- A response has start and end
- Client sends a request
- Server sends logical events as part of response
- Server never writes the end of response
- It is still a request but an unending response
- Client parses the stream data looking for these events
- Works with request/response (HTTP)

#### Mechanism
- Client sends a request, special request with a special content type
- The server will actually respond with an event which is a bunch of bytes that has a start and end. The client actually needs to understand.
- It didn't technically finish writing the response yet because technically, if you write the full response (That's how TCP sockets work)
- Another event can be written in this case and then it pauses
- The client can process these events and then finally it can close that whole connection

#### Pros and Cons
- Pros
   - Real time 
   - Compatible with request/response model and HTTP model
- Cons
   - Client must be online (as you are sending a request and the client has to be there to receive the mini responses)
   - Client might not be able to handle (same problem as Push)
   - Polling is preferred for light clients which are not sophisticated enough
   - HTTP/1.1 problem (6 connections)

### Push Subscribe (Pub/Sub)
Publish, subscribe and publish subscribers where a client can publish and write.

Publish something to the server which then moves on and then the client can consume from the server. 

One publisher, many readers, can also have many publishers

#### Pros and Cons
- Pros
  - Scales with multiple receivers
  - Great for microservices
  - Loose Coupling
  - Works while client not running
- Cons
  - Message delivery issues
  - Complexity
  - Network saturation



### Serve Multiplexer
- Often referred as "ServeMux" or "mux"
- Component used in web development, particularly in web servers, to route incoming HTTP requests to the appropriate handler functions based on the URL path or other request attributes
- Acts as a request router and dispatcher, determines

- If you have a lot of requests, a lot of signals coming into a box, shove all these signals into a single line

#### Purpose
- Help organise and manage the routing of HTTP requests with a web application
- Allows developers to define how different paths are handled, making it easier to structure the application's logic and keep the code clean

### Server Demultiplexer
Reverse of Multiplexing

### Connection Pooling
- A technique where one can effectively spin up multiple database connections



### Stateful v/s Stateless
- Stateful
  - Stores state about clients in its memory
  - Depends on the information being there

- Stateless
  - Client is responsible to "transfer the state" with every request
  - May store but can safely lose it

#### What makes a backend stateless?
  - Stateless backends can store state somewhere else (database)
  - The backend remains stateless but the system is stateful
  - Can you restart the backend during idle time and the client workflow continue to work?

#### Stateful backend
- A login application where a user visits a login page where username and password is entered
- Backend talks are turn around and talk to Postgres or a database which verifies that the username and password is correct
- It responds back and then the application generates, the backend will generate a session ID which returns to the user
- It doesn't store the session in the database stores
- If it is already there in the memory, authentication is done for this user

#### Why is TCP stateful?
- Information stored above the client and the server in both the client and the server. There are sequences.
- Every segment that you send is labeled with a sequence, and the sequence is stored in actually stored in a state.
- There are state diagrams which literally say "connection is closed", "connection is open", "connection is now established" etc
- The state machine living here and in the server side. They maintain the connection. sequence and windows sizes, the flow control, congestion control. All of these are state information.
- If they are lost, this connection is pointless, it's useless
- If the connection is killed then its pointless to move on
- If connection is rested and any of these parameters are lost, effectively
- Have connection file descriptor sequences

#### Why is UDP stateless?
- Message base, but it doesn't store anything

#### Stateless v/s Stateful protocols
- Protocols can be designed to store state
- TCP is stateful
   - Sequences, Connection file descriptors
- UDP is stateless
   - DNS send queryID in UDP to idenfity queries
   - QUIC sends connectionID to identify connection
- Possible to build a stateless protocol on the top of a stateful and vice versa
- HTTP (Stateless) on the top of TCP (stateful)
- If TCP breaks, HTTP blindly create another one
- QUIC (stateful) on top UDP (stateless)

### Complete Stateless System
- Stateless Systems are rare
- State is carried with every request
- A backend service that relies completely on input
   - Check if input param is a prime number
- JWT (JSON Web Token)

### Changing the library is hard
- The library your app is entrenched
- App and Library should be same language
- Changing the library require retesting
- Breaking changes backward compatibility
- Adding features to the library is hard
- Microservices suffer

### Sidebar Pattern
Pros
- Language agnostic (polyglot)
- Protocol upgrade
- Security
- Tracing and Monitoring
- Service Discovery
- Caching

Cons
- Complexity
- Latency

## Protocol
- A system that allows two parties to communicate
- A protocol is designed with a set of properties
- Depending on the purpose of the protocol
- TCP. UDP. HTTP, gRPC, FTP

### Protocol Properties

- Data formats
  - Text based (plain text, JSON, XML)
  - Binary (protobud, RESP, h2, h3)

- Transfer mode
   - Message based (UDP, HTTP)
   - Stream (TCP, WebRTC)

- Addressing System (where it is coming from and where is this going to)
  - DNS name, IP, MAC

- Directionality
  - Bidirectional (TCP)
  - Unidirectional (HTTP)
  - Full/Half duplex

- State
  - Stateful (TCP, gRPC, apache limit)
  - Stateless (UDP, HTTP)

- Routing
  - Proxies, Gateways

- Flow & Congestion control
  - TCP (Flow & Congestion)
  - UDP (No control)

- Error management
   - Error codes
   - Retries and timeouts


### Open Systems Interconnection (OSI) Model

#### Need for a communication model?

- Agnostic applications
  - Application must have the knowledge of underlying network medium if there's no standard model
  - Imaging authoring different versions of apps so that it works on WiFI v/s ethernet v/s LTE v/s fiber

- Network Equipment Management
  - Without a standard model, upgrading network equipment becomes difficult

- Decoupled Innovation
  - Innovations can be done in each layer separately without affecting the rest of the models

### OSI Model

- 7 layers each describe a specific network component
- Layer 7 - Application - HTTP/FTP/gRPC
- Layer 6 - Presentation - Encoding, Serialization
- Layer 5 - Session - Connection establishment, TLS
- Layer 4 - Transport - UDP/TCP
- Layer 3 - Network - IP
- Layer 2 - Data link - Frames, Mac address Ethernet
- Layer 1 - Physical - Electric signals
#### Example (Sender)

- Example sending a POST request to an HTTPS webpage
- Layer 7 - Application
  - POST request with JSON  data to HTTPS Server
- Layer 6 - Presentation
   - Serialize JSON to flat byte strings
- Layer 5 - Session
  - Request to establish TCP connection/TLS
- Layer 4 - Transport
  - Sends SYN request target port 443
- Layer 3 - Network
   - SYN is placed an IP packet(s) and adds the source/dest IPs
- Layer 2 - Data link
  - Each packet goes into a single frame and adds the source/dest MAC address
- Layer 1 - Physical
   - Each frame becomes string of bits which converted into either a radio signal (WiFi), electronic signal (ethernet), or light (fiber)
- Take it with a gain of salt, it's not always cut and dry



### TCP

- TCP is a widely used network protocol. It's a "reliable" protocol that runs on top of an unreliable protocol: IP, short for Internet Protocol.

- Primarily, TCP offers two guarantees: **(a)** Reliable delivery of packets and **(b)** In-order delivery of packets.
#### Guarantees

- TCP ensures that no packets are lost in transit. It does this by asking the receiver to acknowledge all sent packets, and re-transmitting any packets if an acknowledgement isn't received.

- In addition to guaranteeing packets reach their destination, TCP also guarantees that the packets are delivered in order. It does this by labelling each packet with a sequence number. The receiver tracks these numbers and reorders out-of-sequence packets. If a packet is missing, the receiver waits for it to be re-transmitted.
#### Connections

- TCP is a connection-oriented protocol, which means that to interact over TCP a program must first "establish a connection". To do this, one program takes the role of a "server", and the other program takes the role of a "client".

- The server waits for connections, and the client initiates a connection. Once a connection is established, the client & server can both receive and send data (it's a two-way channel).

- A TCP connection is identified using a unique combination of four values:
	-  destination IP address
	- destination port number
	- source IP address
	- source port number

#### Handshake

 - The TCP handshake is how clients establish connections with servers. This is a 3-step process.

  ##### Step 1: SYN
  - First, the client initiates the connection by sending a SYN (synchronize) packet to the server, indicating a request to establish a connection. This packet also contains a sequence number to maintain the order of the packets being sent.
  
  ##### Step 2: SYN-ACK
  - The server, upon receiving this SYN packet, sends back a SYN-ACK (synchronize-acknowledge) packet.

  ##### Step 3: ACK
   - In the final step of this three-way handshake, the client acknowledges the server's SYN-ACK packet by sending an ACK (acknowledge) packet. The connection is considered established once this last packet is received by the server.


### IP

- When a program sends data over the network using IP, the data is broken up and sent as multiple "packets".

- Each packet contains:
    - a header section
    -  a data section

- The header contains a source and destination address, much like an envelope that you send through your local postal service.

- The important similarity between IP and a postal service is that packets are **not guaranteed** to arrive at the destination. Although every effort is made to get it there, sometimes packets get lost in transit.

- Furthermore, if you send 5 packets at once, there's no guarantee that they'll arrive at their destination at the same time or in the same order.









What is OSI model?
The OSI model (Open System Interconnection) model defines a computer networking framework to implement protocols in seven layers. A protocol in the networking terms is a kind of negotiation and rule in between two networking entities.

Layers of OSI model:
Physical layer

The Physical layer is also called as the Layer 1. Here are the basic functionalities of the Physical layer:

Responsible for electrical signals, light signal, radio signals etc.
Hardware layer of the OSI layer
Devices like repeater, hub, cables, ethernet work on this layer
Protocols like RS232, ATM, FDDI, Ethernet work on this layer
Data Link layer

The data link layer is also called as the Layer 2 of the OSI model. Here are the basic functionalities of the data link layer:

Responsible for encoding and decoding of the electrical signals into bits.
Manages data errors from the physical layer
Convers electrical signals into frames
The data link layer is divided into two sub-layers
The Media Access Control (MAC) layer
Logical Link Control (LLC) layer.
The MAC sublayer controls how a computer on the network gains access to the data and permission to transmit it.
The LLC layer controls frame synchronization, flow control and error checking.
MAC address is a part of the layer 2.
Devices like Switch work at this layer
Network Layer

The Network layer is also called as the layer 3 of the OSI model. Here are the basic functionalities of the network layer:

Switching and routing technologies work here
Creates logical paths between two hosts across the world wide web called as virtual circuits
Routes the data packet to destination
Routing and forwarding of the data packets.
Internetworking, error handling, congestion control and packet sequencing work at this layer
Router works at layer three
Different network protocols like TCP/ IP, IPX, AppleTalk work at this layer
Transport layer

The Transport  layer is also called as the layer 4 of the OSI model. Here are the basic functionalities of the Transport layer:

Responsible for the transparent transfer of data between end systems
Responsible for end-to-end error recovery and flow control
Responsible for complete data transfer.
Protocols like SPX, TCP, UDP work here
Session layer

The Session  layer is also called as the layer 5 of the OSI model. Here are the basic functionalities of the Session layer:

Responsible for establishment, management and termination of connections between applications.
The session layer sets up, coordinates, and terminates conversations, exchanges, and dialogues between the applications at each end.
It deals with session and connection coordination.
Protocols like NFS, NetBios names, RPC, SQL work at this layer.
Presentation layer

The Presentation layer is also called as the layer 6 of the OSI model. Here are the basic functionalities of the presentation layer:

Responsible for data representation on your screen
Encryption and decryption of the data
Data semantics and syntax
Layer 6 Presentation examples include encryption, ASCII, EBCDIC, TIFF, GIF, PICT, JPEG, MPEG, MIDI.
Application Layer

The Application layer is also called as the layer 7 of the OSI model. Here are the basic functionalities of the Application layer:

Application layer supports application, apps, and end-user processes.
Quality of service
This layer is responsible for application services for file transfers, e-mail, and other network software services.
Protocols like Telnet, FTP, HTTP work on this layer.
