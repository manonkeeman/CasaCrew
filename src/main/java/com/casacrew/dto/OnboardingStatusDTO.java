package com.casacrew.dto;

public record OnboardingStatusDTO(
        boolean profileComplete,
        boolean roomsComplete,
        boolean rentSettingsComplete,
        boolean paymentSettingsComplete,
        boolean huisregelsComplete,
        boolean studentsComplete,
        boolean cleanerComplete,
        int completedSteps,
        int totalSteps
) {
    public static OnboardingStatusDTO of(
            boolean profileComplete,
            boolean roomsComplete,
            boolean rentSettingsComplete,
            boolean paymentSettingsComplete,
            boolean huisregelsComplete,
            boolean studentsComplete,
            boolean cleanerComplete
    ) {
        int completed = 0;
        for (boolean step : new boolean[]{
                profileComplete, roomsComplete, rentSettingsComplete,
                paymentSettingsComplete, huisregelsComplete, studentsComplete, cleanerComplete
        }) {
            if (step) completed++;
        }
        return new OnboardingStatusDTO(
                profileComplete, roomsComplete, rentSettingsComplete,
                paymentSettingsComplete, huisregelsComplete, studentsComplete, cleanerComplete,
                completed, 7
        );
    }
}
