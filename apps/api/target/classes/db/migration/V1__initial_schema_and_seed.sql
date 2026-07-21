CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE public.app_users (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE public.password_reset_tokens (
  token TEXT NOT NULL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX password_reset_tokens_user_idx ON public.password_reset_tokens(user_id);

-- ============ CATEGORIES ============
CREATE TABLE public.categories (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  name TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  color TEXT NOT NULL DEFAULT '#6366f1',
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ QUESTIONS ============
CREATE TABLE public.questions (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  prompt TEXT NOT NULL,
  qtype TEXT NOT NULL CHECK (qtype IN ('recall','scenario','why','choose','proscons','followup')),
  difficulty TEXT NOT NULL DEFAULT 'sde2' CHECK (difficulty IN ('sde1','sde2','sde3')),
  category_id UUID REFERENCES public.categories(id) ON DELETE SET NULL,
  companies TEXT[] NOT NULL DEFAULT '{}',
  sources TEXT[] NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX questions_category_idx ON public.questions(category_id);

-- ============ ANSWER KEYS ============
CREATE TABLE public.answer_keys (
  question_id UUID NOT NULL PRIMARY KEY REFERENCES public.questions(id) ON DELETE CASCADE,
  bullets JSONB NOT NULL DEFAULT '[]'::jsonb,
  explanation TEXT NOT NULL DEFAULT '',
  follow_ups JSONB NOT NULL DEFAULT '[]'::jsonb,
  common_mistakes JSONB NOT NULL DEFAULT '[]'::jsonb,
  when_not_to_use TEXT NOT NULL DEFAULT ''
);

-- ============ PROFILES ============
CREATE TABLE public.profiles (
  user_id UUID NOT NULL PRIMARY KEY REFERENCES public.app_users(id) ON DELETE CASCADE,
  display_name TEXT,
  daily_goal INT NOT NULL DEFAULT 10,
  streak_count INT NOT NULL DEFAULT 0,
  last_active_date DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- ============ USER CARD STATE (SRS) ============
CREATE TABLE public.user_card_state (
  user_id UUID NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
  question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
  ease REAL NOT NULL DEFAULT 2.5,
  interval_days INT NOT NULL DEFAULT 0,
  due_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  times_seen INT NOT NULL DEFAULT 0,
  times_correct INT NOT NULL DEFAULT 0,
  last_result TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, question_id)
);
CREATE INDEX user_card_state_due_idx ON public.user_card_state(user_id, due_at);

-- ============ ATTEMPTS ============
CREATE TABLE public.attempts (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
  question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
  self_rating TEXT CHECK (self_rating IN ('got','missed')),
  ai_score INT,
  user_answer TEXT,
  ai_feedback JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX attempts_user_idx ON public.attempts(user_id, created_at DESC);

-- ============ FAVORITES ============
CREATE TABLE public.favorites (
  user_id UUID NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
  question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, question_id)
);

-- ============ SEED CATEGORIES ============
INSERT INTO public.categories (slug, name, color, sort_order) VALUES
  ('databases','Databases','#3b82f6',1),
  ('caching','Caching','#ef4444',2),
  ('load-balancing','Load Balancing','#22c55e',3),
  ('messaging','Messaging','#f59e0b',4),
  ('scalability','Scalability','#a855f7',5),
  ('reliability','Reliability','#06b6d4',6),
  ('monitoring','Monitoring','#eab308',7),
  ('security','Security','#ec4899',8),
  ('tradeoffs','Tradeoffs','#8b5cf6',9),
  ('troubleshooting','Troubleshooting','#f97316',10);

-- ============ SEED QUESTIONS + ANSWER KEYS ============
DO $seed$
DECLARE
  cat_db UUID; cat_cache UUID; cat_lb UUID; cat_msg UUID; cat_scale UUID;
  cat_rel UUID; cat_mon UUID; cat_sec UUID; cat_tr UUID; cat_tb UUID;
  qid UUID;
BEGIN
  SELECT id INTO cat_db FROM public.categories WHERE slug='databases';
  SELECT id INTO cat_cache FROM public.categories WHERE slug='caching';
  SELECT id INTO cat_lb FROM public.categories WHERE slug='load-balancing';
  SELECT id INTO cat_msg FROM public.categories WHERE slug='messaging';
  SELECT id INTO cat_scale FROM public.categories WHERE slug='scalability';
  SELECT id INTO cat_rel FROM public.categories WHERE slug='reliability';
  SELECT id INTO cat_mon FROM public.categories WHERE slug='monitoring';
  SELECT id INTO cat_sec FROM public.categories WHERE slug='security';
  SELECT id INTO cat_tr FROM public.categories WHERE slug='tradeoffs';
  SELECT id INTO cat_tb FROM public.categories WHERE slug='troubleshooting';

  -- Databases
  INSERT INTO public.questions (prompt, qtype, category_id, companies) VALUES
    ('How do you remove a Single Point of Failure from a relational database?', 'recall', cat_db, ARRAY['Netflix','Uber']) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Primary + replica replication","Automatic failover (Patroni, RDS Multi-AZ)","Leader election via consensus (Raft/ZooKeeper)","Deploy across multiple availability zones","Health checks + connection draining"]'::jsonb,
    'Combine synchronous or semi-sync replication with an orchestrator that promotes a replica when the primary fails, spread across AZs so a zone outage does not take everything down.',
    '["What if two nodes think they are leader?","How do you avoid split-brain?","Sync vs async replication trade-off?"]'::jsonb,
    '["Assuming a single hot standby is enough","Ignoring quorum/fencing","Forgetting the app-side connection retry logic"]'::jsonb,
    'When strong write availability is not required and RTO of minutes is acceptable, a single-AZ setup with backups may suffice.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('When would you pick NoSQL over SQL for 500M users, global, read-heavy traffic?', 'choose', cat_db) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Access patterns are known and key-based","Horizontal scale > complex joins","Eventual consistency acceptable","Schema flexibility needed","Multi-region low-latency reads"]'::jsonb,
    'For global read-heavy workloads with simple lookups, NoSQL (DynamoDB, Cassandra) scales writes/reads linearly and supports multi-region replication with tunable consistency.',
    '["What about strong consistency?","How do you handle secondary indexes?","When would you still choose SQL?"]'::jsonb,
    '["Choosing NoSQL and then needing joins","Ignoring hot partitions","Forgetting cost of eventual consistency for user-visible flows"]'::jsonb,
    'When you need ACID multi-row transactions, complex ad-hoc queries, or strong analytical joins.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Explain the CAP theorem in one sentence and give an example of a CP and an AP system.', 'recall', cat_db) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Under a network partition, you choose Consistency or Availability","CP: HBase, Zookeeper, MongoDB (strong reads)","AP: Cassandra, DynamoDB (eventual)","Latency (PACELC) matters when no partition","Most real systems are tunable"]'::jsonb,
    'CAP says during a partition you must trade consistency for availability; PACELC extends this to latency vs consistency during normal operation.',
    '["What is PACELC?","Is Kafka CP or AP?","How does DynamoDB tune consistency?"]'::jsonb,
    '["Thinking CAP means pick 2 of 3 always","Ignoring latency trade-offs"]'::jsonb,
    'CAP is a decision framework, not a strict classification for every operation.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('When should you shard a database?', 'why', cat_db) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Vertical scaling has hit a hardware ceiling","Working set no longer fits in RAM","Write throughput exceeds one node","Storage exceeds one node","Blast radius must be limited"]'::jsonb,
    'Shard when a single node cannot handle writes, storage, or working set, and read replicas + caching cannot bridge the gap.',
    '["How do you pick a shard key?","How do you rebalance?","How do you do cross-shard joins?"]'::jsonb,
    '["Sharding too early (adds ops cost)","Picking a low-cardinality shard key -> hot shards"]'::jsonb,
    'When read replicas + caching still handle load, and the dataset fits on one node with headroom.');

  -- Caching
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('What are cache stampede, penetration, and avalanche — and one fix for each?', 'recall', cat_cache) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Stampede: many requests rebuild same key -> single-flight / request coalescing","Penetration: repeated misses for keys that do not exist -> negative caching / bloom filter","Avalanche: many keys expire at once -> jittered TTLs","Add stale-while-revalidate","Use per-key locks for hot rebuilds"]'::jsonb,
    'Three distinct failure modes: too many concurrent rebuilds (stampede), lookups that never hit (penetration), and synchronized expiry storms (avalanche).',
    '["How does single-flight work?","When is a bloom filter wrong choice?","How much jitter is enough?"]'::jsonb,
    '["Using same TTL for all keys","Caching only successful lookups"]'::jsonb,
    'For very low-traffic keys, simple TTL is fine without any of these mitigations.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Write-through vs write-back vs write-around — when do you use each?', 'proscons', cat_cache) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Write-through: writes go to cache + DB (consistent, higher latency)","Write-back: writes to cache, async to DB (fast, risk data loss)","Write-around: writes skip cache (avoids polluting cache with rarely-read data)","Use write-through for read-heavy consistent data","Use write-back for high write throughput tolerant of loss"]'::jsonb,
    'Pick based on read/write ratio, durability needs, and tolerance for stale data.',
    '["What if the cache node dies in write-back?","How do you invalidate write-around?"]'::jsonb,
    '["Using write-back for financial data","Ignoring cache warming"]'::jsonb,
    'Write-back is unsafe for anything you cannot afford to lose on cache failure.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Why invalidate cache AFTER a DB update rather than before?', 'why', cat_cache) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Prevents another reader from repopulating with old value between invalidate and DB write","Reduces window of inconsistency","Common pattern: update DB, then delete cache key (cache-aside)","Consider double-delete for replication lag","Use versioned keys for strict correctness"]'::jsonb,
    'If you delete before writing, a concurrent read can miss, fetch old DB row, and repopulate the cache with stale data.',
    '["What is double-delete?","How do you handle read replicas?"]'::jsonb,
    '["Invalidating before commit","Ignoring replication lag when reading own writes"]'::jsonb,
    'When strict correctness matters, prefer versioned cache keys or transactional outbox instead.');

  -- Load Balancing
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('L4 vs L7 load balancing — key differences?', 'proscons', cat_lb) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["L4: TCP/UDP, faster, no payload inspection","L7: HTTP, can route by path/header/cookie","L7: TLS termination, WAF, retries","L4: lower latency, protocol-agnostic","Use L7 for microservice routing, L4 for raw throughput"]'::jsonb,
    'L4 forwards packets; L7 understands application protocols and can make content-based routing decisions.',
    '["Where does mTLS terminate?","How does health checking differ?"]'::jsonb,
    '["Assuming L7 always needed","Ignoring cost of TLS termination"]'::jsonb,
    'L7 adds latency and CPU cost; skip it if you do not need path/header awareness.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('When are sticky sessions a bad idea?', 'why', cat_lb) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Uneven load distribution (hot instances)","Failover loses session on instance death","Harder to autoscale","Prevents rolling deploys of session-state","Fix: externalize session (Redis, JWT)"]'::jsonb,
    'Stickiness ties users to instances, which breaks rebalancing, autoscaling, and clean deploys.',
    '["How would you migrate off sticky sessions?","Is stickiness ever OK?"]'::jsonb,
    '["Using sticky sessions to avoid designing shared session store","Assuming cookies alone solve it across regions"]'::jsonb,
    'When sessions are cheap to recreate or a shared session store exists, avoid stickiness.');

  -- Messaging
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Kafka vs RabbitMQ — when do you pick each?', 'proscons', cat_msg) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Kafka: high-throughput log, replayable, partitioned ordering, analytics/streaming","RabbitMQ: flexible routing, low-latency task queues, per-message ack, complex topologies","Kafka: consumers track offsets","RabbitMQ: broker tracks delivery","Kafka scales to millions/sec; Rabbit better for RPC-like workflows"]'::jsonb,
    'Kafka is a distributed log optimized for throughput and replay; RabbitMQ is a broker optimized for flexible per-message routing and delivery.',
    '["How does exactly-once work in Kafka?","How would you do priority queues?","When does Rabbit fall over?"]'::jsonb,
    '["Using Kafka for RPC-style request/response","Using Rabbit for TB/day event streams"]'::jsonb,
    'Kafka is heavy operationally — avoid for small task queues.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('How do you guarantee ordered processing in Kafka across many consumers?', 'recall', cat_msg) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Order is per-partition only","Use a partition key that groups related messages","Consumer group: one partition -> one consumer at a time","Scale consumers up to partition count","Repartition or use hash-based key for skew"]'::jsonb,
    'Kafka only guarantees order within a partition, so key routing must align with your ordering domain (e.g. user_id).',
    '["What happens on rebalance?","How do you handle poison messages?"]'::jsonb,
    '["Expecting global order across partitions","Picking too few partitions (bottleneck) or too many (overhead)"]'::jsonb,
    'When strict global order across all events is needed — use a single partition (with throughput limits) or a different tech.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('What is a Dead Letter Queue and when should messages go there?', 'recall', cat_msg) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Separate queue for messages that failed processing repeatedly","After max retry count","For poison messages / malformed payloads","Allows main queue to drain","Enables manual inspection + replay"]'::jsonb,
    'DLQs isolate failures so consumers keep making progress; you must alert on DLQ growth and have a replay path.',
    '["How do you replay a DLQ?","What if the DLQ fills up?"]'::jsonb,
    '["Silent DLQs with no alerting","Auto-retrying without backoff"]'::jsonb,
    'For fire-and-forget metrics events, dropping may be cheaper than DLQ.');

  -- Scalability
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('You need to serve a 500M user, global, read-heavy workload. Pick the top 3 things you would deploy.', 'choose', cat_scale) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["CDN in front of static + cacheable API responses","Multi-region read replicas / edge database","Distributed cache (Redis) for hot keys","Horizontal app tier behind L7 LB","Async writes via queues"]'::jsonb,
    'Push reads to the edge (CDN + cache), replicate data close to users, and keep the origin thin.',
    '["Where do writes go?","How do you handle stale reads?","What about consistency for user-visible actions?"]'::jsonb,
    '["Assuming one region can serve globally","Skipping cache invalidation strategy"]'::jsonb,
    'For low-scale internal tools, this stack is over-engineered — a single region + DB may be enough.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Rate limiting: token bucket vs leaky bucket vs fixed window — trade-offs?', 'proscons', cat_scale) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Token bucket: allows bursts up to bucket size","Leaky bucket: smooths output at constant rate","Fixed window: simple but bursty at boundaries","Sliding window log: precise but memory-heavy","Sliding window counter: good balance"]'::jsonb,
    'Pick based on whether you want to allow bursts (token) or smooth traffic (leaky), and how much state you can hold.',
    '["Where do you store counters at scale?","How do you handle distributed rate limits?"]'::jsonb,
    '["Fixed window causing 2x burst at boundary","Global limit without per-user fairness"]'::jsonb,
    'For strict per-second SLAs, sliding window counter is safer than fixed window.');

  -- Reliability
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Explain the Circuit Breaker pattern and its three states.', 'recall', cat_rel) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Closed: requests flow, count failures","Open: fail fast, do not call downstream","Half-open: allow a probe, close on success","Prevents cascading failure","Pair with timeouts + bulkheads"]'::jsonb,
    'A circuit breaker stops hammering a failing dependency, giving it time to recover and shielding the caller.',
    '["What thresholds do you use?","What if breaker never closes?"]'::jsonb,
    '["Using retries without a breaker","Global breaker instead of per-dependency"]'::jsonb,
    'For non-critical fire-and-forget calls, a simple timeout may be enough.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Why is idempotency critical for retries?', 'why', cat_rel) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Network failures cause client to retry after server already processed","Without idempotency: duplicate charges, double writes","Use idempotency keys (UUID from client)","Server stores key + result for TTL","At-least-once + idempotent = effectively exactly-once"]'::jsonb,
    'Retries under failure produce duplicates; idempotency keys let the server safely deduplicate.',
    '["Where do you store keys?","How long is TTL?","What about idempotency across sharded systems?"]'::jsonb,
    '["Only relying on DB unique constraints","Retrying non-idempotent operations without keys"]'::jsonb,
    'For pure reads or naturally idempotent operations (SET x=5), keys are unnecessary.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('What is exponential backoff with jitter and why is jitter important?', 'why', cat_rel) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Wait time doubles per retry (1s, 2s, 4s...)","Jitter randomizes each wait","Prevents synchronized retry storms (thundering herd)","Cap max wait to bound tail latency","Combine with retry budget"]'::jsonb,
    'Backoff spreads retries in time; jitter spreads them across clients so recovery does not trigger a new spike.',
    '["Full jitter vs equal jitter?","When do you give up?"]'::jsonb,
    '["Retrying without cap","No jitter -> herd recovery"]'::jsonb,
    'For interactive user calls, backoff must be short enough to keep UX acceptable.');

  -- Monitoring
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('What are the RED and USE metrics frameworks?', 'recall', cat_mon) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["RED (services): Rate, Errors, Duration","USE (resources): Utilization, Saturation, Errors","RED for request-based systems","USE for CPU, memory, disk, network","Combine both for full picture"]'::jsonb,
    'RED characterizes request behavior; USE characterizes underlying resource health. Both together find issues faster.',
    '["Which alerts do you page on?","What SLIs map to RED?"]'::jsonb,
    '["Alerting on averages instead of percentiles","Ignoring saturation until it errors"]'::jsonb,
    'Batch/offline systems may need different metrics (lag, throughput, oldest-unprocessed).');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Why is distributed tracing critical in a microservices architecture?', 'why', cat_mon) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Correlates a single request across N services","Finds latency contributors and error origins","Requires trace/span IDs propagated in headers","Sampling controls cost","OpenTelemetry is the standard"]'::jsonb,
    'Without tracing, debugging a slow request in a graph of microservices becomes guesswork; traces show the actual causal path.',
    '["What sampling rate do you use?","How does context propagation work across queues?"]'::jsonb,
    '["Only tracing errors","No trace context in async work"]'::jsonb,
    'For small monoliths, logs + metrics may be enough.');

  -- Security
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('JWT vs session cookies — key trade-offs.', 'proscons', cat_sec) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["JWT: stateless, self-contained, hard to revoke early","Sessions: server state, easy revoke, small cookie","JWT: good for cross-service auth","Sessions: good when you control the frontend","Rotate keys and use short JWT expiry + refresh"]'::jsonb,
    'JWTs remove server state but complicate revocation; sessions add state but give you a kill switch.',
    '["How do you revoke a JWT before expiry?","Where do you store refresh tokens?"]'::jsonb,
    '["Long-lived JWTs without rotation","Storing JWTs in localStorage (XSS risk)"]'::jsonb,
    'For a single trusted client with tight session control, sessions are simpler and safer.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('When would you prefer OAuth 2.0 over API keys?', 'why', cat_sec) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Third-party access on behalf of a user","Fine-grained scoped permissions","Short-lived tokens with refresh","Standard user consent flow","Revocable without password change"]'::jsonb,
    'OAuth is for delegated user access with scopes; API keys are for machine-to-machine or first-party server access.',
    '["What about mTLS?","When is PKCE needed?"]'::jsonb,
    '["Using API keys for user delegation","Ignoring scope granularity"]'::jsonb,
    'For internal server-to-server calls, a rotating secret or mTLS is often simpler than full OAuth.');

  -- Tradeoffs
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('When would you deliberately choose eventual consistency?', 'why', cat_tr) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Read-heavy workloads with tolerance for staleness (feeds, counters, likes)","Cross-region low-latency requirements","High availability more valuable than strict correctness","When user cannot easily observe inconsistency","When compensating actions exist"]'::jsonb,
    'Eventual consistency buys availability and latency; it works when the domain tolerates a short window of staleness.',
    '["How stale is too stale?","How do you handle read-your-own-writes?"]'::jsonb,
    '["Using eventual consistency for balances / inventory","No monitoring on replication lag"]'::jsonb,
    'When users must see the exact result of their own action immediately (payments, bookings).');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('When should you avoid distributed transactions (2PC / sagas)?', 'why', cat_tr) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["When services can be redesigned to co-own data","When compensating actions are cheap","When eventual consistency + outbox pattern suffices","When latency budget is tight","When failure modes are hard to reason about"]'::jsonb,
    'Distributed transactions add coupling, latency, and complex failure modes; prefer patterns like the transactional outbox and sagas with idempotent steps.',
    '["What is the outbox pattern?","How do sagas handle partial failure?"]'::jsonb,
    '["Reaching for 2PC to avoid design work","No compensation strategy in sagas"]'::jsonb,
    'For truly atomic multi-record needs across services, keep the data in one service instead.');

  -- Troubleshooting
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('DB CPU is 20% but queries are timing out. First three hypotheses?', 'scenario', cat_tb) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Connection pool exhausted (app-side or PG max_connections)","Lock contention / long-running transactions blocking writes","Slow I/O or IOPS throttling on disk","Network latency between app and DB","Replication lag saturating replicas"]'::jsonb,
    'Low CPU with timeouts almost always points to waiting on locks, connections, or I/O — not compute.',
    '["How do you find blocking transactions?","How do you size the pool?"]'::jsonb,
    '["Scaling CPU when problem is I/O","Ignoring pg_stat_activity"]'::jsonb,
    'When CPU is high, this hypothesis set changes — look at query plans and hot queries first.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('Latency spikes only in P99, P50 is fine. Where do you look?', 'scenario', cat_tb) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["GC pauses in JVM / long stop-the-world","Cold cache misses on rare keys","Noisy neighbor / shared resource contention","Retries + backoff adding tail latency","Occasional slow query hitting cold pages"]'::jsonb,
    'P99 without P50 impact means most requests are healthy but a small tail hits a slow path — usually GC, cold cache, or noisy neighbors.',
    '["How do you detect GC contribution?","Would you add a hedge?"]'::jsonb,
    '["Scaling capacity when P50 is fine","Alerting only on P50"]'::jsonb,
    'When P50 also degrades, the whole system is at capacity; treat as a capacity problem instead.');

  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('A queue consumer lag keeps growing despite adding consumers. What are the likely causes?', 'scenario', cat_tb) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Partition count is the ceiling — more consumers than partitions do nothing","Downstream (DB / API) is the real bottleneck","Poison messages causing repeated retries","Consumer group rebalancing storms","Slow processing per message (unoptimized code)"]'::jsonb,
    'Scaling consumers only helps up to the partition count and only if downstream can absorb the throughput.',
    '["How do you repartition without downtime?","How would you detect a poison message?"]'::jsonb,
    '["Adding consumers past partition count","Ignoring downstream DB write latency"]'::jsonb,
    'If lag is small and transient (deploy, spike), no action needed — just monitor.');

  -- Follow-up style
  INSERT INTO public.questions (prompt, qtype, category_id) VALUES
    ('You designed a URL shortener. What if traffic becomes 100x overnight?', 'followup', cat_scale) RETURNING id INTO qid;
  INSERT INTO public.answer_keys VALUES (qid,
    '["Put reads behind CDN (short URLs are cacheable)","Add Redis cache for hot keys","Horizontally scale write path or pre-generate keys","Shard the mapping DB by hash of short code","Async analytics writes via queue"]'::jsonb,
    '100x usually means read amplification — push reads to the edge, cache hot keys, and offload analytics from the write path.',
    '["What if one short URL goes viral?","How do you prevent hot shard?"]'::jsonb,
    '["Scaling the DB only","Ignoring cache TTL / invalidation"]'::jsonb,
    'For low-traffic private links, none of this is needed.');
END $seed$;
