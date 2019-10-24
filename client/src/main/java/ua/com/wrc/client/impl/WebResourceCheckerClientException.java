package ua.com.wrc.client.impl;

/**
 * Checked exception thrown by {@link ua.com.wrc.client.WebResourceCheckerClient} implementations.
 *
 * @author Igor Volnov
 */
public class WebResourceCheckerClientException extends Exception {

    public WebResourceCheckerClientException(Throwable cause) {
        super(cause);
    }
}
