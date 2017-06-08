package com.fooock.ticket.opencv;

import android.os.Build;

/**
 * Check the android version
 */
final class CheckVersion {

    /**
     * Check the given android version code in the current device
     *
     * @param version android version code to check
     * @return true if the device is the specified version, false if not
     */
    boolean isEqualTo(final int version) {
        return Build.VERSION.SDK_INT == version;
    }

    /**
     * Check if the given version code is equal or greater than the device version
     *
     * @param version android version code to check
     * @return true if the device version is equal or greater, false if not
     */
    boolean isEqualOrGreater(final int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    /**
     * Check if the given version code is greater than the device version
     *
     * @param version android version code to check
     * @return true if the device version is greater, false if not
     */
    boolean isGreaterThan(final int version) {
        return Build.VERSION.SDK_INT > version;
    }
}