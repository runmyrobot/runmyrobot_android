package tv.letsrobot.android.api.enums

/**
 * Status of components. Used for status indicators mainly
 */
enum class ComponentStatus {
    /**
     * When Component has been disabled via settings
     */
    DISABLED_FROM_SETTINGS,
    /**
     * When Component has been disabled. This is the starting state as well
     */
    DISABLED,
    /**
     * State set right when component is enabled and before component takes over
     */
    CONNECTING,
    /**
     * State when component has not seen any issues recently, and is connected to its endpoint
     */
    STABLE,
    /**
     * State when component is seeing frequent errors occurring
     */
    INTERMITTENT,
    /**
     * State when something is preventing the component from running
     */
    ERROR
}