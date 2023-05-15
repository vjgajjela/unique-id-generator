package com.uniqueid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "unique_id.init", havingValue = "true")
public class UniqueIdGeneratorService {

	@Value("${unique_id.charset : 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz}")
	private String charSet;

	@Value("${unique_id.length}")
	private Integer length;

	@Autowired
	private UniqueIdGenerator idGenerator;

	private static final String ZERO_PAD = "0";

	private static final Logger LOG = LoggerFactory.getLogger(UniqueIdGeneratorService.class);

	/**
	 * @return This method returns Alpha-numeric unique id of length provided in
	 *         ${unique_id.length} using character set ${unique_id.charset} or
	 *         default character set.
	 */
	public String generateAlphaNumericId() {
		String id = convertToBase(idGenerator.nextId() % (long) Math.pow(10, length));
		LOG.debug("AlphaNumericId id : {}", id);
		return id;
	}

	/**
	 * @return This method returns Numeric unique id of length provided in
	 *         ${unique_id.length} using character set ${unique_id.charset} or
	 *         default character set.
	 */
	public String generateNumericId() {
		String id = StringUtils.leftPad(Long.toString(idGenerator.nextId() % (long) Math.pow(10, length)), length,
				ZERO_PAD);
		LOG.debug("NumericId id : {}", id);
		return id;
	}

	/**
	 * @param id
	 * @return This method will trim the extra digits from the left and convert the
	 *         id to alpha-num using division and mod 4909487288248320 to
	 *         4nuBqWHmsqQGi9Uq. Appending zero is required in case the id generated
	 *         is like 550000009487288248, it will become 9487288248 first and
	 *         converted to alpha-num which will result in id less than 16.
	 */
	private String convertToBase(long id) {
		int base = charSet.length();
		StringBuilder builder = new StringBuilder();
		while (id != 0) {
			builder.append(charSet.charAt((int) (id % base)));
			id = id / 10;
		}
		return StringUtils.leftPad(builder.reverse().toString(), length, ZERO_PAD);
	}

}
