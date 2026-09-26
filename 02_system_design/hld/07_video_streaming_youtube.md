# System Design 6: Video Streaming Platform (YouTube / Netflix)

## 1. Problem Statement & Requirements

Design a global video streaming platform (like YouTube or Netflix) supporting video uploads, multi-resolution transcoding, adaptive bitrate streaming, and global CDN delivery.

### Functional Requirements:
* Upload raw video files (4K, 1080p, 720p).
* Stream videos smoothly without buffering across diverse network connections (3G, 4G, 5G, Fiber).
* View video search results, channel metadata, and view count analytics.

### Non-Functional Requirements:
* Scale: Upload 100,000 videos/day; serve 1 Billion video hours viewed/day.
* High Availability & Low Playback Startup Latency ($< 1\text{ second}$ initial buffer).
* Global Content Delivery: CDN edge caching.

---

## 2. Adaptive Bitrate Streaming (ABR) & Transcoding

Raw uploaded video files (e.g., 50GB uncompressed `.mov` files) cannot be streamed directly to mobile devices. They must be split into small chunks and transcoded into multiple formats and resolutions.

```
 Raw Uploaded Video File (50 GB MOV)
                 |
                 v
 Transcoding DAG Workflow Pipeline
                 |
                 +-----------------------+-----------------------+
                 |                       |                       |
                 v                       v                       v
          1080p H.264 (5 Mbps)     720p H.264 (2 Mbps)     480p H.264 (800 Kbps)
                 |                       |                       |
                 v                       v                       v
          Chunk Into 2-Second    Chunk Into 2-Second    Chunk Into 2-Second
          Segment Files (.ts)    Segment Files (.ts)    Segment Files (.ts)
                 |                       |                       |
                 +-----------------------+-----------------------+
                                         |
                                         v
                         Master Manifest File (master.m3u8)
                         (HLS / MPEG-DASH Streaming Protocol)
```

### Adaptive Bitrate Streaming (HLS / MPEG-DASH):
The client media player inspects network bandwidth dynamically every 2 seconds. If bandwidth drops, the player seamlessly requests the next 2-second chunk from the 480p manifest without stopping playback.

---

## 3. End-to-End System Blueprint

```
 Upload Client                                API Gateway & Transcoding Service                                CDN Edge Network
      |                                                      |                                                       |
 1. Initiate Upload (Presigned URL) ------------------------>|                                                       |
      |                                                      |                                                       |
 2. Stream Raw Video Data directly to Object Storage (S3)    |                                                       |
      |----------------------------------------------------->|                                                       |
                                                             | 3. Publish Message to Kafka Transcode Queue           |
                                                             v                                                       |
                                                +---------------------------+                                        |
                                                | Transcoding Worker Pool   |                                        |
                                                | (FFmpeg / AWS Elemental)  |                                        |
                                                +-------------+-------------+                                        |
                                                              |                                                      |
                                                              | 4. Stores HLS Transcoded Segments (.ts, .m3u8)       |
                                                              v                                                      |
                                                +---------------------------+                                        |
                                                | S3 Output Object Storage  |                                        |
                                                +-------------+-------------+                                        |
                                                              |                                                      |
 Playback Client                                              | 5. Replicates Video Segments to Edge CDN Cache       |
      |                                                       +----------------------------------------------------->|
      |                                                                                                              |
      | 6. GET Stream Manifest & Video Segments -------------------------------------------------------------------->|
      |<-------------------------------------------------------------------------------------------------------------|
```

---

## 4. Scalability Bottlenecks & Video CDN Optimization

1. **Multi-CDN Routing Strategy:** Routing video stream requests across multiple CDN providers (Akamai, Cloudflare, Fastly, AWS CloudFront) based on regional ISP performance.
2. **Edge Pre-fetching & Popularity Tiering:** Caching the first 10 seconds (initial 5 chunks) of newly trending videos at all edge CDN locations to guarantee sub-second playback start.
3. **Database Sharding for Analytics:** Sharding video metadata by `video_id` using Cassandra or Redis to track view counts without locking database rows.
