package pl.zpo.app.domain.config;

import java.util.List;

/**
 * The active domain description. This is the single object that turns the generic engine into a
 * concrete subject (car rental, lockers, cinema, vet, room booking, ...). It carries display
 * labels, the algorithm mode + pricing unit, and the dynamic field definitions for resources and
 * requests. Built in {@link DomainProfileProvider}.
 *
 * @param domainName            human name of the deployed system
 * @param resourceLabelSingular label for one resource
 * @param resourceLabelPlural   plural resource label
 * @param requestLabelSingular  label for one request
 * @param requestLabelPlural    plural request label
 * @param currency              ISO currency code used for calculated values
 * @param algorithmMode         which checks the algorithm performs
 * @param pricingUnit           how baseValue scales into a value
 * @param requiresTimeWindow    whether requests must provide start/end
 * @param requiresQuantity      whether requests must provide a quantity
 * @param resourceFields        dynamic metadata fields for resources
 * @param requestFields         dynamic metadata fields for requests
 */
public record DomainProfile(
        String domainName,
        String resourceLabelSingular,
        String resourceLabelPlural,
        String requestLabelSingular,
        String requestLabelPlural,
        String currency,
        AlgorithmMode algorithmMode,
        PricingUnit pricingUnit,
        boolean requiresTimeWindow,
        boolean requiresQuantity,
        List<DomainFieldConfig> resourceFields,
        List<DomainFieldConfig> requestFields
) {

    public DomainProfile {
        resourceFields = resourceFields == null ? List.of() : List.copyOf(resourceFields);
        requestFields = requestFields == null ? List.of() : List.copyOf(requestFields);
    }
}
