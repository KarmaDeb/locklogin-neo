package com.github.karmadeb.locklogin.neo.api.extension.description;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an author, mostly used for
 * extensions. The format in which LockLogin
 * parses an author is
 * {@code [username] <[address]> ([website])}
 * @param username the author username, could be a full name
 * @param address the author mail address
 * @param website the author website
 */
public record Author(String username, Optional<String> address, Optional<URI> website) {

	public static final Pattern PATTERN = Pattern.compile(
			"^(?<username>.*?)(\\s*<(?<address>[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}?)>(\\s*(?<website>https?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?))?)?$");

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Author(String username1, Optional<String> address1, Optional<URI> website1)))
			return false;

		if (!this.username.equals(username1)) return false;
		if (this.address.isPresent() != address1.isPresent()) return false;
		if (this.website.isPresent() != website1.isPresent()) return false;

		if (this.address.isPresent()) {
			String thisAddr = this.address.get();
			String otherAddr = address1.get();

			if (!thisAddr.equals(otherAddr)) return false;
		}

		if (this.website.isPresent()) {
			URI thisWeb = this.website.get();
			URI otherWeb = website1.get();

			return thisWeb.compareTo(otherWeb) == 0;
		}

		return true;
	}

	@Override
	public int hashCode() {
		List<String> objects = new ArrayList<>(3);
		objects.add(this.username);
		this.address.ifPresent(objects::add);
		this.website.map(URI::toString).ifPresent(objects::add);

		return Objects.hash(objects.toArray());
	}

	@Override
	@NotNull
	public String toString() {
		StringBuilder builder = new StringBuilder(this.username);
		this.address.ifPresent(address ->
				builder.append(" <").append(address).append(">"));
		this.website.map(URI::toString).ifPresent(website ->
				builder.append(" (").append(website).append(")"));

		return builder.toString();
	}

	/**
	 * Parse an author from a string
	 * @param input the input string
	 * @return the parsed author
	 */
	public static Optional<Author> parse(final String input) {
		Matcher matcher = PATTERN.matcher(input);
		if (!matcher.matches()) {
			return Optional.empty();
		}

		String username = matcher.group("username");
		String address = matcher.group("address");
		String rawWebsite = matcher.group("website");

		URI website = null;
		if (rawWebsite != null) {
			website = URI.create(rawWebsite);
		}

		Author author = new Author(username,
				Optional.ofNullable(address),
				Optional.ofNullable(website));
		return Optional.of(author);
	}
}
