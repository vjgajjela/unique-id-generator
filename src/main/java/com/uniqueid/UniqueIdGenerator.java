package com.uniqueid;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * @author vjgajjela Distributed Sequence Generator
 * 
 *         This class should be used as Singleton. Make sure that you create and
 *         reuse a single instance of UniqueIdGenerator per node in your
 *         distributed system cluster.
 *
 */
@Component
@ConditionalOnProperty(name = "unique_id.init", havingValue = "true")
public class UniqueIdGenerator {

	// Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)
	@Value("${unique_id.custom_epoch : 1420070400000}")
	private Long customEpoch;

	@Value("${unique_id.node_bits}")
	private Integer nodeBits;

	@Value("${unique_id.sequence_bits}")
	private Integer sequenceBits;

	private Long nodeId;
	private Long maxNodeId;
	private Long maxSequence;

	private volatile long lastTimestamp = -1L;
	private volatile long sequence = 0L;

	private static final Logger LOG = LoggerFactory.getLogger(UniqueIdGenerator.class);

	@PostConstruct
	public void initUniqueIdGenerator() {
		this.maxNodeId = (1L << nodeBits) - 1;
		this.maxSequence = (1L << sequenceBits) - 1;
		this.nodeId = createNodeId();
	}

	public synchronized long nextId() {
		long currentTimestamp = timestamp();
		if (currentTimestamp < lastTimestamp) {
			throw new IllegalStateException("Invalid System Clock!");
		}
		if (currentTimestamp == lastTimestamp) {
			sequence = (sequence + 1) & maxSequence;
			if (sequence == 0) {
				// Sequence Exhausted, wait till next millisecond.
				currentTimestamp = waitNextMillis(currentTimestamp);
			}
		} else {
			// reset sequence to start with zero for the next millisecond
			sequence = 0;
		}
		lastTimestamp = currentTimestamp;
		return currentTimestamp << (nodeBits + sequenceBits) | (nodeId << sequenceBits) | sequence;
	}

	// Get current timestamp in milliseconds, adjust for the custom epoch.
	private long timestamp() {
		return Instant.now().toEpochMilli() - customEpoch;
	}

	// Block and wait till next millisecond
	private long waitNextMillis(long currentTimestamp) {
		while (currentTimestamp == lastTimestamp) {
			currentTimestamp = timestamp();
		}
		return currentTimestamp;
	}

	private long createNodeId() {
		long currentNodeId;
		try {
			StringBuilder sb = new StringBuilder();
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				byte[] mac = networkInterface.getHardwareAddress();
				if (mac != null) {
					for (byte macPort : mac) {
						sb.append(String.format("%02X", macPort));
					}
				}
			}
			LOG.info("UNQ-ID-GEN Node String : {}", sb);
			currentNodeId = sb.toString().hashCode();
		} catch (Exception ex) {
			LOG.info("UNQ-ID-GEN Exception : {}", ex.getMessage());
			currentNodeId = (new SecureRandom().nextInt());
		}
		currentNodeId = currentNodeId & maxNodeId;
		LOG.info("UNQ-ID-GEN Node Id : {}", currentNodeId);
		return currentNodeId;
	}
}
