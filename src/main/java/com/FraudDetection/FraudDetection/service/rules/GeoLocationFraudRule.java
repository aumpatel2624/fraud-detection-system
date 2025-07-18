package com.FraudDetection.FraudDetection.service.rules;

import com.FraudDetection.FraudDetection.entity.Transaction;
import com.FraudDetection.FraudDetection.repository.TransactionRepository;
import com.FraudDetection.FraudDetection.service.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class GeoLocationFraudRule extends AbstractFraudRule {
    
    private final TransactionRepository transactionRepository;
    
    @Value("${fraud.rules.geo.max-distance-km:1000}")
    private double maxDistanceKm;
    
    @Value("${fraud.rules.geo.min-time-between-locations-minutes:60}")
    private int minTimeBetweenLocationsMinutes;
    
    @Value("${fraud.rules.geo.high-risk-countries:}")
    private List<String> highRiskCountries;
    
    public GeoLocationFraudRule(TransactionRepository transactionRepository) {
        super("GEO_LOCATION_RULE", "1.0", "Detects impossible travel and suspicious geographical patterns", true, 90);
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    protected RuleResult executeRule(Transaction transaction) {
        String accountId = transaction.getAccountId();
        String currentLocation = transaction.getLocation();
        String currentCountry = extractCountryFromLocation(transaction.getLocation());
        LocalDateTime currentTime = transaction.getTimestamp();
        
        // Check for impossible travel
        ImpossibleTravelCheck travelCheck = checkImpossibleTravel(accountId, currentLocation, currentTime);
        
        // Check for high-risk country
        boolean isHighRiskCountry = checkHighRiskCountry(currentCountry);
        
        // Check for multiple countries in short period
        MultiCountryCheck multiCountryCheck = checkMultipleCountries(accountId, currentTime);
        
        // Determine if rule is triggered
        boolean triggered = travelCheck.isImpossibleTravel() || isHighRiskCountry || multiCountryCheck.isSuspicious();
        
        if (triggered) {
            BigDecimal score = calculateGeoScore(travelCheck, isHighRiskCountry, multiCountryCheck);
            String reason = buildGeoReason(travelCheck, isHighRiskCountry, multiCountryCheck, currentCountry);
            
            Map<String, Object> ruleData = new HashMap<>();
            ruleData.put("currentLocation", currentLocation);
            ruleData.put("currentCountry", currentCountry);
            ruleData.put("impossibleTravel", travelCheck.isImpossibleTravel());
            ruleData.put("distanceKm", travelCheck.getDistanceKm());
            ruleData.put("timeDifferenceMinutes", travelCheck.getTimeDifferenceMinutes());
            ruleData.put("isHighRiskCountry", isHighRiskCountry);
            ruleData.put("countryCount", multiCountryCheck.getCountryCount());
            ruleData.put("accountId", accountId);
            
            return RuleResult.builder()
                .ruleName(ruleName)
                .triggered(true)
                .score(score)
                .reason(reason)
                .severity(determineSeverity(score))
                .ruleData(ruleData)
                .recommendation(getGeoRecommendation(score, isHighRiskCountry))
                .build();
        }
        
        return createNotTriggeredResult();
    }
    
    private ImpossibleTravelCheck checkImpossibleTravel(String accountId, String currentLocation, LocalDateTime currentTime) {
        LocalDateTime lookbackTime = currentTime.minusHours(24);
        
        Optional<Transaction> lastTransaction = transactionRepository
            .findFirstByAccountIdAndTimestampBeforeOrderByTimestampDesc(accountId, currentTime);
        
        if (lastTransaction.isEmpty()) {
            return ImpossibleTravelCheck.builder()
                .impossibleTravel(false)
                .distanceKm(0.0)
                .timeDifferenceMinutes(0)
                .build();
        }
        
        Transaction last = lastTransaction.get();
        String lastLocation = last.getLocation();
        
        // Calculate distance between locations (simplified calculation)
        double distance = calculateDistance(lastLocation, currentLocation);
        
        // Calculate time difference in minutes
        long timeDiffMinutes = java.time.Duration.between(last.getTimestamp(), currentTime).toMinutes();
        
        // Check if travel is impossible (distance too great for time period)
        // Assuming maximum travel speed of 800 km/h (commercial aircraft)
        double maxPossibleDistance = (timeDiffMinutes / 60.0) * 800;
        boolean impossibleTravel = distance > maxPossibleDistance && timeDiffMinutes < minTimeBetweenLocationsMinutes;
        
        return ImpossibleTravelCheck.builder()
            .impossibleTravel(impossibleTravel)
            .distanceKm(distance)
            .timeDifferenceMinutes((int) timeDiffMinutes)
            .lastLocation(lastLocation)
            .lastTransactionTime(last.getTimestamp())
            .build();
    }
    
    private boolean checkHighRiskCountry(String country) {
        if (country == null || highRiskCountries == null) {
            return false;
        }
        return highRiskCountries.contains(country.toUpperCase());
    }
    
    private MultiCountryCheck checkMultipleCountries(String accountId, LocalDateTime currentTime) {
        LocalDateTime lookbackTime = currentTime.minusHours(6);
        
        List<String> recentLocations = transactionRepository
            .findDistinctCountriesByAccountIdAndTimestampBetween(accountId, lookbackTime, currentTime);
        
        // Extract unique countries from the location strings
        List<String> recentCountries = recentLocations.stream()
            .map(this::extractCountryFromLocation)
            .distinct()
            .toList();
        
        int countryCount = recentCountries.size();
        boolean suspicious = countryCount > 3; // More than 3 countries in 6 hours is suspicious
        
        return MultiCountryCheck.builder()
            .countryCount(countryCount)
            .countries(recentCountries)
            .suspicious(suspicious)
            .build();
    }
    
    private double calculateDistance(String location1, String location2) {
        // Simplified distance calculation
        // In a real implementation, you would use actual coordinates and proper distance calculation
        if (location1 == null || location2 == null || location1.equals(location2)) {
            return 0.0;
        }
        
        // Mock calculation based on location string similarity
        // This should be replaced with actual geo-coordinate distance calculation
        if (location1.equalsIgnoreCase(location2)) {
            return 0.0;
        }
        
        // Simple heuristic: different cities = 100km, different states/provinces = 500km, different countries = 2000km
        String[] parts1 = location1.split(",");
        String[] parts2 = location2.split(",");
        
        if (parts1.length >= 2 && parts2.length >= 2) {
            String country1 = parts1[parts1.length - 1].trim();
            String country2 = parts2[parts2.length - 1].trim();
            
            if (!country1.equalsIgnoreCase(country2)) {
                return 2000.0; // Different countries
            }
            
            String state1 = parts1[parts1.length - 2].trim();
            String state2 = parts2[parts2.length - 2].trim();
            
            if (!state1.equalsIgnoreCase(state2)) {
                return 500.0; // Different states/provinces
            }
            
            return 100.0; // Different cities
        }
        
        return 1000.0; // Default distance for unclear locations
    }
    
    private BigDecimal calculateGeoScore(ImpossibleTravelCheck travelCheck, boolean isHighRiskCountry, MultiCountryCheck multiCountryCheck) {
        BigDecimal score = BigDecimal.ZERO;
        
        // Impossible travel is the highest risk
        if (travelCheck.isImpossibleTravel()) {
            score = score.add(BigDecimal.valueOf(80));
            
            // Additional score based on how impossible the travel is
            if (travelCheck.getTimeDifferenceMinutes() < 30) {
                score = score.add(BigDecimal.valueOf(15));
            }
        }
        
        // High-risk country adds significant score
        if (isHighRiskCountry) {
            score = score.add(BigDecimal.valueOf(60));
        }
        
        // Multiple countries in short time adds moderate score
        if (multiCountryCheck.isSuspicious()) {
            score = score.add(BigDecimal.valueOf(40));
            
            // Additional score for more countries
            int extraCountries = multiCountryCheck.getCountryCount() - 3;
            score = score.add(BigDecimal.valueOf(extraCountries * 10));
        }
        
        // Cap the score at 100
        return score.min(BigDecimal.valueOf(100));
    }
    
    private String buildGeoReason(ImpossibleTravelCheck travelCheck, boolean isHighRiskCountry, 
                                  MultiCountryCheck multiCountryCheck, String currentCountry) {
        StringBuilder reason = new StringBuilder("Geographical anomaly detected: ");
        
        if (travelCheck.isImpossibleTravel()) {
            reason.append(String.format("Impossible travel detected - %.2f km in %d minutes; ", 
                travelCheck.getDistanceKm(), travelCheck.getTimeDifferenceMinutes()));
        }
        
        if (isHighRiskCountry) {
            reason.append(String.format("Transaction from high-risk country: %s; ", currentCountry));
        }
        
        if (multiCountryCheck.isSuspicious()) {
            reason.append(String.format("Multiple countries (%d) accessed in short period; ", 
                multiCountryCheck.getCountryCount()));
        }
        
        return reason.toString().trim();
    }
    
    private String extractCountryFromLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        // Extract country from location string (assuming format: "City, State, Country")
        String[] parts = location.split(",");
        if (parts.length >= 3) {
            return parts[parts.length - 1].trim().toUpperCase();
        } else if (parts.length == 2) {
            return parts[1].trim().toUpperCase();
        } else {
            return parts[0].trim().toUpperCase();
        }
    }
    
    private String getGeoRecommendation(BigDecimal score, boolean isHighRiskCountry) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "IMMEDIATE_BLOCK_REQUIRED";
        } else if (isHighRiskCountry || score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "ENHANCED_AUTHENTICATION_REQUIRED";
        } else {
            return "ADDITIONAL_VERIFICATION";
        }
    }
    
    @lombok.Data
    @lombok.Builder
    private static class ImpossibleTravelCheck {
        private boolean impossibleTravel;
        private double distanceKm;
        private int timeDifferenceMinutes;
        private String lastLocation;
        private LocalDateTime lastTransactionTime;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class MultiCountryCheck {
        private int countryCount;
        private List<String> countries;
        private boolean suspicious;
    }
}