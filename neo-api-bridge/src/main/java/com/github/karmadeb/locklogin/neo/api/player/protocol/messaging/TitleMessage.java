package com.github.karmadeb.locklogin.neo.api.player.protocol.messaging;

import org.jetbrains.annotations.ApiStatus.Experimental;

import java.util.Optional;


/**
 * Title message adapter
 */
@Experimental
public final class TitleMessage {

	private String title;
	private String subtitle;
	private float fadeIn;
	private float show;
	private float fadeOut;

	/**
	 * Create a new empty title message. By default, the
	 * values are empty title, empty subtitle, 0.5 seconds
	 * fade in, 5 seconds in-screen time and 1 second fade-out.
	 * Time units always in seconds
	 */
	public TitleMessage() {
		this("", "", 0.5f, 5, 1);
	}

	/**
	 * Create a new title message. By default, the values are
	 * 0.5 seconds fade in, 5 seconds in-screen time and 1 second
	 * fade-out.
	 * Time units always in seconds
	 * @param title the title
	 * @param subtitle the subtitle
	 */
	public TitleMessage(final String title,
						final String subtitle) {
		this(title, subtitle, 0.5f, 5, 1);
	}

	/**
	 * Create a new title message
	 * Time units always in seconds
	 * @param title the title
	 * @param subtitle the subtitle
	 * @param fadeIn the fade in time
	 * @param show the in-screen time
	 * @param fadeOut the fade-out time
	 */
	public TitleMessage(final String title,
						final String subtitle,
						final float fadeIn,
						final float show,
						final float fadeOut) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = Math.max(0.0f, fadeIn);
		this.show = Math.max(0.0f, show);
		this.fadeOut = Math.max(0.0f, fadeOut);
	}

	/**
	 * Get the title
	 * @return the title
	 */
	public Optional<String> title() {
		return Optional.ofNullable(this.title);
	}

	/**
	 * Get the subtitle
	 * @return the subtitle
	 */
	public Optional<String> subtitle() {
		return Optional.ofNullable(this.subtitle);
	}

	/**
	 * Get the fade in time
	 * @return the fade-in in ticks
	 */
	public int fadeIn() {
		float result = this.fadeIn * 20;
		return Math.round(result);
	}

	/**
	 * Get the in-screen time
	 * @return the time in ticks
	 */
	public int show() {
		float result = this.show * 20;
		return Math.round(result);
	}

	/**
	 * Get the fade out time
	 * @return the fade-out in ticks
	 */
	public int fadeOut() {
		float result = this.fadeOut * 20;
		return Math.round(result);
	}

	/**
	 * Update the title. The value can be null, and when
	 * so, the title is not updated. Useful when you want
	 * to update an existing title
	 * @param value the title
	 * @return this instance
	 */
	public TitleMessage title(final String value) {
		this.title = value;
		return this;
	}

	/**
	 * Update the subtitle. The value can be null, and when
	 * so, the subtitle is not updated. Useful when you want
	 * to update an existing title
	 * @param value the subtitle
	 * @return this instance
	 */
	public TitleMessage subtitle(final String value) {
		this.subtitle = value;
		return this;
	}

	/**
	 * Set the fade in time
	 * @param value the fade in time
	 * @return this instance
	 */
	public TitleMessage fadeIn(final float value) {
		this.fadeIn = Math.max(0.0f, value);
		return this;
	}

	/**
	 * Set the in-screen in time
	 * @param value the in-screen in time
	 * @return this instance
	 */
	public TitleMessage show(final float value) {
		this.show = Math.max(0.0f, value);
		return this;
	}

	/**
	 * Set the fade out time
	 * @param value the fade out time
	 * @return this instance
	 */
	public TitleMessage fadeOut(final float value) {
		this.fadeOut = Math.max(0.0f, value);
		return this;
	}
}
