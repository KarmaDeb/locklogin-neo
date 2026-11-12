package com.github.karmadeb.locklogin.neo.api.player.protocol.messaging;

import org.jetbrains.annotations.ApiStatus.Experimental;

import java.util.Optional;


/**
 * Actionbar message adapter
 */
@Experimental
public final class ActionbarMessage {

	private String content;
	private float time;

	/**
	 * Create a new empty title message. By default, the
	 * values are empty message, no time
	 */
	public ActionbarMessage() {
		this("", 0);
	}

	/**
	 * Create a new actionbar message. Time units always in seconds
	 * @param content the content
	 * @param time the time
	 */
	public ActionbarMessage(final String content,
							final float time) {
		this.content = content;
		this.time = time;
	}

	/**
	 * Get the content
	 * @return the content
	 */
	public Optional<String> content() {
		return Optional.ofNullable(this.content);
	}

	/**
	 * Get the display time
	 * @return the display time in ticks
	 */
	public int time() {
		float result = this.time * 20;
		return Math.round(result);
	}

	/**
	 * Update the content. The value can be null, and when
	 * so, the content is not updated. Useful when you want
	 * to stop showing an actionbar
	 * @param value the content
	 * @return this instance
	 */
	public ActionbarMessage content(final String value) {
		this.content = value;
		return this;
	}

	/**
	 * Set the display time
	 * @param value the display time
	 * @return this instance
	 */
	public ActionbarMessage fadeIn(final float value) {
		this.time = Math.max(0.0f, value);
		return this;
	}
}
