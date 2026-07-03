package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.availability.CapacityMatcher;
import pl.zpo.app.domain.availability.TimeCollisionDetector;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    private final TimeCollisionDetector collisionDetector;
    private final CapacityMatcher capacityMatcher;

    public DefaultDomainAlgorithm(TimeCollisionDetector collisionDetector, CapacityMatcher capacityMatcher) {
        this.collisionDetector = collisionDetector;
        this.capacityMatcher = capacityMatcher;
    }

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        if (isShopInput(input)) return evaluateShop(input);
        return evaluateGeneric(input);
    }

    private boolean isShopInput(DomainAlgorithmInput input) {
        return hasAny(input.requestMetadata(), "quantity", "customerName", "totalPrice")
                || (input.resource() != null && hasAny(input.resource().getMetadata(),
                "price", "category", "stock"));
    }

    private DomainAlgorithmResult evaluateShop(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            breakdown.addRule("PRODUCT_CHECK: brak produktu");
            return DomainAlgorithmResult.failure(List.of("Produkt jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            breakdown.addRule("PRODUCT_CHECK: produkt niedostepny");
            return DomainAlgorithmResult.failure(List.of("Produkt nie jest dostepny"), breakdown.build());
        }
        breakdown.addRule("PRODUCT_CHECK: ok");

        Integer stock = readInteger(resource.getMetadata(), "stock");
        Integer quantity = readInteger(input.requestMetadata(), "quantity");
        if (quantity == null || quantity <= 0) {
            errors.add("Podaj ilość sztuk większą od 0");
            breakdown.addRule("QUANTITY_CHECK: nieprawidlowa ilosc");
        } else if (stock == null || stock < quantity) {
            errors.add("Brak wystarczającej ilości na stanie (dostępne: " + (stock != null ? stock : 0) + ")");
            breakdown.addRule("STOCK_CHECK: za malo (stan=" + stock + ", zamowienie=" + quantity + ")");
        } else {
            breakdown.addRule("STOCK_CHECK: ok (stan=" + stock + ", zamowienie=" + quantity + ")");
        }

        if (!errors.isEmpty()) {
            return DomainAlgorithmResult.failure(errors, breakdown.build());
        }

        BigDecimal price = readDecimal(resource.getMetadata(), "price");
        if (price == null) price = resource.getBaseValue() != null ? resource.getBaseValue() : BigDecimal.ZERO;
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        breakdown.addLine("totalPrice", total, quantity + " x " + price.toPlainString() + " PLN");
        breakdown.addRule("ORDER_TOTAL: " + total.toPlainString() + " PLN");

        return DomainAlgorithmResult.success(total, resource.getId(), breakdown.build());
    }

    // ----- generic -----
    private DomainAlgorithmResult evaluateGeneric(DomainAlgorithmInput input) {
        DomainProfile profile = input.profile();
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder(profile.currency());
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Zasób jest wymagany"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Zasób nie jest aktywny"), breakdown.build());
        }
        breakdown.addRule("RESOURCE_CHECK: ok");

        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());
        return DomainAlgorithmResult.success(null, resource.getId(), breakdown.build());
    }

    private BigDecimal readDecimal(Map<String, Object> m, String k) {
        if (m == null || m.get(k) == null) return null;
        Object v = m.get(k);
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    private Integer readInteger(Map<String, Object> m, String k) {
        if (m == null || m.get(k) == null) return null;
        Object v = m.get(k);
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    private boolean hasAny(Map<String, Object> m, String... keys) {
        if (m == null) return false;
        for (String k : keys) if (m.containsKey(k)) return true;
        return false;
    }
}
