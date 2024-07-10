package org.eclipse.winery.lsp.Server.ServerAPI.API;

public interface ClientLogManager {
    /**
    * This class provides a set of generic APIs to the server to 
    * publish various types of logs.
    * <p>
    * When we publish diagnostics via protocol operations, we 
    * need to generate a specific set of data models which is common. Therefore, a single 
    * logger instance is going to be used within the server’s core implementation. 
    * </p>
    *
    */    
    
    /**
     * Log an Info message to the client.
     *
     * @param message {@link String}
     */
    void publishInfo(String message);

    /**
     * Log a Log message to the client.
     *
     * @param message {@link String}
     */
    void publishLog(String message);

    /**
     * Log an Error message to the client.
     *
     * @param message {@link String}
     */
    void publishError(String message);

    /**
     * Log a Warning message to the client.
     *
     * @param message {@link String}
     */
    void publishWarning(String message);

    /**
     * Show an error message to the client.
     *
     * @param message message to be shown
     */
    void showErrorMessage(String message);

    /**
     * Show an info message to the client.
     *
     * @param message message to be shown
     */
    void showInfoMessage(String message);

    /**
     * Show a log message to the client.
     *
     * @param message message to be shown
     */
    void showLogMessage(String message);
}
